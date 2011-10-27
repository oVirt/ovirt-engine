package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ListImageIdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetImageLegalityVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public final class ImagesSyncronizer {
    private static final ImagesSyncronizer _imagesSyncronizer = new ImagesSyncronizer();
    private static int _imageRefreshRate;

    private ImagesSyncronizer() {
        _imageRefreshRate = Config.<Integer> GetValue(ConfigValues.ImagesSyncronizationTimeout);
        if (_imageRefreshRate != 0) {
            // /Should be changed to support current Storage file structure.
            // SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
            // "OnTimer",
            // new Class[0], new Object[0],
            // 0, _imageRefreshRate,
            // TimeUnit.HOURS);
        }
    }

    public static ImagesSyncronizer getInstance() {
        return _imagesSyncronizer;
    }

    @OnTimerMethodAnnotation("OnTimer")
    public void OnTimer() {
        Syncronize();

    }

    public void Syncronize() {
        /**
         * Get All Images from IRS
         */

        java.util.ArrayList<Guid> imagesIdsFromIrs = GetImagesFromIrs();
        java.util.ArrayList<Guid> inVdcButNotInIrs = new java.util.ArrayList<Guid>();
        List<DiskImage> imagesFromVdc = GetImagesFromVdc();
        for (DiskImage image : imagesFromVdc) {
            if (!imagesIdsFromIrs.contains(image.getId()) && image.getimageStatus() != ImageStatus.ILLEGAL) {
                inVdcButNotInIrs.add(image.getId());
            } else {
                imagesIdsFromIrs.remove(image.getId());
            }
        }
        java.util.ArrayList<Guid> imageIdsFromIrsNotInVdc = new java.util.ArrayList<Guid>();
        for (Guid imageId : imagesIdsFromIrs) {
            // todo - omer what parameters to send here? and why this code runs
            // again?!
            DiskImage imageFromIrs = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(Guid.Empty, Guid.Empty, Guid.Empty, imageId))
                    .getReturnValue();
            if (imageFromIrs.getimageStatus() != ImageStatus.ILLEGAL) {
                imageIdsFromIrsNotInVdc.add(imageId);
            }
        }
        /**
         * Remove all images, found in Irs and not found in Vdc
         */
        ProceedImagesNotFoundInVdc(imageIdsFromIrsNotInVdc);
        ProceedImagesNotFoundInIrs(inVdcButNotInIrs);
    }

    private static java.util.ArrayList<Guid> GetImagesFromIrs() {
        // todo - omer review this - not sending any parameters should get all
        // volumes
        java.util.ArrayList<Guid> imagesIdsFromIrs = new java.util.ArrayList<Guid>(
                java.util.Arrays.asList((Guid[]) Backend.getInstance().getResourceManager()
                        .RunVdsCommand(VDSCommandType.ListImageIds, new ListImageIdsVDSCommandParameters())
                        .getReturnValue()));
        java.util.ArrayList<Guid> toRemoveFromIrs = new java.util.ArrayList<Guid>();
        for (Guid imageId : imagesIdsFromIrs) {
            // todo - omer what parameters to send here?
            DiskImage image = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(Guid.Empty, Guid.Empty, Guid.Empty, imageId))
                    .getReturnValue();
            if (image.getimageStatus() == ImageStatus.ILLEGAL) {
                toRemoveFromIrs.add(imageId);
            }
        }
        for (Guid toRemove : toRemoveFromIrs) {
            imagesIdsFromIrs.remove(toRemove);
        }
        return imagesIdsFromIrs;
    }

    private static List<DiskImage> GetImagesFromVdc() {
        List<DiskImage> imagesFromVdc = DbFacade.getInstance().getDiskImageDAO().getAll();
        List<DiskImage> toRemove = new java.util.ArrayList<DiskImage>();
        for (DiskImage image : imagesFromVdc) {
            if (image.getimageStatus() == ImageStatus.ILLEGAL) {
                toRemove.add(image);
            }
        }
        for (DiskImage image : toRemove) {
            imagesFromVdc.remove(image);
        }
        return imagesFromVdc;
    }

    private static AuditLogType DesktopNotExistInVdcTreatment(Guid imageId, AuditLogableBase logable) {
        AuditLogType returnValue;
        DiskImageTemplate dt = DbFacade.getInstance().getDiskImageTemplateDAO().get(imageId);

        if (dt != null) {

            VmTemplate template = DbFacade.getInstance().getVmTemplateDAO()
                    .get(dt.getvmt_guid());
            logable.getCustomValues().put("TemplateName", template.getname());
            logable.setVmTemplateId(template.getId());
            /**
             * Error message: Found Image without vm.
             */
            returnValue = AuditLogType.IMAGES_SYNCRONIZER_DESKTOP_NOT_EXIST_IN_VDC;
        } else {
            returnValue = AuditLogType.IMAGES_SYNCRONIZER_TEMPLATE_NOT_EXIST_IMAGE_EXIST;
            /**
             * Error. Zombi in Vdc database.
             */
        }
        return returnValue;

    }

    private static AuditLogType SnapshotNotExistInVdcTreatment(Guid imageId, AuditLogableBase logable) {
        /**
         * Vm snapshot
         */
        image_vm_map map = DbFacade.getInstance().getImageVmMapDAO().getByImageId(imageId);
        AuditLogType returnValue;
        if (map != null) {
            VM vm = DbFacade.getInstance().getVmDAO().getById(map.getvm_id());
            logable.setVmId(vm.getvm_guid());
            returnValue = AuditLogType.IMAGES_SYNCRONIZER_SNAPSHOT_NOT_EXIST_IN_VDC;
            /**
             * Error message: Found VM snapshot not in Vdc.
             */

        } else {
            returnValue = AuditLogType.IMAGES_SYNCRONIZER_SNAPSHOTS_NOT_ATTACHED_TO_VM_IN_VDC;
            /**
             * Error. Zombi in Vdc database.
             */
        }
        return returnValue;
    }

    private static void ProceedImagesNotFoundInVdc(java.util.ArrayList<Guid> images) {
        java.util.HashMap<Guid, Integer> proceeded = new java.util.HashMap<Guid, Integer>();
        for (Guid image : images) {
            if (!proceeded.containsKey(image)) {
                Iterable<DiskImage> snapshots = ImagesHandler.getAllImageSnapshotsFromIrs(image, Guid.Empty);
                boolean isFound = false;
                AuditLogableBase logable = new AuditLogableBase();
                AuditLogType type = AuditLogType.UNASSIGNED;
                // todo - omer what parameters to send here?
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.SetImageLegality,
                                new SetImageLegalityVDSCommandParameters(Guid.Empty, Guid.Empty, Guid.Empty, image,
                                        false));
                /**
                 * TODO: set image illegal in IRS
                 */
                for (DiskImage diskImage : snapshots) {
                    logable.AppendCustomValue("SnapshotDescription", diskImage.getdescription(), ",");
                    if (!proceeded.containsKey(diskImage.getId())) {
                        proceeded.put(diskImage.getId(), 0);
                    }
                    Guid storagePoolId = diskImage.getstorage_pool_id() != null ? diskImage.getstorage_pool_id()
                            .getValue() : Guid.Empty;
                    Guid storageDomainId = diskImage.getstorage_id() != null ? diskImage.getstorage_id().getValue()
                            : Guid.Empty;
                    Guid imageGroupId = diskImage.getimage_group_id() != null ? diskImage.getimage_group_id()
                            .getValue() : Guid.Empty;

                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.SetImageLegality,
                                    new SetImageLegalityVDSCommandParameters(storagePoolId, storageDomainId,
                                            imageGroupId, diskImage.getId(), false));
                    /**
                     * TODO: set image illegal in IRS
                     */
                    DiskImage snapshotFromVdc =
                            DbFacade.getInstance().getDiskImageDAO().getSnapshotById(diskImage.getId());
                    if (snapshotFromVdc != null) {
                        snapshotFromVdc.setimageStatus(ImageStatus.ILLEGAL);

                        DbFacade.getInstance().getDiskImageDAO().update(snapshotFromVdc);
                        /**
                         * Image found in Vdc Db
                         */
                        if (diskImage.getParentId().equals(Guid.Empty)) {
                            type = DesktopNotExistInVdcTreatment(diskImage.getId(), logable);
                        } else {
                            type = SnapshotNotExistInVdcTreatment(diskImage.getId(), logable);
                        }
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    type = AuditLogType.IMAGES_SYNCRONIZER_TEMPLATE_NOT_EXIST_IN_VDC;
                    /**
                     * Error message: found image snapshot not in Vdc. There is
                     * no data in Vdc.
                     */
                }
                AuditLogDirector.log(logable, type);
                if (!proceeded.containsKey(image)) {
                    proceeded.put(image, 0);
                }
            }
        }
    }

    private static void ProceedImagesNotFoundInIrs(java.util.ArrayList<Guid> images) {
        java.util.ArrayList<image_vm_map> vmImages = new java.util.ArrayList<image_vm_map>();
        for (Guid image : images) {
            image_vm_map map = DbFacade.getInstance().getImageVmMapDAO().getByImageId(image);
            if (map != null) {
                vmImages.add(map);
            }
        }
        if (vmImages.size() > 0) {
            ProceedVmsNotFoundInIrs(vmImages, images);
        }

        java.util.ArrayList<DiskImageTemplate> templateImages = new java.util.ArrayList<DiskImageTemplate>();
        for (Guid image : images) {
            DiskImageTemplate dt = DbFacade.getInstance().getDiskImageTemplateDAO().get(image);
            if (dt != null) {
                templateImages.add(dt);
            }
        }
        if (templateImages.size() > 0) {
            // Not existing template images treatment
            for (DiskImageTemplate dt : templateImages) {
                DiskImage disk = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(dt.getit_guid());
                disk.setimageStatus(ImageStatus.ILLEGAL);
                DbFacade.getInstance().getDiskImageDAO().update(disk);
                AuditLogableBase logable = new AuditLogableBase();
                logable.setVmTemplateId(dt.getvmt_guid());
                AuditLogDirector.log(logable, AuditLogType.IMAGES_SYNCRONIZER_IMAGE_TEMPLATE_NOT_EXIST);
            }
        }

        /**
         * TODO: do something with images remained.
         */
    }

    private static void ProceedVmsNotFoundInIrs(Iterable<image_vm_map> vmImages, java.util.Collection<Guid> images) {
        boolean isFound = false;
        AuditLogableBase logable = new AuditLogableBase();
        AuditLogType type = AuditLogType.UNASSIGNED;
        for (image_vm_map map : vmImages) {
            DiskImage imageFromVdc = DbFacade.getInstance().getDiskImageDAO().get(map.getimage_id());
            imageFromVdc.setimageStatus(ImageStatus.ILLEGAL);
            DbFacade.getInstance().getDiskImageDAO().update(imageFromVdc);
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVmStatus,
                            new SetVmStatusVDSCommandParameters(map.getvm_id(), VMStatus.ImageIllegal));
            logable.setVmId(map.getvm_id());
            while (!imageFromVdc.getParentId().equals(Guid.Empty) && !isFound) {
                logable.AppendCustomValue("SnapshotDescription", imageFromVdc.getdescription(), ",");
                DiskImage parentFromIrs = null;
                try {
                    // todo - omer review if this is right
                    Guid storagePoolId = imageFromVdc.getstorage_pool_id() != null ? imageFromVdc.getstorage_pool_id()
                            .getValue() : Guid.Empty;
                    Guid storageDomainId = imageFromVdc.getstorage_id() != null ? imageFromVdc.getstorage_id()
                            .getValue() : Guid.Empty;
                    Guid imageGroupId = imageFromVdc.getimage_group_id() != null ? imageFromVdc.getimage_group_id()
                            .getValue() : Guid.Empty;

                    parentFromIrs = (DiskImage) Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.GetImageInfo,
                                    new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                            imageFromVdc.getParentId())).getReturnValue();
                }
                // catch(IRSErrorImageNotExistException)
                catch (VdcBLLException e) {
                }

                imageFromVdc.setimageStatus(ImageStatus.ILLEGAL);
                DbFacade.getInstance().getDiskImageDAO().update(imageFromVdc);

                if (parentFromIrs != null) {
                    isFound = true;
                }
                imageFromVdc = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(imageFromVdc.getParentId());
                images.remove(imageFromVdc.getId());
            }
            if (isFound) {
                /**
                 * Found snapshot in Irs
                 */
                if (imageFromVdc.getParentId().equals(Guid.Empty)) {
                    /**
                     * Found template
                     */
                    type = AuditLogType.IMAGES_SYNCRONIZER_DESKTOP_NOT_EXIST_IN_IRS;
                } else {
                    /**
                     * Found vm
                     */
                    type = AuditLogType.IMAGES_SYNCRONIZER_SNAPSHOT_NOT_EXIST_IN_IRS;
                }
            } else {
                /**
                 * VM without image/Template
                 */
                type = AuditLogType.IMAGES_SYNCRONIZER_DESKTOP_WITHOUT_TEMPLATE_VDC;
            }
        }
        AuditLogDirector.log(logable, type);
    }
}
