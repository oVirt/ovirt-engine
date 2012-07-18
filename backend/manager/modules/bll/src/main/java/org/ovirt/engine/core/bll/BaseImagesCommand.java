package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageDao;

/**
 * Base class for all image handling commands
 */
@SuppressWarnings("serial")
public abstract class BaseImagesCommand<T extends ImagesActionsParametersBase> extends StorageDomainCommandBase<T> {
    private DiskImage _destinationImage;
    private DiskImage mImage;
    private Guid mImageId = Guid.Empty;
    VM vm;

    public BaseImagesCommand(T parameters) {
        super(parameters);
        mImageId = parameters.getImageId();
        if (parameters instanceof ImagesContainterParametersBase) {
            ImagesContainterParametersBase tempVar = (ImagesContainterParametersBase) parameters;
            super.setVmId(tempVar.getContainerId());
            if (getDiskImage() != null && getDiskImage().getstorage_pool_id() != null) {
                setStoragePoolId(getDiskImage()
                        .getstorage_pool_id().getValue());
            }
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
            _imageGroupId = getDiskImage().getId() != null ? getDiskImage().getId().getValue()
                    : Guid.Empty;
        }
        return _imageGroupId;

    }

    protected void setImageGroupId(Guid value) {
        _imageGroupId = value;
    }

    @Override
    protected void executeCommand() {
        CheckImageValidity();
    }

    /**
     * Check if image is valid snapshot of vm
     */
    protected void CheckImageValidity() {
        try {
            DiskImage diskImage = getImage();
            Guid storagePoolId = diskImage.getstorage_pool_id() != null ? diskImage.getstorage_pool_id().getValue()
                    : Guid.Empty;
            Guid storageDomainId =
                    getStorageDomainId() != null && !getStorageDomainId().getValue().equals(Guid.Empty) ? getStorageDomainId()
                            .getValue()
                            : diskImage.getstorage_ids() != null && diskImage.getstorage_ids().size() > 0 ? diskImage.getstorage_ids()
                                    .get(0)
                                    : Guid.Empty;
            Guid imageGroupId = diskImage.getId() != null ? diskImage.getId().getValue()
                    : Guid.Empty;

            DiskImage image = (DiskImage) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageInfo,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    getImage().getImageId())).getReturnValue();

            if (image.getimageStatus() != ImageStatus.OK) {
                diskImage.setimageStatus(image.getimageStatus());
                getImageDao().update(diskImage.getImage());
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }

            diskImage.setlastModified(image.getlast_modified_date());
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
        if (ImagesHandler.isVmInPreview(getVmId())) {
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
        return findImageForSameDrive(DbFacade.getInstance()
                .getSnapshotDao()
                .getId(getVmDAO().getVmsListForDisk(getImage().getId()).get(0).getId(), snapshotType));
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
            if (getDiskImage().getId().equals(diskImage.getId())) {
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
        retDiskImage.setId(getImageGroupId());
        retDiskImage.setlast_modified_date(new Date());
        retDiskImage.setQuotaId(getParameters().getQuotaId());
        return retDiskImage;
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
    }

    protected static void CompleteAdvancedDiskData(DiskImage from, DiskImage to) {
        to.setBoot(from.isBoot());
        to.setDiskInterface(from.getDiskInterface());
        to.setPropagateErrors(from.getPropagateErrors());
        to.setWipeAfterDelete(from.isWipeAfterDelete());
    }

    protected void AddDiskImageToDb(DiskImage image, CompensationContext compensationContext) {
        image.setactive(true);
        getImageDao().save(image.getImage());
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(image.getImageId());
        diskDynamic.setactual_size(image.getactual_size());
        DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
        image_storage_domain_map image_storage_domain_map = new image_storage_domain_map(image.getImageId(),
                image.getstorage_ids().get(0));
        DbFacade.getInstance()
                .getImageStorageDomainMapDao()
                .save(image_storage_domain_map);
        boolean isDiskAdded = saveDiskIfNotExists(image);
        if (compensationContext != null) {
            compensationContext.snapshotNewEntity(image.getImage());
            compensationContext.snapshotNewEntity(diskDynamic);
            compensationContext.snapshotNewEntity(image_storage_domain_map);
            if (isDiskAdded) {
                compensationContext.snapshotNewEntity(image);
            }
            compensationContext.stateChanged();
        }
    }

    /**
     * Save the disk from the given image info, only if the disk doesn't exist already.
     * @param image
     *            The image to take the disk's details from.
     */
    protected boolean saveDiskIfNotExists(DiskImage image) {
        if (!getBaseDiskDao().exists(image.getId())) {
            getBaseDiskDao().save(image);
            return true;
        }
        return false;
    }

    protected BaseDiskDao getBaseDiskDao() {
        return DbFacade.getInstance().getBaseDiskDao();
    }

    protected void LockImage() {
        SetImageStatus(getParameters().isImportEntity() ? getDestinationDiskImage() : getDiskImage(),
                ImageStatus.LOCKED);
    }

    protected void UnLockImage() {
        SetImageStatus(getParameters().isImportEntity() ? getDestinationDiskImage() : getDiskImage(), ImageStatus.OK);
    }

    protected void MarkImageAsIllegal() {
        SetImageStatus(getParameters().isImportEntity() ? getDestinationDiskImage() : getDiskImage(),
                ImageStatus.ILLEGAL);
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

            Guid newImageGroupId = getDestinationDiskImage().getId() != null ? getDestinationDiskImage()
                    .getId().getValue() : Guid.Empty;
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
            RemoveSnapshot(getDestinationDiskImage());
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
        DbFacade.getInstance().getImageStorageDomainMapDao().remove(snapshot.getImageId());
        getImageDao().remove(snapshot.getImageId());
        List<DiskImage> imagesForDisk =
                getDiskImageDao().getAllSnapshotsForImageGroup(snapshot.getId());
        if (imagesForDisk == null || imagesForDisk.isEmpty()) {
            getBaseDiskDao().remove(snapshot.getId());
        }
    }

    public static void GetImageChildren(Guid snapshot, List<Guid> children) {
        List<Guid> list = new ArrayList<Guid>();
        for (DiskImage image : DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForParent(snapshot)) {
            list.add(image.getImageId());
        }
        children.addAll(list);
        for (Guid snapshotId : list) {
            GetImageChildren(snapshotId, children);
        }
    }

    protected void RemoveChildren(Guid snapshot) {
        List<Guid> children = new ArrayList<Guid>();
        GetImageChildren(snapshot, children);
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
    static public image_storage_domain_map saveImage(DiskImage diskImage) {
        DbFacade.getInstance().getImageDao().save(diskImage.getImage());
        image_storage_domain_map image_storage_domain_map = new image_storage_domain_map(diskImage.getImageId(),
                diskImage.getstorage_ids()
                        .get(0));
        DbFacade.getInstance()
                .getImageStorageDomainMapDao()
                .save(image_storage_domain_map);
        return image_storage_domain_map;
    }
}
