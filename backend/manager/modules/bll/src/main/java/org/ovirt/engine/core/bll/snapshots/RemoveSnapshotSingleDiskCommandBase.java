package org.ovirt.engine.core.bll.snapshots;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class RemoveSnapshotSingleDiskCommandBase<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {

    @Inject
    protected OvfManager ovfManager;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    protected RemoveSnapshotSingleDiskCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Disk.name().toLowerCase(), getDiskImage().getDiskAlias());
            jobProperties.put("sourcesnapshot",
                    getSnapshotDescriptionById(getDiskImage().getVmSnapshotId()));
            jobProperties.put("destinationsnapshot",
                    getSnapshotDescriptionById(getDestinationDiskImage().getVmSnapshotId()));
        }
        return jobProperties;
    }

    protected DiskImage getImageInfoFromVdsm(final DiskImage targetImage) {

        try {
            VDSReturnValue ret = runVdsCommand(
                    VDSCommandType.GetImageInfo,
                    new GetImageInfoVDSCommandParameters(targetImage.getStoragePoolId(),
                            targetImage.getStorageIds().get(0),
                            targetImage.getId(),
                            targetImage.getImageId()));

            return (DiskImage) ret.getReturnValue();
        } catch (EngineException e) {
            log.warn("Failed to get info of volume '{}' using GetImageInfo", targetImage.getImageId(), e);
            return null;
        }
    }

    protected void updateDiskImageDynamic(final DiskImage imageFromVdsm, final DiskImage targetImage) {
        // Update image's actual size in DB
        if (imageFromVdsm != null) {
            completeImageData(imageFromVdsm);
        } else {
            log.warn("Could not update DiskImage's size with ID '{}'",
                    targetImage.getImageId());
        }
    }

    protected DestroyImageParameters buildDestroyImageParameters(Guid imageGroupId, List<Guid> imageList, ActionType actionType) {
        DestroyImageParameters parameters = new DestroyImageParameters(
                getVdsId(),
                getVmId(),
                getDiskImage().getStoragePoolId(),
                getDiskImage().getStorageIds().get(0),
                imageGroupId,
                imageList,
                getDiskImage().isWipeAfterDelete(),
                false);
        parameters.setParentCommand(actionType);
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    /**
     * Updates (but does not persist) the parameters.childCommands list to ensure the current
     * child command is present.  This is necessary in various entry points called externally
     * (e.g. by endAction()), which can be called after a child command is started but before
     * the main proceedCommandExecution() loop has persisted the updated child list.
     */
    protected void syncChildCommandList(RemoveSnapshotSingleDiskParameters parameters) {
        // Both ColdMergeSnapshotSingleDiskCommand and RemoveSnapshotSingleDiskLiveCommand, hold
        // a list of the executed children. The list is constructed based on the number of children
        // provided by CoCo. The number of children will also be incremented if child commands fail.
        // In this method, we build a mapping between the command step and the command Id. As the last
        // command in the children command list is the current step being executed, we always put that
        // command as the value of the current step.
        List<Guid> childCommandIds = commandCoordinatorUtil
                .getChildCommandIds(getCommandId())
                .stream()
                .map(guid -> commandCoordinatorUtil.getCommandEntity(guid))
                .sorted(Comparator.comparing(CommandEntity::getCreatedAt))
                .map(CommandEntity::getId)
                .collect(Collectors.toList());
        if (childCommandIds.size() != parameters.getChildCommands().size()) {
            parameters.getChildCommands().put(parameters.getCommandStep(), childCommandIds.get(childCommandIds.size()-1));
        }
    }

    protected Guid getCurrentChildId(RemoveSnapshotSingleDiskParameters parameters) {
        return parameters.getChildCommands().get(parameters.getCommandStep());
    }

    protected DiskImage getActiveDiskImage() {
        Guid snapshotId = snapshotDao.getId(getVmId(), Snapshot.SnapshotType.ACTIVE);
        return diskImageDao.getDiskSnapshotForVmSnapshot(getDiskImage().getId(), snapshotId);
    }

    /**
     * After merging the snapshots, update the image and snapshot records in the
     * database to reflect the changes.  This handles either forward or backwards
     * merge (detected).  It will either then remove the images, or mark them
     * illegal (to handle the case where image deletion failed).
     *
     * @param removeImages Remove the images from the database, or if false, only
     *                     mark them illegal
     */
    protected void syncDbRecords(DiskImage imageFromVdsm, Set<Guid> imagesToUpdate, boolean removeImages) {
        TransactionSupport.executeInNewTransaction(() -> {
            // If deletion failed after a backwards merge, the snapshots' images need to be swapped
            // as they would upon success.  Instead of removing them, mark them illegal.
            DiskImage baseImage = getDiskImage();
            DiskImage topImage = getDestinationDiskImage();

            // The vdsm merge verb may decide to perform a forward or backward merge.
            if (topImage == null) {
                log.info("No merge destination image, not updating image/snapshot association");
            } else {
                handleBackwardMerge(topImage, baseImage, imageFromVdsm);
            }

            if (imagesToUpdate == null) {
                log.error("Failed to update orphaned images in db: image list could not be retrieved");
                return null;
            }
            for (Guid imageId : imagesToUpdate) {
                if (removeImages) {
                    imageDao.remove(imageId);
                } else {
                    // The (illegal && no-parent && no-children) status indicates an orphaned image.
                    Image image = imageDao.get(imageId);
                    image.setStatus(ImageStatus.ILLEGAL);
                    image.setParentId(Guid.Empty);
                    imageDao.update(image);
                }
            }
            return null;
        });
    }

    protected Pair<ActionType, DestroyImageParameters> buildDestroyCommand(ActionType actionToRun,
                                                                              ActionType parentCommand,
                                                                              List<Guid> images) {
        return new Pair<>(actionToRun, buildDestroyImageParameters(getActiveDiskImage().getId(),
                images, parentCommand));
    }

    private void handleBackwardMerge(DiskImage topImage, DiskImage baseImage, DiskImage imageFromVdsm) {
        // For backwards merge, the prior base image now has the data associated with the newer
        // snapshot we want to keep.  Re-associate this older image with the newer snapshot.
        // The base snapshot is deleted if everything went well.  In case it's not deleted, we
        // hijack it to preserve a link to the broken image.  This makes the image discoverable
        // so that we can retry the deletion later, yet doesn't corrupt the VM image chain.
        List<DiskImage> children = diskImageDao.getAllSnapshotsForParent(topImage.getImageId());
        if (!children.isEmpty()) {
            DiskImage childImage = children.get(0);
            childImage.setParentId(baseImage.getImageId());
            imageDao.update(childImage.getImage());
        }

        Image oldTopImage = topImage.getImage();
        topImage.setImage(baseImage.getImage());
        baseImage.setImage(oldTopImage);

        Guid oldTopSnapshotId = topImage.getImage().getSnapshotId();
        topImage.getImage().setSnapshotId(baseImage.getImage().getSnapshotId());
        baseImage.getImage().setSnapshotId(oldTopSnapshotId);

        boolean oldTopIsActive = topImage.getImage().isActive();
        topImage.getImage().setActive(baseImage.getImage().isActive());
        VolumeClassification baseImageVolumeClassification =
                VolumeClassification.getVolumeClassificationByActiveFlag(baseImage.getImage().isActive());
        topImage.getImage().setVolumeClassification(baseImageVolumeClassification);
        baseImage.getImage().setActive(oldTopIsActive);
        VolumeClassification oldTopVolumeClassification =
                VolumeClassification.getVolumeClassificationByActiveFlag(oldTopIsActive);
        topImage.getImage().setVolumeClassification(oldTopVolumeClassification);

        // If we remain with a RAW disk after removing a snapshot, backup property must be set back to none.
        if (oldTopImage.getVolumeFormat() == VolumeFormat.COW && topImage.getVolumeFormat() == VolumeFormat.RAW
                && topImage.getImage().isActive()) {
            baseImage.setBackup(DiskBackup.None);
        }
        topImage.setSize(baseImage.getSize());
        topImage.setActualSizeInBytes(imageFromVdsm.getActualSizeInBytes());
        topImage.setImageStatus(ImageStatus.OK);
        baseDiskDao.update(topImage);
        imageDao.update(topImage.getImage());
        updateDiskImageDynamic(imageFromVdsm, topImage);

        baseDiskDao.update(baseImage);
        imageDao.update(baseImage.getImage());

        removeTopImageMemoryIfNeeded(topImage);

        updateVmConfigurationForImageChange(topImage.getImage().getSnapshotId(),
                baseImage.getImageId(), topImage);

        updateVmConfigurationForImageRemoval(baseImage.getImage().getSnapshotId(),
                topImage.getImageId());
    }

    private boolean isRemoveTopImageMemoryNeeded(Snapshot snapshot) {
        if (snapshot.containsMemory()) {
            VM vmSnapshot = new VM();
            FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vmSnapshot);
            try {
                ovfManager.importVm(snapshot.getVmConfiguration(), vmSnapshot, fullEntityOvfData);
                if (Version.getLowest()
                        .greater(vmSnapshot.getStaticData().getClusterCompatibilityVersionOrigin())) {
                    return true;
                }
            } catch (OvfReaderException e) {
                log.error("Failed to read snapshot '{}' configuration", snapshot.getId());
            }
        }
        return false;
    }

    private void removeTopImageMemoryIfNeeded(DiskImage topImage) {
        // If the top image snapshot created in a cluster version < 3.6, the memory of the snapshot
        // is not supported by the engine.
        // The engine updates the snapshot's cluster compatibility version to the lowest supported version (3.6)
        // to be able to update the snapshot OVF.
        // Therefore, the memory of the snapshot should be removed to prevent a preview of unsupported snapshot memory.
        Snapshot topSnapshot = snapshotDao.get(topImage.getImage().getSnapshotId());
        if (isRemoveTopImageMemoryNeeded(topSnapshot)) {
            ActionReturnValue retVal = runInternalAction(
                    ActionType.RemoveMemoryVolumes,
                    new RemoveMemoryVolumesParameters(topSnapshot, getVmId()),
                    cloneContextAndDetachFromParent());

            if (!retVal.getSucceeded()) {
                log.error("Failed to remove memory volumes '{}, {}'",
                        topSnapshot.getMemoryDiskId(),
                        topSnapshot.getMetadataDiskId());
            }
        }
    }

    private void updateVmConfigurationForImageChange(final Guid snapshotId, final Guid oldImageId, final DiskImage newImage) {
        try {
            lockVmSnapshotsWithWait(getVm());

            TransactionSupport.executeInNewTransaction(() -> {
                Snapshot s = snapshotDao.get(snapshotId);
                s = imagesHandler.prepareSnapshotConfigWithAlternateImage(s, oldImageId, newImage, ovfManager);
                snapshotDao.update(s);
                return null;
            });
        } finally {
            if (getSnapshotsEngineLock() != null) {
                lockManager.releaseLock(getSnapshotsEngineLock());
            }
        }
    }

    private void updateVmConfigurationForImageRemoval(final Guid snapshotId, final Guid imageId) {
        try {
            lockVmSnapshotsWithWait(getVm());

            TransactionSupport.executeInNewTransaction(() -> {
                Snapshot s = snapshotDao.get(snapshotId);
                s = imagesHandler.prepareSnapshotConfigWithoutImageSingleImage(s, imageId, ovfManager);
                snapshotDao.update(s);
                return null;
            });
        } finally {
            if (getSnapshotsEngineLock() != null) {
                lockManager.releaseLock(getSnapshotsEngineLock());
            }
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        // Lock the template image to prevent having it teared down
        if (!Guid.Empty.equals(getDiskImage().getImageTemplateId())) {
            return Collections.singletonMap(getDiskImage().getImageTemplateId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE,
                                EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED));
        }

        return null;
    }


    @Override
    protected void endWithFailure() {
        // TODO: FILL! We should determine what to do in case of
        // failure (is everything rolled-backed? rolled-forward?
        // some and some?).
        setSucceeded(true);
    }

    private String getSnapshotDescriptionById(Guid snapshotId) {
        Snapshot snapshot = snapshotDao.get(snapshotId);
        return snapshot != null ? snapshot.getDescription() : StringUtils.EMPTY;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }
}
