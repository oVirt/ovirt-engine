package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.RemoveImageCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class RemoveCinderVolumeParentCommand<T extends RemoveCinderDiskParameters> extends RemoveImageCommand<T> implements SerialChildExecutingCommand {

    @Inject
    private ImageDao imageDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public RemoveCinderVolumeParentCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    /**
     * Return the future command to be executed from the childCommandsParameters, we pass the storage's disk as the
     * subject entity only in the first call, since all the other volumes should be on the same Storage Domain.
     *
     * @param removedChildCommandParametersIndex
     *            - The index to fetch the child command parameters.
     * @return - The future command at the ChildCommandsParameters[removedChildCommandParametersIndex].
     */
    protected Future<ActionReturnValue> getFutureRemoveCinderDiskVolume(int removedChildCommandParametersIndex) {
        return commandCoordinatorUtil.executeAsyncCommand(ActionType.RemoveCinderDiskVolume,
                getParameters().getChildCommandsParameters().get(removedChildCommandParametersIndex),
                cloneContextAndDetachFromParent());
    }

    public boolean removeCinderVolume(int removedVolumeIndex) {
        RemoveCinderDiskVolumeParameters param = getParameters().getChildCommandsParameters().get(removedVolumeIndex);
        try {
            ActionReturnValue actionReturnValue =
                    getFutureRemoveCinderDiskVolume(removedVolumeIndex).get();
            if (actionReturnValue == null || !actionReturnValue.getSucceeded()) {
                handleExecutionFailure(param.getRemovedVolume(), actionReturnValue);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error removing Cinder disk volume. Exception: {}", e);
            return false;
        }
        return true;
    }

    protected void initCinderDiskVolumesParametersList(CinderDisk cinderDisk) {
        // Get all the Cinder disk volumes ordered from the leaf to the parent.
        getParameters().getChildCommandsParameters().add(createChildParams(cinderDisk));
        List<DiskImage> dependantVolumesForSnapshot =
                diskImageDao.getAllSnapshotsForParent(cinderDisk.getImageId());
        while (!dependantVolumesForSnapshot.isEmpty()) {
            cinderDisk = (CinderDisk) dependantVolumesForSnapshot.get(0);
            getParameters().getChildCommandsParameters().addFirst(createChildParams(cinderDisk));
            dependantVolumesForSnapshot = diskImageDao.getAllSnapshotsForParent(cinderDisk.getImageId());
        }
    }

    private RemoveCinderDiskVolumeParameters createChildParams(CinderDisk cinderDiskVolume) {
        RemoveCinderDiskVolumeParameters childParam = new RemoveCinderDiskVolumeParameters(cinderDiskVolume);
        childParam.setParentCommand(getActionType());
        childParam.setParentParameters(getParameters());
        childParam.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return childParam;
    }

    protected void handleExecutionFailure(CinderDisk disk, ActionReturnValue actionReturnValue) {
        log.error("Failed to remove cider volume id '{}' for disk id '{}'.", disk.getImageId(), disk.getId());
        EngineFault fault = actionReturnValue == null ? new EngineFault() : actionReturnValue.getFault();
        getReturnValue().setFault(fault);
    }

    protected void removeDiskFromDb(final CinderDisk cinderVolume, Snapshot updated) {
        if (cinderVolume.getActive()) {
            // Get the base volume and set it as active, so the disk will not disappear from the disks view.
            Image baseVol = imageDao.get(cinderVolume.getId());
            baseVol.setActive(true);
            imageDao.update(baseVol);
        }
        imageStorageDomainMapDao.remove(cinderVolume.getImageId());
        imageDao.remove(cinderVolume.getImageId());
        diskImageDynamicDao.remove(cinderVolume.getImageId());
        if (updated != null) {
            snapshotDao.update(updated);
        }
    }

    protected Snapshot getSnapshotWithoutCinderVolume(CinderDisk lastCinderVolume) {
        Guid vmSnapshotId = lastCinderVolume.getVmSnapshotId();
        Snapshot updated = null;
        if (vmSnapshotId != null && !Guid.Empty.equals(vmSnapshotId)) {
            Snapshot snapshot = snapshotDao.get(vmSnapshotId);
            if (snapshot != null) {
                updated = imagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot,
                        lastCinderVolume.getImageId(), ovfManager);
            }
        }
        return updated;
    }

    public void removeDiskFromDbCallBack(final CinderDisk cinderVolume) {
        final Snapshot updated =
                getParameters().isUpdateSnapshot() ? getSnapshotWithoutCinderVolume(cinderVolume) : null;

        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                () -> {
                    removeDiskFromDb(cinderVolume, updated);
                    return null;
                });
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        CinderDisk cinderVolume =
                getParameters().getChildCommandsParameters().get(completedChildCount - 1).getRemovedVolume();
        removeDiskFromDbCallBack(cinderVolume);

        if(getParameters().getChildCommandsParameters().size() == completedChildCount){
            return false;
        }
        getParameters().setRemovedVolumeIndex(completedChildCount);
        removeCinderVolume(completedChildCount);
        return true;
    }
}
