package org.ovirt.engine.core.bll;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
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
import org.ovirt.engine.core.dao.DiskImageDynamicDAO;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

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
            initContainerDetails((ImagesContainterParametersBase) parameters);
        }
    }

    protected BaseImagesCommand(Guid commandId) {
        super(commandId);
    }

    protected void initContainerDetails(ImagesContainterParametersBase parameters) {
        super.setVmId(parameters.getContainerId());
        if (getDiskImage() != null && getDiskImage().getStoragePoolId() != null) {
            setStoragePoolId(getDiskImage().getStoragePoolId().getValue());
        }
    }

    protected DiskImage getImage() {
        if (mImage == null) {
            mImage = getDiskImageDao().get(getImageId());
            if (mImage == null) {
                mImage = getDiskImageDao().getSnapshotById(getImageId());
            }
        }
        return mImage;
    }

    @Override
    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    @Override
    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    @Override
    protected DiskImageDynamicDAO getDiskImageDynamicDAO() {
        return getDbFacade().getDiskImageDynamicDao();
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
        checkImageValidity();
    }

    /**
     * Check if image is valid snapshot of vm
     */
    protected void checkImageValidity() {
        try {
            DiskImage diskImage = getImage();
            Guid storagePoolId = diskImage.getStoragePoolId() != null ? diskImage.getStoragePoolId().getValue()
                    : Guid.Empty;
            Guid storageDomainId =
                    getStorageDomainId() != null && !getStorageDomainId().getValue().equals(Guid.Empty)
                            ? getStorageDomainId().getValue()
                            : (diskImage.getStorageIds() != null && diskImage.getStorageIds().size() > 0
                                    ? diskImage.getStorageIds().get(0)
                                    : Guid.Empty);
            Guid imageGroupId = diskImage.getId() != null ? diskImage.getId().getValue()
                    : Guid.Empty;

            DiskImage image = (DiskImage) runVdsCommand(
                    VDSCommandType.GetImageInfo,
                    new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                            getImage().getImageId())).getReturnValue();

            if (image.getImageStatus() != ImageStatus.OK) {
                diskImage.setImageStatus(image.getImageStatus());
                getImageDao().update(diskImage.getImage());
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }

            diskImage.setLastModified(image.getLastModifiedDate());
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
    protected boolean canCreateSnapshot() {
        if (!new SnapshotsValidator().vmNotDuringSnapshot(getVmId()).isValid()) {
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
        return findImageForSameDrive(getSnapshotDao()
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
        List<DiskImage> imagesFromSanpshot = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId);
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
    protected DiskImage cloneDiskImage(Guid newImageGuid) {
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
        retDiskImage.setVmSnapshotId(getParameters().getVmSnapshotId());
        retDiskImage.setId(getImageGroupId());
        retDiskImage.setLastModifiedDate(new Date());
        retDiskImage.setQuotaId(getParameters().getQuotaId());
        retDiskImage.setDiskAlias(getParameters().getDiskAlias());
        return retDiskImage;
    }

    /**
      * Overrides the relevant fields of the destination disk image ('DestinationDiskImage') with some values of the IRS
      * disk image.
      * @param fromIRS
      *            the IRS disk image.
      */
    protected void completeImageData(DiskImage fromIRS) {
        getDestinationDiskImage().setCreationDate(fromIRS.getCreationDate());
        getDestinationDiskImage().setLastModifiedDate(fromIRS.getLastModifiedDate());
        getDestinationDiskImage().setLastModified(getDestinationDiskImage().getLastModifiedDate());
        DiskImageDynamic destinationDiskDynamic = getDiskImageDynamicDAO().get(getDestinationDiskImage().getImageId());
        if (destinationDiskDynamic != null) {
            destinationDiskDynamic.setactual_size(fromIRS.getActualSizeFromDiskImage());
            getDiskImageDynamicDAO().update(destinationDiskDynamic);
        }
    }

    protected static void CompleteAdvancedDiskData(DiskImage from, DiskImage to) {
        to.setBoot(from.isBoot());
        to.setDiskInterface(from.getDiskInterface());
        to.setPropagateErrors(from.getPropagateErrors());
        to.setWipeAfterDelete(from.isWipeAfterDelete());
    }

    protected void addDiskImageToDb(DiskImage image, CompensationContext compensationContext) {
        image.setActive(true);
        getImageDao().save(image.getImage());
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(image.getImageId());
        diskDynamic.setactual_size(image.getActualSizeFromDiskImage());
        getDiskImageDynamicDAO().save(diskDynamic);
        image_storage_domain_map image_storage_domain_map = new image_storage_domain_map(image.getImageId(),
                image.getStorageIds().get(0));
        getImageStorageDomainMapDao().save(image_storage_domain_map);
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

    @Override
    protected BaseDiskDao getBaseDiskDao() {
        return getDbFacade().getBaseDiskDao();
    }

    protected void lockImage() {
        setImageStatus(ImageStatus.LOCKED);
    }

    protected void lockImageWithCompensation() {
        final DiskImage diskImage = getRelevantDiskImage();
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntityStatus(diskImage.getImage(), diskImage.getImageStatus());
                getCompensationContext().stateChanged();
                setImageStatus(ImageStatus.LOCKED);
                return null;
            }});
    }

    protected void unLockImage() {
        setImageStatus(ImageStatus.OK);
    }

    protected void setImageStatus(ImageStatus imageStatus) {
        DiskImage diskImage = getRelevantDiskImage();
        if (diskImage != null) {
            diskImage.setImageStatus(imageStatus);
            ImagesHandler.updateImageStatus(diskImage.getImage().getId(), imageStatus);
        }
    }

    protected DiskImage getRelevantDiskImage() {
        return getParameters().isImportEntity() ? getDestinationDiskImage() : getDiskImage();
    }

    @Override
    protected void endSuccessfully() {
        if (getDestinationDiskImage() != null) {
            Guid storagePoolId = getDestinationDiskImage().getStoragePoolId() != null ? getDestinationDiskImage()
                    .getStoragePoolId().getValue() : Guid.Empty;

            Guid newImageGroupId = getDestinationDiskImage().getId() != null ? getDestinationDiskImage()
                    .getId().getValue() : Guid.Empty;
            Guid newImageId = getDestinationDiskImage().getImageId();
            Guid newStorageDomainID = getDestinationDiskImage().getStorageIds().get(0);

            // complete IRS data to DB disk image:
            DiskImage newImageIRS = (DiskImage) runVdsCommand(
                    VDSCommandType.GetImageInfo,
                    new GetImageInfoVDSCommandParameters(storagePoolId, newStorageDomainID, newImageGroupId,
                            newImageId)).getReturnValue();

            if (newImageIRS != null) {
                completeImageData(newImageIRS);
            }

            // Unlock destination image:
            getDestinationDiskImage().setImageStatus(ImageStatus.OK);
            getImageDao().update(getDestinationDiskImage().getImage());
        }

        unLockImage();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        undoActionOnSourceAndDestination();

        setSucceeded(true);
    }

    protected void undoActionOnSourceAndDestination() {
        if (getDestinationDiskImage() != null) {
            removeSnapshot(getDestinationDiskImage());
        }

        unLockImage();
    }

    /**
     * Vitaly TODO: move it other class in hierarchy
     */

    protected void removeSnapshot(DiskImage snapshot) {
        getImageStorageDomainMapDao().remove(snapshot.getImageId());
        getImageDao().remove(snapshot.getImageId());
        List<DiskImage> imagesForDisk =
                getDiskImageDao().getAllSnapshotsForImageGroup(snapshot.getId());
        if (imagesForDisk == null || imagesForDisk.isEmpty()) {
            getBaseDiskDao().remove(snapshot.getId());
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
                diskImage.getStorageIds()
                        .get(0));
        DbFacade.getInstance()
                .getImageStorageDomainMapDao()
                .save(image_storage_domain_map);
        return image_storage_domain_map;
    }
}
