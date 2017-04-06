package org.ovirt.engine.core.bll.snapshots;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
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

    protected DestroyImageParameters buildDestroyImageParameters(Guid imageGroupId, List<Guid> imageList, VdcActionType actionType) {
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
        List<Guid> childCommandIds = CommandCoordinatorUtil.getChildCommandIds(getCommandId());
        if (childCommandIds.size() != parameters.getChildCommands().size()) {
            for (Guid id : childCommandIds) {
                if (!parameters.getChildCommands().containsValue(id)) {
                    parameters.getChildCommands().put(parameters.getCommandStep(), id);
                    break;
                }
            }
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
    protected void syncDbRecords(VmBlockJobType blockJobType, DiskImage imageFromVdsm, Set<Guid> imagesToUpdate, boolean removeImages) {
        TransactionSupport.executeInNewTransaction(() -> {
            // If deletion failed after a backwards merge, the snapshots' images need to be swapped
            // as they would upon success.  Instead of removing them, mark them illegal.
            DiskImage baseImage = getDiskImage();
            DiskImage topImage = getDestinationDiskImage();

            // The vdsm merge verb may decide to perform a forward or backward merge.
            if (topImage == null) {
                log.info("No merge destination image, not updating image/snapshot association");
            } else if (blockJobType == VmBlockJobType.PULL) {
                handleForwardMerge(topImage, baseImage, imageFromVdsm);
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

    protected Pair<VdcActionType, DestroyImageParameters> buildDestroyCommand(VdcActionType actionToRun,
                                                                              VdcActionType parentCommand,
                                                                              List<Guid> images) {
        return new Pair<>(actionToRun, buildDestroyImageParameters(getActiveDiskImage().getId(),
                images, parentCommand));
    }

    private void handleForwardMerge(DiskImage topImage, DiskImage baseImage, DiskImage imageFromVdsm) {
        // For forward merge, the volume format and type may change.
        topImage.setVolumeFormat(baseImage.getVolumeFormat());
        topImage.setVolumeType(baseImage.getVolumeType());
        topImage.setParentId(baseImage.getParentId());
        getDestinationDiskImage().setSize(baseImage.getSize());
        getDestinationDiskImage().setActualSizeInBytes(getImageInfoFromVdsm(getDestinationDiskImage()).getActualSizeInBytes());

        baseDiskDao.update(topImage);
        imageDao.update(topImage.getImage());
        updateDiskImageDynamic(imageFromVdsm, topImage);

        updateVmConfigurationForImageChange(getDestinationDiskImage().getImage().getSnapshotId(),
                getDestinationDiskImage().getImageId(), getDestinationDiskImage());
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

        topImage.setSize(baseImage.getSize());
        topImage.setActualSizeInBytes(imageFromVdsm.getActualSizeInBytes());
        topImage.setImageStatus(ImageStatus.OK);
        baseDiskDao.update(topImage);
        imageDao.update(topImage.getImage());
        updateDiskImageDynamic(imageFromVdsm, topImage);

        baseDiskDao.update(baseImage);
        imageDao.update(baseImage.getImage());

        updateVmConfigurationForImageChange(topImage.getImage().getSnapshotId(),
                baseImage.getImageId(), topImage);
        updateVmConfigurationForImageRemoval(baseImage.getImage().getSnapshotId(),
                topImage.getImageId());
    }

    private void updateVmConfigurationForImageChange(final Guid snapshotId, final Guid oldImageId, final DiskImage newImage) {
        try {
            lockVmSnapshotsWithWait(getVm());

            TransactionSupport.executeInNewTransaction(() -> {
                Snapshot s = snapshotDao.get(snapshotId);
                s = ImagesHandler.prepareSnapshotConfigWithAlternateImage(s, oldImageId, newImage, ovfManager);
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
                s = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(s, imageId, ovfManager);
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
}
