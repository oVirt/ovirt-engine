package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.IImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;

/**
 * Base class for all image handling commands
 *
 *
 */
public abstract class BaseImagesCommand<T extends ImagesActionsParametersBase> extends StorageDomainCommandBase<T> {
    private DiskImage _destinationImage;
    private IImage mImage;
    private Guid mImageId = new Guid();
    private Guid mImageContainerId = Guid.Empty;
    /**
     * Default mapping - drive 1
     */
    private String mDrive = ImagesHandler.DefaultDriveName;

    public BaseImagesCommand(T parameters) {
        super(parameters);
        mImageId = parameters.getImageId();
        if (parameters instanceof ImagesContainterParametersBase) {
            ImagesContainterParametersBase tempVar = (ImagesContainterParametersBase) parameters;
            mImageContainerId = tempVar.getContainerId();
            mDrive = tempVar.getDrive();
            super.setVmId(mImageContainerId);
            setStoragePoolId(getDiskImage() != null && getDiskImage().getstorage_pool_id() != null ? getDiskImage()
                    .getstorage_pool_id().getValue() : Guid.Empty);
        }
    }

    protected IImage getImage() {
        if (mImage == null) {
            DiskImage image = DbFacade.getInstance().getDiskImageDAO().get(getImageId());
            if (image != null) {
                mImage = image;
            } else {
                image = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(getImageId());
                if (image != null) {
                    image.setvm_guid(getImageContainerId());
                    mImage = image;
                }
            }
        }
        return mImage;
    }

    protected Guid getImageId() {
        return mImageId;
    }

    protected Guid getImageContainerId() {
        return mImageContainerId;
    }

    protected void setImageContainerId(Guid value) {
        mImageContainerId = value;
    }

    private DiskImage _diskImage;

    protected DiskImage getDiskImage() {
        if (_diskImage == null) {
            IImage tempVar = getImage();
            _diskImage = (DiskImage) ((tempVar instanceof DiskImage) ? tempVar : null);
        }
        return _diskImage;
    }

    protected void setDiskImage(DiskImage value) {
        _diskImage = value;
    }

    protected String getDrive() {
        return mDrive;
    }

    private Guid _destinationImageId = Guid.Empty;

    protected Guid getDestinationImageId() {
        return getParameters() != null ? getParameters().getDestinationImageId() : _destinationImageId;
    }

    protected void setDestinationImageId(Guid value) {
        if (getParameters() != null) {
            getParameters().setDestinationImageId(value);
        } else {
            _destinationImageId = value;
        }
    }

    protected DiskImage getDestinationDiskImage() {
        if (_destinationImage == null) {
            DiskImage image = DbFacade.getInstance().getDiskImageDAO().get(getDestinationImageId());
            if (image != null) {
                _destinationImage = image;
            } else {
                image = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(getDestinationImageId());
                if (image != null) {
                    _destinationImage = image;
                }
            }
        }
        return _destinationImage;
    }

    private Guid _imageGroupId = Guid.Empty;

    protected Guid getImageGroupId() {
        if (_imageGroupId.equals(Guid.Empty)) {
            _imageGroupId = getDiskImage().getimage_group_id() != null ? getDiskImage().getimage_group_id().getValue()
                    : Guid.Empty;
        }
        return _imageGroupId;

    }

    protected void setImageGroupId(Guid value) {
        _imageGroupId = value;
    }

    @Override
    protected void executeCommand() {
        InitImageContainer();
        CheckImageValidity();
    }

    /**
     * Initialize identity of object, contains image(VM or VmTemplate)
     */
    protected void InitImageContainer() {
        if (mImageContainerId.equals(Guid.Empty)) {
            mImageContainerId = getImage().getcontainer_guid();
        }
    }

    /**
     * Check if image is valid snapshot of vm
     */
    protected void CheckImageValidity() {
        try {
            IImage tempVar = getImage();
            DiskImage diskImage = (DiskImage) ((tempVar instanceof DiskImage) ? tempVar : null);

            /**
             * Vitaly change. Prevent operating image with illegal status TODO:
             * insert it in new CanDoAction mechanizm
             */
            if (diskImage == null) {
                diskImage = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(getImage().getId());
            }
            Guid storagePoolId = diskImage.getstorage_pool_id() != null ? diskImage.getstorage_pool_id().getValue()
                    : Guid.Empty;
            Guid storageDomainId =
                    getStorageDomainId() != null && !getStorageDomainId().getValue().equals(Guid.Empty) ? getStorageDomainId()
                            .getValue()
                            : diskImage.getstorage_id() != null ? diskImage.getstorage_id().getValue() : Guid.Empty;
            Guid imageGroupId = diskImage.getimage_group_id() != null ? diskImage.getimage_group_id().getValue()
                    : Guid.Empty;

            DiskImage image = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    getImage().getId())).getReturnValue();

            if (image.getimageStatus() != ImageStatus.OK) {
                if (diskImage != null) {
                    diskImage.setimageStatus(image.getimageStatus());
                    DbFacade.getInstance().getDiskImageDAO().update(diskImage);
                    throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
                }
            }

            if (diskImage != null) {
                diskImage.setlastModified(image.getlast_modified_date());
            }
        } catch (RuntimeException ex) {
            if (ex instanceof VdcBLLException) {
                throw ex;
            }
            throw new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_VM_SNAPSHOT_MISSMATCH, ex);
        }
    }

    /**
     * Snapshot can be created only when there is no other images maped to same
     * drive in vm.
     *
     * @return TODO: Vitaly. Remove coupling between this and
     *         CanCreateAllSnapshotsFromVm
     */
    protected boolean CanCreateSnapshot() {
        List<DiskImage> images = DbFacade.getInstance().getDiskImageDAO().getAllForVm(getImageContainerId());
        int count = 0;
        for (DiskImage image : images) {
            if (StringHelper.EqOp(image.getinternal_drive_mapping(), getImage().getinternal_drive_mapping())) {
                count++;
                if (count > 1) {
                    log.error("Cannot create snapshot. Vm is in preview status");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns first found image in database that assigned to Image's parent Vm
     * and mapped to same drive
     *
     * @return m
     */
    protected DiskImage GetOtherImageMappedToSameDrive() {
        List<DiskImage> images = DbFacade.getInstance().getDiskImageDAO().getAllForVm(getImageContainerId());
        if (getImage() != null) {
            for (DiskImage image : images) {
                if (StringHelper.EqOp(image.getinternal_drive_mapping(), getImage().getinternal_drive_mapping())
                        && !getImage().getId().equals(image.getId())) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * Creates a copy of the source disk image ('DiskImage').
     *
     * @param newImageGuid
     *            the image id of the cloned disk image.
     * @return the cloned disk image. Note that the cloned image's status is
     *         'Locked'.
     */
    protected DiskImage CloneDiskImage(Guid newImageGuid) {
        DiskImage retDiskImage = DiskImage.copyOf(getDiskImage());

        retDiskImage.setId(newImageGuid);
        retDiskImage.setdescription(CalculateImageDescription());
        retDiskImage.setParentId(getDiskImage().getId());
        retDiskImage.setvm_snapshot_id(getParameters().getVmSnapshotId());
        retDiskImage.setvm_guid(getImageContainerId());
        retDiskImage.setimage_group_id(getImageGroupId());
        retDiskImage.setlast_modified_date(getNow());
        return retDiskImage;
    }

    /**
     * Overrides the relevant fields of the destination disk image
     * ('DestinationDiskImage') with some values of the IRS disk image.
     *
     * @param fromIRS
     *            the IRS disk image.
     */
    protected void CompleteImageData(DiskImage fromIRS) {
        getDestinationDiskImage().setcreation_date(fromIRS.getcreation_date());
        getDestinationDiskImage().setlast_modified_date(fromIRS.getlast_modified_date());
        getDestinationDiskImage().setlastModified(getDestinationDiskImage().getlast_modified_date());
        DiskImageDynamic destinationDiskDynamic = DbFacade.getInstance().getDiskImageDynamicDAO().get(
                getDestinationDiskImage().getId());
        if (destinationDiskDynamic != null) {
            destinationDiskDynamic.setactual_size(fromIRS.getactual_size());
            DbFacade.getInstance().getDiskImageDynamicDAO().update(destinationDiskDynamic);
        }
        // DestinationDiskImage.description = CalculateImageDescription();
    }

    /**
     * Building the label name for volume.
     *
     * @return - Calculated label name.
     */
    protected String CalculateImageDescription() {
        VM vm = DbFacade.getInstance().getVmDAO().getById(getImageContainerId());

        // If vm is null (could be because the getImageContainerId() is a Blank
        // template) , use the vm id.
        if (vm == null) {
            vm = DbFacade.getInstance().getVmDAO().getById(getVmId());
        }

        /**
         * Vitaly: added description per QA request
         */
        StringBuilder vmLabel = new StringBuilder("ActiveImage");
        vmLabel = (vm == null) ? vmLabel : vmLabel.append("_").append(vm.getvm_name());
        return String.format("_%1$s_%2$s", vmLabel, new java.util.Date());
    }

    protected static void CompleteAdvancedDiskData(DiskImage from, DiskImage to) {
        to.setboot(from.getboot());
        to.setdisk_interface(from.getdisk_interface());
        to.setpropagate_errors(from.getpropagate_errors());
        to.setwipe_after_delete(from.getwipe_after_delete());
    }

    protected void AddDiskImageToDb(DiskImage image) {
        // TODO handle creation date & the difference between direct parent &
        // image template
        // TODO - transaction
        // Adding new disk to the image table in the DB
        try {
            DbFacade.getInstance().getDiskImageDAO().save(image);
            DbFacade.getInstance().getImageVmMapDAO().save(
                    new image_vm_map(image.getactive(), image.getId(), image.getvm_guid()));
            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(image.getId());
            diskDynamic.setactual_size(image.getactual_size());
            DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
            saveDiskIfNotExists(image);
        } catch (RuntimeException ex) {
            log.error("AddDiskImageToDB::Failed adding new created snapshot into the db", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
        }

    }

    /**
     * Save the disk from the given image info, only if the disk doesn't exist already.
     *
     * @param image
     *            The image to take the disk's details from.
     */
    protected void saveDiskIfNotExists(DiskImage image) {
        if (!getDiskDao().exists(image.getimage_group_id())) {
            getDiskDao().save(image.getDisk());
        }
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected void LockImage() {
        SetImageStatus(getDiskImage(), ImageStatus.LOCKED);
    }

    protected void UnLockImage() {
        SetImageStatus(getDiskImage(), ImageStatus.OK);
    }

    protected void MarkImageAsIllegal() {
        SetImageStatus(getDiskImage(), ImageStatus.ILLEGAL);
    }

    protected static void SetImageStatus(DiskImage diskImage, ImageStatus imageStatus) {
        if (diskImage != null) {
            diskImage.setimageStatus(imageStatus);
            DbFacade.getInstance().getDiskImageDAO().update(diskImage);
        }
    }

    @Override
    protected void EndSuccessfully() {
        if (getDestinationDiskImage() != null) {
            Guid storagePoolId = getDestinationDiskImage().getstorage_pool_id() != null ? getDestinationDiskImage()
                    .getstorage_pool_id().getValue() : Guid.Empty;

            Guid newImageGroupId = getDestinationDiskImage().getimage_group_id() != null ? getDestinationDiskImage()
                    .getimage_group_id().getValue() : Guid.Empty;
            Guid newImageId = getDestinationDiskImage().getId();
            Guid newStorageDomainID = getDestinationDiskImage().getstorage_id() != null ? getDestinationDiskImage()
                    .getstorage_id().getValue() : Guid.Empty;

            // complete IRS data to DB disk image:
            DiskImage newImageIRS = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, newStorageDomainID, newImageGroupId,
                                    newImageId)).getReturnValue();

            if (newImageIRS != null) {
                CompleteImageData(newImageIRS);
            }

            // Unlock destination image:
            getDestinationDiskImage().setimageStatus(ImageStatus.OK);
            DbFacade.getInstance().getDiskImageDAO().update(getDestinationDiskImage());
        }

        if (getDiskImage() != null) {
            // Unlock source image:
            UnLockImage();
        }

        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        UndoActionOnSourceAndDestination();

        setSucceeded(true);
    }

    protected void UndoActionOnSourceAndDestination() {
        if (getDestinationDiskImage() != null) {
            RemoveSnapshotFromDB(getDestinationDiskImage());
        }

        if (getDiskImage() != null) {
            // Unlock source image:
            UnLockImage();
        }
    }

    /**
     * Vitaly TODO: move it other class in hierarch
     */

    protected void RemoveSnapshot(DiskImage snapshot) {
        RemoveSnapshotFromDB(snapshot);
        AdditionalImageRemoveTreatment(snapshot);
    }

    protected void AdditionalImageRemoveTreatment(DiskImage snapshot) {
    }

    protected void RemoveSnapshotFromDB(DiskImage snapshot) {
        DbFacade.getInstance().getDiskImageDAO().remove(snapshot.getId());
        List<DiskImage> imagesForDisk =
                DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForImageGroup(snapshot.getimage_group_id());
        if (imagesForDisk == null || imagesForDisk.isEmpty()) {
            getDiskDao().remove(snapshot.getimage_group_id());
        }
    }

    public static void GetImageChildren(Guid snapshot, RefObject<java.util.ArrayList<Guid>> children) {
        java.util.ArrayList<Guid> list = new java.util.ArrayList<Guid>();
        for (DiskImage image : DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForParent(snapshot)) {
            list.add(image.getId());
        }
        children.argvalue.addAll(list);
        for (Guid snapshotId : list) {
            GetImageChildren(snapshotId, children);
        }
    }

    protected void RemoveChildren(Guid snapshot) {
        java.util.ArrayList<Guid> children = new java.util.ArrayList<Guid>();
        RefObject<java.util.ArrayList<Guid>> tempRefObject = new RefObject<java.util.ArrayList<Guid>>(children);
        GetImageChildren(snapshot, tempRefObject);
        children = tempRefObject.argvalue;
        // children.Reverse();
        Collections.reverse(children);
        for (Guid child : children) {
            RemoveSnapshot(DbFacade.getInstance().getDiskImageDAO().getSnapshotById(child));
        }
    }
}
