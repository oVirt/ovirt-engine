package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class ImagesHandler {
    public static final Guid BlankImageTemplateId = new Guid("00000000-0000-0000-0000-000000000000");
    public static final String DefaultDriveName = "1";

    /**
     * The following method will find all images and storages where they located for provide template and will fill an
     * imageToDestinationDomainMap by imageId mapping on active storage id where image is located. The second map is
     * mapping of founded storage ids to storage object
     * @param template
     * @param imageToDestinationDomainMap
     * @param destStorages
     */
    public static void fillImagesMapBasedOnTemplate(VmTemplate template,
            Map<Guid, Guid> imageToDestinationDomainMap,
            Map<Guid, storage_domains> destStorages) {
        List<storage_domains> domains =
                DbFacade.getInstance()
                        .getStorageDomainDAO()
                        .getAllForStoragePool(template.getstorage_pool_id().getValue());
        Map<Guid, storage_domains> storageDomainsMap = new HashMap<Guid, storage_domains>();
        for (storage_domains storageDomain : domains) {
            StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
            ArrayList<String> messages = new ArrayList<String>();
            if (validator.isDomainExistAndActive(messages) && validator.domainIsValidDestination(messages)
                    && StorageDomainSpaceChecker.isBelowThresholds(storageDomain)) {
                storageDomainsMap.put(storageDomain.getId(), storageDomain);
            }
        }
        for (DiskImage image : template.getDiskMap().values()) {
            for (Guid storageId : image.getstorage_ids()) {
                if (storageDomainsMap.containsKey(storageId)) {
                    imageToDestinationDomainMap.put(image.getId(), storageId);
                    break;
                }
            }
        }
        for (Guid storageDomainId : new HashSet<Guid>(imageToDestinationDomainMap.values())) {
            destStorages.put(storageDomainId, storageDomainsMap.get(storageDomainId));
        }
    }

    public static Map<Guid, List<DiskImage>> buildStorageToDiskMap(Collection<DiskImage> images,
            Map<Guid, Guid> imageToDestinationDomainMap) {
        Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage disk : images) {
            Guid storageDomainId = imageToDestinationDomainMap.get(disk.getId());
            List<DiskImage> diskList = storageToDisksMap.get(storageDomainId);
            if (diskList == null) {
                diskList = new ArrayList<DiskImage>();
                storageToDisksMap.put(storageDomainId, diskList);
            }
            diskList.add(disk);
        }
        return storageToDisksMap;
    }

    /**
     * Adds a disk image (Adds image, disk and relevant entities)
     *
     * @param image
     *            DiskImage to add
     * @param active
     *            true if the image should be added as active
     * @param imageStorageDomainMap
     *            storage domain map entry to map between the image and its storage domain
     */
    public static void addDiskImage(DiskImage image, boolean active, image_storage_domain_map imageStorageDomainMap) {
        try {
            addImage(image, active, imageStorageDomainMap);
            addDiskToVmIfNotExists(image.getDisk(), image.getvm_guid());
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
        }
    }

    /**
     * Gets a map of DiskImage IDs to DiskImage objects
     *
     * @param diskImages
     *            collection of DiskImage objects to create the map for
     * @return map object is the collection is not null
     */
    public static Map<Guid, DiskImage> getDiskImagesByIdMap(Collection<DiskImage> diskImages) {
        Map<Guid, DiskImage> result = null;
        if (diskImages != null) {
            result = new HashMap<Guid, DiskImage>();
            for (DiskImage diskImage : diskImages) {
                result.put(diskImage.getId(), diskImage);
            }
        }
        return result;
    }

    /**
     * Adds a disk image (Adds image, disk, and relevant entities , but not VmDevice) This may be useful for Clone VMs,
     * where besides adding images it is required to copy all vm devices (VmDeviceUtils.copyVmDevices) from the source
     * VM
     *
     * @param image
     *            image to add
     * @param active
     *            true if to add as active image
     * @param imageStorageDomainMap
     *            entry of image storagte domain map
     */
    public static void addDiskImageWithNoVmDevice(DiskImage image,
            boolean active,
            image_storage_domain_map imageStorageDomainMap) {
        try {
            addImage(image, active, imageStorageDomainMap);
            addDisk(image.getDisk());
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
        }
    }

    /**
     * Adds a disk image (Adds image, disk, and relevant entities , but not VmDevice) This may be useful for Clone VMs,
     * where besides adding images it is required to copy all vm devices (VmDeviceUtils.copyVmDevices) from the source
     * VM
     *
     * @param image
     * @param active
     * @param imageStorageDomainMap
     */
    public static void addDiskImageWithNoVmDevice(DiskImage image) {
        addDiskImageWithNoVmDevice(image,
                image.getactive(),
                new image_storage_domain_map(image.getId(), image.getstorage_ids().get(0)));
    }

    /**
     * Adds disk to a VM without creating a VmDevice entry
     *
     * @param disk
     *            disk to add
     * @param vmId
     *            ID of the VM the disk will be associated with
     */
    public static void addDisk(Disk disk) {
        if (!DbFacade.getInstance().getDiskDao().exists(disk.getId())) {
            DbFacade.getInstance().getDiskDao().save(disk);
        }
    }

    /**
     * Adds a disk image (Adds image with active flag according to the value in image, using the first storage domain in
     * the storage id as entry to the storage domain map)
     *
     * @param image
     *            DiskImage to add
     */
    public static void addDiskImage(DiskImage image) {
        addDiskImage(image, image.getactive(), new image_storage_domain_map(image.getId(), image.getstorage_ids()
                .get(0)));
    }

    /**
     * Add image and related entities to DB (Adds image, disk image dynamic and image storage domain map)
     *
     * @param image
     *            the image to add
     * @param active
     *            if true the image will be active
     * @param imageStorageDomainMap
     *            entry of mapping between the storage domain and the image
     */
    public static void addImage(DiskImage image, boolean active, image_storage_domain_map imageStorageDomainMap) {
        image.setactive(active);
        DbFacade.getInstance().getDiskImageDAO().save(image);
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(image.getId());
        diskDynamic.setactual_size(image.getactual_size());
        DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
        if (imageStorageDomainMap != null) {
            DbFacade.getInstance()
                    .getStorageDomainDAO()
                    .addImageStorageDomainMap(imageStorageDomainMap);
        }
    }

    /**
     * Add disk if it does not exist to a given vm
     *
     * @param disk
     *            the disk to add
     * @param vmId
     *            the ID of the vm to add to if the disk does not exist for this VM
     */
    public static void addDiskToVmIfNotExists(Disk disk, Guid vmId) {
        if (!DbFacade.getInstance().getDiskDao().exists(disk.getId())) {
            addDiskToVm(disk, vmId);
        }
    }

    /**
     * Adds disk to vm
     *
     * @param disk
     *            the disk to add
     * @param vmId
     *            the ID of the VM to add to
     */
    public static void addDiskToVm(Disk disk, Guid vmId) {
        DbFacade.getInstance().getDiskDao().save(disk);
        VmDeviceUtils.addManagedDevice(new VmDeviceId(disk.getId(), vmId),
                VmDeviceType.DISK, VmDeviceType.DISK, "", true, false);
    }

    /**
     * This function was developed especially for GUI needs. It returns a list of all the snapshots of current image of
     * a specific VM. If there are two images mapped to same VM, it's assumed that this is a TryBackToImage case and the
     * function returns a list of snapshots of inactive images. In this case the parent of the active image appears to
     * be trybackfrom image
     *
     * @param imageId
     * @param imageTemplateId
     * @return
     */
    public static ArrayList<DiskImage> getAllImageSnapshots(Guid imageId, Guid imageTemplateId) {
        ArrayList<DiskImage> snapshots = new ArrayList<DiskImage>();
        Guid curImage = imageId;
        while (!imageTemplateId.equals(curImage) && !curImage.equals(Guid.Empty)) {
            DiskImage curDiskImage = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(curImage);
            snapshots.add(curDiskImage);
            curImage = curDiskImage.getParentId();
        }
        return snapshots;
    }

    public static String cdPathWindowsToLinux(String windowsPath, Guid storagePoolId) {
        if (StringHelper.isNullOrEmpty(windowsPath)) {
            return windowsPath; // empty string is used for 'eject'.
        }
        String fileName = Path.GetFileName(windowsPath);
        String isoPrefix = (String) Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.IsoPrefix, new IrsBaseVDSCommandParameters(storagePoolId))
                .getReturnValue();
        return String.format("%1$s/%2$s", isoPrefix, fileName);
    }

    public static boolean isImagesExists(Iterable<DiskImage> images, Guid storagePoolId, Guid storageDomainId) {
        return isImagesExists(images, storagePoolId, storageDomainId, new RefObject<ArrayList<DiskImage>>());
    }

    private static boolean isImagesExists(Iterable<DiskImage> images, Guid storagePoolId, Guid domainId,
            RefObject<ArrayList<DiskImage>> irsImages) {
        irsImages.argvalue = new ArrayList<DiskImage>();
        boolean returnValue = true;

        for (DiskImage image : images) {
            Guid storageDomainId = !Guid.Empty.equals(domainId) ? domainId : image.getstorage_ids().get(0);
            DiskImage fromIrs = isImageExist(storagePoolId, storageDomainId, image);
            if (fromIrs == null) {
                returnValue = false;
                break;
            } else {
                irsImages.argvalue.add(fromIrs);
            }
        }
        return returnValue;
    }

    private static DiskImage isImageExist(Guid storagePoolId,
            Guid domainId,
            DiskImage image) {
        DiskImage fromIrs = null;
        try {
            Guid storageDomainId =
                    image.getstorage_ids() != null && !image.getstorage_ids().isEmpty() ? image.getstorage_ids()
                            .get(0) : domainId;
            Guid imageGroupId = image.getimage_group_id() != null ? image.getimage_group_id().getValue()
                    : Guid.Empty;
            fromIrs = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    image.getId())).getReturnValue();
        } catch (Exception e) {
        }
        return fromIrs;
    }

    public static boolean isVmInPreview(Guid vmId) {
        return DbFacade.getInstance().getSnapshotDao().exists(vmId, SnapshotStatus.IN_PREVIEW);
    }

    public static boolean CheckImageConfiguration(storage_domain_static storageDomain,
            DiskImageBase diskInfo, List<String> messages) {
        boolean result = true;
        if ((diskInfo.getvolume_type() == VolumeType.Preallocated && diskInfo.getvolume_format() == VolumeFormat.COW)
                || ((storageDomain.getstorage_type() == StorageType.FCP || storageDomain.getstorage_type() == StorageType.ISCSI) && (diskInfo
                        .getvolume_type() == VolumeType.Sparse && diskInfo.getvolume_format() == VolumeFormat.RAW))
                || (diskInfo.getvolume_format() == VolumeFormat.Unassigned || diskInfo.getvolume_type() == VolumeType.Unassigned)) {
            // not supported
            result = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED.toString());
        }
        return result;
    }

    public static boolean CheckImagesConfiguration(Guid storageDomainId,
            Collection<? extends DiskImageBase> disksConfigList,
            List<String> messages) {
        boolean result = true;
        storage_domain_static storageDomain = DbFacade.getInstance().getStorageDomainStaticDAO().get(storageDomainId);
        for (DiskImageBase diskInfo : disksConfigList) {
            result = CheckImageConfiguration(storageDomain, diskInfo, messages);
            if (!result)
                break;
        }
        return result;
    }

    public static boolean PerformImagesChecks(VM vm,
            List<String> messages,
            Guid storagePoolId,
            Guid storageDomainId,
            boolean diskSpaceCheck,
            boolean checkImagesLocked,
            boolean checkImagesIllegal,
            boolean checkImagesExist,
            boolean checkVmInPreview,
            boolean checkVmIsDown,
            boolean checkStorageDomain,
            boolean checkIsValid, List<DiskImage> diskImageList) {

        boolean returnValue = true;
        boolean isValid = checkIsValid
                && ((Boolean) Backend.getInstance().getResourceManager()
                        .RunVdsCommand(VDSCommandType.IsValid, new IrsBaseVDSCommandParameters(storagePoolId))
                        .getReturnValue()).booleanValue();
        if (checkIsValid && !isValid) {
            returnValue = false;
            if (messages != null) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND.toString());
            }
        }

        if (returnValue && checkImagesLocked) {
            if (vm.getstatus() == VMStatus.ImageLocked) {
                returnValue = false;
            } else if (diskImageList != null) {
                for (DiskImage diskImage : diskImageList) {
                    if (diskImage.getimageStatus() == ImageStatus.LOCKED) {
                        returnValue = false;
                        break;
                    }
                }
            }
            if (!returnValue && messages != null) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_LOCKED.toString());
            }
        } else if (returnValue && checkVmIsDown && vm.getstatus() != VMStatus.Down) {
            returnValue = false;
            if (messages != null) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN.toString());
            }
        } else if (returnValue && checkVmInPreview && isVmInPreview(vm.getId())) {
            returnValue = false;
            if (messages != null) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW.toString());
            }
        } else if (returnValue && isValid) {
            List<DiskImage> images;
            if (diskImageList == null) {
                images = DbFacade.getInstance().getDiskImageDAO().getAllForVm(vm.getId());
            } else {
                images = diskImageList;
            }
            if (images.size() > 0) {
                returnValue = returnValue &&
                        checkDiskImages(messages,
                                storagePoolId,
                                storageDomainId,
                                diskSpaceCheck,
                                checkImagesIllegal,
                                checkImagesExist,
                                checkStorageDomain,
                                vm,
                                images);
            } else if (checkImagesExist) {
                returnValue = false;
                if (messages != null) {
                    messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS.toString());
                }
            }
        }
        return returnValue;
    }

    private static boolean checkDiskImages(List<String> messages,
            Guid storagePoolId,
            Guid storageDomainId,
            boolean diskSpaceCheck,
            boolean checkImagesIllegal,
            boolean checkImagesExist,
            boolean checkStorageDomain,
            VM vm,
            List<DiskImage> images) {
        boolean returnValue = true;
        ArrayList<DiskImage> irsImages = null;

        if (checkImagesExist) {
            RefObject<ArrayList<DiskImage>> tempRefObject =
                    new RefObject<ArrayList<DiskImage>>();
            boolean isImagesExist = isImagesExists(images, storagePoolId, storageDomainId, tempRefObject);
            irsImages = tempRefObject.argvalue;
            if (!isImagesExist) {
                returnValue = false;
                if (messages != null) {
                    messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST.toString());
                }
            }
        }
        if (returnValue && checkImagesIllegal) {
            returnValue = CheckImagesLegality(messages, images, vm, irsImages);
        }
        if (returnValue && (diskSpaceCheck || checkStorageDomain)) {
            Set<Guid> domainsIds = new HashSet<Guid>();
            if (!Guid.Empty.equals(storageDomainId)) {
                domainsIds.add(storageDomainId);
            } else {
                for (DiskImage image : images) {
                    domainsIds.add(image.getstorage_ids().get(0));
                }
            }
            for (Guid domainId : domainsIds) {
                storage_domains domain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                        domainId, storagePoolId);
                if (checkStorageDomain) {
                    StorageDomainValidator storageDomainValidator =
                                new StorageDomainValidator(domain);
                    returnValue = storageDomainValidator.isDomainExistAndActive(messages);
                }
                if (diskSpaceCheck && returnValue && !StorageDomainSpaceChecker.isBelowThresholds(domain)) {
                    returnValue = false;
                    if (messages != null) {
                        messages.add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString());
                    }
                    break;
                }
            }
        }
        return returnValue;
    }

    private static boolean CheckImagesLegality(List<String> messages,
            List<DiskImage> images, VM vm, List<DiskImage> irsImages) {
        boolean returnValue = true;
        if (vm.getstatus() == VMStatus.ImageIllegal) {
            returnValue = false;
            if (messages != null) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL.toString());
            }
        } else {
            int i = 0;
            for (DiskImage diskImage : images) {
                if (diskImage != null) {
                    DiskImage image = irsImages.get(i++);
                    if (image.getimageStatus() != ImageStatus.OK) {
                        diskImage.setimageStatus(image.getimageStatus());
                        DbFacade.getInstance().getDiskImageDAO().update(diskImage);
                        returnValue = false;
                        if (messages != null) {
                            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL.toString());
                        }

                        break;
                    }
                }
            }
        }
        return returnValue;
    }

    public static String calculateImageDescription(VM vm) {
        String vmName = (vm == null) ? null : vm.getvm_name();
        return calculateImageDescription(vmName);
    }

    public static String calculateImageDescription(String vmName) {
        StringBuilder vmLabel = new StringBuilder("ActiveImage");
        vmLabel = (vmName == null) ? vmLabel : vmLabel.append("_").append(vmName);
        return String.format("_%1$s_%2$s", vmLabel, new java.util.Date());
    }

    public static void removeDiskImage(DiskImage diskImage) {
        try {
            removeDiskFromVm(diskImage.getvm_guid(), diskImage.getDisk().getId());
            removeImage(diskImage);
        } catch (RuntimeException ex) {
            log.error("Failed adding new disk image and related entities to db", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
        }
    }

    public static void removeImage(DiskImage diskImage) {
        DbFacade.getInstance()
                .getStorageDomainDAO()
                .removeImageStorageDomainMap(diskImage.getId());
        DbFacade.getInstance().getDiskImageDynamicDAO().remove(diskImage.getId());
        DbFacade.getInstance().getDiskImageDAO().remove(diskImage.getId());
    }

    public static void removeDiskFromVm(Guid vmGuid, Guid diskId) {
        DbFacade.getInstance().getVmDeviceDAO().remove(new VmDeviceId(diskId, vmGuid));
        DbFacade.getInstance().getDiskDao().remove(diskId);
    }

    protected static Log log = LogFactory.getLog(ImagesHandler.class);

}
