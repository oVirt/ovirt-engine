package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.lock.EngineLock;

/**
 * Base class for all image handling commands
 */
public abstract class BaseImagesCommand<T extends ImagesActionsParametersBase> extends StorageDomainCommandBase<T> {

    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmDao vmDao;

    private DiskImage destinationImage;
    private DiskImage image;
    private Guid imageId = Guid.Empty;
    private EngineLock snapshotsEngineLock;

    protected BaseImagesCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        imageId = parameters.getImageId();
    }

    @Override
    public void init() {
        super.init();
        if (getParameters() instanceof ImagesContainterParametersBase) {
            initContainerDetails((ImagesContainterParametersBase) getParameters());
        }
    }

    protected BaseImagesCommand(Guid commandId) {
        super(commandId);
    }

    private void initContainerDetails(ImagesContainterParametersBase parameters) {
        super.setVmId(parameters.getContainerId());
        if (parameters.getStoragePoolId() != null && !Guid.Empty.equals(parameters.getStoragePoolId())) {
            setStoragePoolId(parameters.getStoragePoolId());
        } else if (getDiskImage() != null && getDiskImage().getStoragePoolId() != null) {
            setStoragePoolId(getDiskImage().getStoragePoolId());
        }
    }

    protected DiskImage getImage() {
        if (image == null) {
            image = diskImageDao.get(getImageId());
            if (image == null) {
                image = diskImageDao.getSnapshotById(getImageId());
            }
        }
        return image;
    }

    protected void setImage(DiskImage image) {
        this.image = image;
    }

    protected Guid getImageId() {
        return imageId;
    }

    protected void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    private DiskImage diskImage;

    protected DiskImage getDiskImage() {
        if (diskImage == null) {
            diskImage = getImage();
        }
        return diskImage;
    }

    protected void setDiskImage(DiskImage value) {
        diskImage = value;
    }

    private Guid destinationImageId = Guid.Empty;

    protected Guid getDestinationImageId() {
        return getParameters() != null ? getParameters().getDestinationImageId() : destinationImageId;
    }

    protected void setDestinationImageId(Guid value) {
        if (getParameters() != null) {
            getParameters().setDestinationImageId(value);
        } else {
            destinationImageId = value;
        }
    }

    protected boolean isDataOperationsByHSM() {
        return getStorageDomain().getStorageStaticData().getStorageDomainType().isDataDomain() ||
                getStorageDomain().getStorageStaticData().getStorageDomainType() == StorageDomainType.ManagedBlockStorage;
    }

    protected boolean performImageVdsmOperation() {
        throw new UnsupportedOperationException();
    }

    protected DiskImage getDestinationDiskImage() {
        if (destinationImage == null) {
            destinationImage = diskImageDao.get(getDestinationImageId());
            if (destinationImage == null) {
                destinationImage = diskImageDao.getSnapshotById(getDestinationImageId());
            }
        }
        return destinationImage;
    }

    private Guid imageGroupId = Guid.Empty;

    protected Guid getImageGroupId() {
        if (imageGroupId.equals(Guid.Empty)) {
            imageGroupId = getDiskImage().getId() != null ? getDiskImage().getId()
                    : Guid.Empty;
        }
        return imageGroupId;

    }

    protected void setImageGroupId(Guid value) {
        imageGroupId = value;
    }

    protected EngineLock getSnapshotsEngineLock() {
        return snapshotsEngineLock;
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
        return findImageForSameDrive(snapshotDao
                .getId(vmDao.getVmsListForDisk(getImage().getId(), false).get(0).getId(), snapshotType));
    }

    /**
     * Update the old image that represents the disk of the command's image to be in the given active state.
     *
     * @param snapshotType
     *            The type of snapshot to look for the same image in.
     * @param active
     *            The active state.
     */
    protected void updateOldImageAsActive(SnapshotType snapshotType, boolean active) {
        Guid oldImageId = findImageForSameDrive(snapshotType);
        if (oldImageId == null) {
            log.error("Can't find image to update to active '{}', snapshot type '{}', original image id '{}'",
                    active,
                    snapshotType,
                    getImageId());
            return;
        }

        DiskImage oldImage = diskImageDao.getSnapshotById(oldImageId);
        oldImage.setActive(active);
        imageDao.update(oldImage.getImage());
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
        List<DiskImage> imagesFromSnapshot = diskImageDao.getAllSnapshotsForVmSnapshot(snapshotId);
        for (DiskImage diskImage : imagesFromSnapshot) {
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
        retDiskImage.setDiskProfileId(getParameters().getDiskProfileId());
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
        DiskImageDynamic destinationDiskDynamic = diskImageDynamicDao.get(getDestinationDiskImage().getImageId());
        if (destinationDiskDynamic != null) {
            destinationDiskDynamic.setActualSize(fromIRS.getActualSizeInBytes());
            diskImageDynamicDao.update(destinationDiskDynamic);
        }
    }

    protected void addDiskImageToDb(DiskImage image, CompensationContext compensationContext, boolean active) {
        image.setActive(active);
        imageDao.save(image.getImage());
        DiskImageDynamic diskDynamic = updateDiskImageDynamicIntoDB(image);
        ImageStorageDomainMap imageStorageDomainMap = new ImageStorageDomainMap(image.getImageId(),
                image.getStorageIds().get(0), image.getQuotaId(), image.getDiskProfileId());
        imageStorageDomainMapDao.save(imageStorageDomainMap);
        boolean isDiskAdded = saveDiskIfNotExists(image);
        if (compensationContext != null) {
            compensationContext.snapshotNewEntity(image.getImage());
            compensationContext.snapshotNewEntity(diskDynamic);
            compensationContext.snapshotNewEntity(imageStorageDomainMap);
            if (isDiskAdded) {
                compensationContext.snapshotNewEntity(image);
            }
            compensationContext.stateChanged();
        }
    }

    protected DiskImageDynamic updateDiskImageDynamicIntoDB(DiskImage image) {
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(image.getImageId());
        diskDynamic.setActualSize(image.getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);
        return diskDynamic;
    }

    /**
     * Save the disk from the given image info, only if the disk doesn't exist already.
     * @param image
     *            The image to take the disk's details from.
     */
    protected boolean saveDiskIfNotExists(DiskImage image) {
        if (!baseDiskDao.exists(image.getId())) {
            baseDiskDao.save(image);
            return true;
        }
        return false;
    }

    protected void lockImage() {
        setImageStatus(ImageStatus.LOCKED);
    }

    protected void unLockImage() {
        setImageStatus(ImageStatus.OK);
    }

    protected void setImageStatus(ImageStatus imageStatus) {
        setImageStatus(imageStatus, getRelevantDiskImage());
    }

    protected void setImageStatus(ImageStatus imageStatus, DiskImage diskImage) {
        if (diskImage != null && diskImage.getImageStatus() != imageStatus) {
            diskImage.setImageStatus(imageStatus);
            imagesHandler.updateImageStatus(diskImage.getImage().getId(), imageStatus);
        }
    }

    protected DiskImage getRelevantDiskImage() {
        return getParameters().isImportEntity() ? getDestinationDiskImage() : getDiskImage();
    }

    protected DiskImage getVolumeInfo(Guid storagePoolId, Guid newStorageDomainID, Guid newImageGroupId,
                                         Guid newImageId) {
        return (DiskImage) runVdsCommand(
                VDSCommandType.GetImageInfo,
                new GetImageInfoVDSCommandParameters(storagePoolId, newStorageDomainID, newImageGroupId,
                        newImageId)).getReturnValue();
    }

    @Override
    protected void endSuccessfully() {
        if (getDestinationDiskImage() != null && !getDestinationDiskImage().getStorageTypes().get(0).isManagedBlockStorage()) {
            Guid storagePoolId = getDestinationDiskImage().getStoragePoolId() != null ? getDestinationDiskImage()
                    .getStoragePoolId() : Guid.Empty;
            setStoragePoolId(storagePoolId);

            Guid newImageGroupId = getDestinationDiskImage().getId() != null ? getDestinationDiskImage()
                    .getId() : Guid.Empty;
            Guid newImageId = getDestinationDiskImage().getImageId();
            Guid newStorageDomainID = getDestinationDiskImage().getStorageIds().get(0);

            // complete IRS data to DB disk image:
            try {
                DiskImage newImageIRS = getVolumeInfo(storagePoolId, newStorageDomainID, newImageGroupId, newImageId);

                if (newImageIRS != null) {
                    completeImageData(newImageIRS);

                    // Set volume type/format before updating DB in the 'finally' branch
                    getDestinationDiskImage().getImage().setVolumeType(newImageIRS.getVolumeType());
                    getDestinationDiskImage().getImage().setVolumeFormat(newImageIRS.getVolumeFormat());
                    if (newImageIRS.getVolumeFormat().equals(VolumeFormat.COW)) {
                        setQcowCompatByQemuImageInfo(storagePoolId,
                                newImageGroupId,
                                newImageId,
                                newStorageDomainID,
                                getDestinationDiskImage());
                    }
                }
            } catch (EngineException e) {
                // Logging only
                log.error("Unable to update the image info for image '{}' (image group: '{}') on domain '{}'",
                        newImageId, newImageGroupId, newStorageDomainID);
            } finally {
                if (!getParameters().isLeaveLocked()) {
                    getDestinationDiskImage().setImageStatus(ImageStatus.OK);
                }

                // This is a hack for managed block storage -> image copy,
                // the purpose is convert the disk storage type back to IMAGE
                if (getActionType() == ActionType.CopyImageGroup
                        && !getParameters().isImportEntity()
                        && isImageDomain(getDestinationDiskImage().getStorageTypes().get(0))
                        && getDiskImage().getStorageTypes().get(0).isManagedBlockStorage()) {
                    DiskImage diskImage = DiskImage.copyOf(getDestinationDiskImage());
                    baseDiskDao.update(diskImage);
                }

                imageDao.update(getDestinationDiskImage().getImage());
            }
        }

        if (!getParameters().isLeaveLocked()) {
            unLockImage();
        }

        setSucceeded(true);
    }

    private boolean isImageDomain(StorageType storageType) {
        return storageType.isBlockDomain() || storageType.isFileDomain();
    }

    protected void setQcowCompatByQemuImageInfo(Guid storagePoolId,
            Guid newImageGroupId,
            Guid newImageId,
            Guid newStorageDomainID,
            DiskImage diskImage) {

        // If the VM is running then the volume is already prepared in the guest's host so there
        // is no need for prepare and teardown.
        Guid hostIdToExecuteQemuImageInfo = vmDao.getVmsWithPlugInfo(diskImage.getId())
                .stream()
                .filter(p -> p.getSecond().isPlugged())
                .map(p -> p.getFirst().getRunOnVds())
                .filter(Objects::nonNull) // filter out null Vds, happens when VM isn't running
                .findFirst()
                .orElse(null);

        setQcowCompat(diskImage.getImage(),
                storagePoolId,
                newImageGroupId,
                newImageId,
                newStorageDomainID,
                hostIdToExecuteQemuImageInfo);
    }

    protected void setQcowCompat(Image diskImage,
            Guid storagePoolId,
            Guid newImageGroupId,
            Guid newImageId,
            Guid newStorageDomainID,
            Guid hostIdForExecution) {
        diskImage.setQcowCompat(QcowCompat.QCOW2_V2);
        QemuImageInfo qemuImageInfo = imagesHandler.getQemuImageInfoFromVdsm(storagePoolId,
                newStorageDomainID,
                newImageGroupId,
                newImageId,
                hostIdForExecution,
                hostIdForExecution == null);
        if (qemuImageInfo != null) {
            diskImage.setQcowCompat(qemuImageInfo.getQcowCompat());
        }
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

        if (!getParameters().isLeaveLocked()) {
            unLockImage();
        }
    }

    /**
     * TODO: move it other class in hierarchy
     */

    protected void removeSnapshot(DiskImage snapshot) {
        imageStorageDomainMapDao.remove(snapshot.getImageId());
        imageDao.remove(snapshot.getImageId());
        List<DiskImage> imagesForDisk =
                diskImageDao.getAllSnapshotsForImageGroup(snapshot.getId());
        if (imagesForDisk == null || imagesForDisk.isEmpty()) {
            baseDiskDao.remove(snapshot.getId());
        }
    }

    protected void lockVmSnapshotsWithWait(VM vm) {
        snapshotsEngineLock = new EngineLock();
        Map<String, Pair<String, String>> snapshotsExlusiveLockMap =
                Collections.singletonMap(vm.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_SNAPSHOTS, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        snapshotsEngineLock.setExclusiveLocks(snapshotsExlusiveLockMap);
        lockManager.acquireLockWait(snapshotsEngineLock);
    }
}
