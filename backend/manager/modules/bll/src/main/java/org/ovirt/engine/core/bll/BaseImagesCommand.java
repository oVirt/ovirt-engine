package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageDao;

/**
 * Base class for all image handling commands
 */
public abstract class BaseImagesCommand<T extends ImagesActionsParametersBase> extends StorageDomainCommandBase<T> {
    private DiskImage _destinationImage;
    private DiskImage mImage;
    private Guid mImageId = new Guid();
    private Guid mImageContainerId = Guid.Empty;
    VM vm;
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

    protected DiskImage getImage() {
        if (mImage == null) {
            DiskImage image = getDiskImageDao().get(getImageId());
            if (image != null) {
                mImage = image;
            } else {
                image = getDiskImageDao().getSnapshotById(getImageId());
                if (image != null) {
                    image.setvm_guid(getImageContainerId());
                    mImage = image;
                }
            }
        }
        return mImage;
    }

    protected ImageDao getImageDao() {
        return DbFacade.getInstance().getImageDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    protected void setImage(DiskImage image) {
        mImage = image;
    }

    protected Guid getImageId() {
        return mImageId;
    }

    protected void setImageId(Guid imageId) {
        this.mImageId = imageId;
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
            _diskImage = getImage();
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

    protected VDSReturnValue performImageVdsmOperation() {
        throw new NotImplementedException();
    }

    protected DiskImage getDestinationDiskImage() {
        if (_destinationImage == null) {
            _destinationImage = getDiskImageDao().get(getDestinationImageId());
            if (_destinationImage == null) {
                _destinationImage = getDiskImageDao().getSnapshotById(getDestinationImageId());
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
            DiskImage diskImage = getImage();

            /**
             * Prevent operating image with illegal status TODO: insert it in new CanDoAction mechanism
             */
            if (diskImage == null) {
                diskImage = getDiskImageDao().getSnapshotById(getImage().getImageId());
            }

            Guid storagePoolId = diskImage.getstorage_pool_id() != null ? diskImage.getstorage_pool_id().getValue()
                    : Guid.Empty;
            Guid storageDomainId =
                    getStorageDomainId() != null && !getStorageDomainId().getValue().equals(Guid.Empty) ? getStorageDomainId()
                            .getValue()
                            : diskImage.getstorage_ids() != null && diskImage.getstorage_ids().size() > 0 ? diskImage.getstorage_ids()
                                    .get(0)
                                    : Guid.Empty;
            Guid imageGroupId = diskImage.getimage_group_id() != null ? diskImage.getimage_group_id().getValue()
                    : Guid.Empty;

            DiskImage image = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    getImage().getImageId())).getReturnValue();

            if (image.getimageStatus() != ImageStatus.OK) {
                if (diskImage != null) {
                    diskImage.setimageStatus(image.getimageStatus());
                    getImageDao().update(diskImage.getImage());
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
     * Snapshot can be created only when there is no other images mapped to same drive in vm.
     *
     * @return true if snapshot can be created
     */
    // TODO: Should be moved to another class in the hierarchy
    protected boolean CanCreateSnapshot() {
        if (ImagesHandler.isVmInPreview(getImageContainerId())) {
            log.error("Cannot create snapshot. Vm is in preview status");
            return false;
        }
        return true;
    }

    /**
     * Find the image for the same drive by the snapshot type:<br>
     * The image is the image from the snapshot of the given type, which represents the same drive.
     * @param snapshotType
     *            The snapshot type for which the other image should exist.
     *
     * @return The ID of the image for the same drive, or null if none found.
     */
    protected Guid findImageForSameDrive(SnapshotType snapshotType) {
        return findImageForSameDrive(
                DbFacade.getInstance().getSnapshotDao().getId(getImageContainerId(), snapshotType));
    }

    /**
     * Find the image for the same drive by the snapshot ID:<br>
     * The image is the image from the given snapshot, which represents the same drive.
     *
     * @param snapshotId
     *            The snapshot ID for which the other image should exist.
     *
     * @return The ID of the image for the same drive, or null if none found.
     */
    protected Guid findImageForSameDrive(Guid snapshotId) {
        List<DiskImage> imagesFromSanpshot =
                DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForVmSnapshot(snapshotId);
        for (DiskImage diskImage : imagesFromSanpshot) {
            if (getDiskImage().getimage_group_id().equals(diskImage.getimage_group_id())) {
                return diskImage.getImageId();
            }
        }

        return null;
    }

    /**
     * Creates a copy of the source disk image ('DiskImage').
     * @param newImageGuid
     *            the image id of the cloned disk image.
     * @return the cloned disk image. Note that the cloned image's status is 'Locked'.
     */
    protected DiskImage CloneDiskImage(Guid newImageGuid) {
        return cloneDiskImage(newImageGuid, getDiskImage());
    }

    /**
     * Creates a copy of the source disk image
     *
     * @param newImageGuid
     *            the image id of the cloned disk image.
     * @param srcDiskImage
     *            the disk image to copy from
     * @return the cloned disk image. Note that the cloned image's status is 'Locked'.
     */
    protected DiskImage cloneDiskImage(Guid newImageGuid, DiskImage srcDiskImage) {
        DiskImage retDiskImage = DiskImage.copyOf(srcDiskImage);
        retDiskImage.setImageId(newImageGuid);
        retDiskImage.setParentId(getDiskImage().getImageId());
        retDiskImage.setvm_snapshot_id(getParameters().getVmSnapshotId());
        retDiskImage.setvm_guid(getImageContainerId());
        retDiskImage.setimage_group_id(getImageGroupId());
        retDiskImage.setlast_modified_date(new Date());
        retDiskImage.setQuotaId(getParameters().getQuotaId());
        return retDiskImage;
    }

    /**
     * Creates a copy of the source disk image and updates it with fields from diskImageBase object. Currently we're
     * interested only at volume type and format
     *
     * @param newImageGuid
     *            the image id of the cloned disk image.
     * @param srcDiskImage
     *            the source to copy from
     * @param diskImageBase
     *            the disk image base to update fields from
     * @return the cloned disk image. Note that the cloned image's status is 'Locked'.
     */
    protected DiskImage cloneImageAndUpdateFromDiskImageBase(Guid newImageGuid,
            DiskImage srcDiskImage,
            DiskImageBase diskImageBase) {
        DiskImage retDiskImage = cloneDiskImage(newImageGuid, srcDiskImage);
        if (diskImageBase != null) {
            retDiskImage.setvolume_type(diskImageBase.getvolume_type());
            retDiskImage.setvolume_format(diskImageBase.getvolume_format());
        }
        return retDiskImage;
    }

    /**
     * Creates a copy of the source disk image and updates it with fields from diskImageBase object. Currently we're
     * interested only at volume type and format
     *
     * @param newImageGuid
     *            the image id of the cloned disk image.
     * @param srcDiskImage
     *            the source to copy from
     * @param diskImageBase
     *            the disk image base to update fields from
     * @return the cloned disk image. Note that the cloned image's status is 'Locked'.
     */
    protected DiskImage cloneImageAndUpdateFromDiskImageBase(Guid newImageGuid, DiskImageBase diskImageBase) {
        return cloneImageAndUpdateFromDiskImageBase(newImageGuid, getDiskImage(), diskImageBase);
    }

    /**
     * Overrides the relevant fields of the destination disk image ('DestinationDiskImage') with some values of the IRS
     * disk image.
     * @param fromIRS
     *            the IRS disk image.
     */
    protected void CompleteImageData(DiskImage fromIRS) {
        getDestinationDiskImage().setcreation_date(fromIRS.getcreation_date());
        getDestinationDiskImage().setlast_modified_date(fromIRS.getlast_modified_date());
        getDestinationDiskImage().setlastModified(getDestinationDiskImage().getlast_modified_date());
        DiskImageDynamic destinationDiskDynamic = DbFacade.getInstance().getDiskImageDynamicDAO().get(
                getDestinationDiskImage().getImageId());
        if (destinationDiskDynamic != null) {
            destinationDiskDynamic.setactual_size(fromIRS.getactual_size());
            DbFacade.getInstance().getDiskImageDynamicDAO().update(destinationDiskDynamic);
        }
        // DestinationDiskImage.description = CalculateImageDescription();
    }

    protected static void CompleteAdvancedDiskData(DiskImage from, DiskImage to) {
        to.setboot(from.getboot());
        to.setDiskInterface(from.getDiskInterface());
        to.setPropagateErrors(from.getPropagateErrors());
        to.setWipeAfterDelete(from.isWipeAfterDelete());
    }

    protected void AddDiskImageToDb(DiskImage image) {
        // TODO handle creation date & the difference between direct parent &
        // image template
        // TODO - transaction
        // Adding new disk to the image table in the DB
        try {
            image.setactive(true);
            getImageDao().save(image.getImage());
            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(image.getImageId());
            diskDynamic.setactual_size(image.getactual_size());
            DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
            DbFacade.getInstance()
                    .getImageStorageDomainMapDao()
                    .save(new image_storage_domain_map(image.getImageId(),
                            image.getstorage_ids().get(0)));
            saveDiskIfNotExists(image);
        } catch (RuntimeException ex) {
            log.error("AddDiskImageToDB::Failed adding new created snapshot into the db", ex);
            throw new VdcBLLException(VdcBllErrors.DB, ex);
        }

    }

    /**
     * Save the disk from the given image info, only if the disk doesn't exist already.
     * @param image
     *            The image to take the disk's details from.
     */
    protected void saveDiskIfNotExists(DiskImage image) {
        if (!getBaseDiskDao().exists(image.getimage_group_id())) {
            getBaseDiskDao().save(image);
        }
    }

    protected BaseDiskDao getBaseDiskDao() {
        return DbFacade.getInstance().getBaseDiskDao();
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
            DbFacade.getInstance().getImageDao().update(diskImage.getImage());
        }
    }

    @Override
    protected void EndSuccessfully() {
        if (getDestinationDiskImage() != null) {
            Guid storagePoolId = getDestinationDiskImage().getstorage_pool_id() != null ? getDestinationDiskImage()
                    .getstorage_pool_id().getValue() : Guid.Empty;

            Guid newImageGroupId = getDestinationDiskImage().getimage_group_id() != null ? getDestinationDiskImage()
                    .getimage_group_id().getValue() : Guid.Empty;
            Guid newImageId = getDestinationDiskImage().getImageId();
            Guid newStorageDomainID = getDestinationDiskImage().getstorage_ids().get(0);

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
            getImageDao().update(getDestinationDiskImage().getImage());
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
     * Vitaly TODO: move it other class in hierarchy
     */

    protected void RemoveSnapshot(DiskImage snapshot) {
        RemoveSnapshotFromDB(snapshot);
        AdditionalImageRemoveTreatment(snapshot);
    }

    protected void AdditionalImageRemoveTreatment(DiskImage snapshot) {
    }

    protected void RemoveSnapshotFromDB(DiskImage snapshot) {
        DbFacade.getInstance().getImageStorageDomainMapDao().remove(snapshot.getImageId());
        getImageDao().remove(snapshot.getImageId());
        List<DiskImage> imagesForDisk =
                getDiskImageDao().getAllSnapshotsForImageGroup(snapshot.getimage_group_id());
        if (imagesForDisk == null || imagesForDisk.isEmpty()) {
            getBaseDiskDao().remove(snapshot.getimage_group_id());
        }
    }

    public static void GetImageChildren(Guid snapshot, RefObject<java.util.ArrayList<Guid>> children) {
        java.util.ArrayList<Guid> list = new java.util.ArrayList<Guid>();
        for (DiskImage image : DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForParent(snapshot)) {
            list.add(image.getImageId());
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
            RemoveSnapshot(getDiskImageDao().getSnapshotById(child));
        }
    }

    /**
     * The following method unify saving of image, it will be also saved with its storage
     * mapping.
     * @param diskImage
     */
    static public void saveImage(DiskImage diskImage) {
        DbFacade.getInstance().getImageDao().save(diskImage.getImage());
        DbFacade.getInstance()
                .getImageStorageDomainMapDao()
                .save(new image_storage_domain_map(diskImage.getImageId(),
                        diskImage.getstorage_ids()
                                .get(0)));
    }
}
