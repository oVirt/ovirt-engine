package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.action.RestoreAllCinderSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RestoreAllCinderSnapshotsCommand<T extends RestoreAllCinderSnapshotsParameters> extends VmCommand<T> {

    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public RestoreAllCinderSnapshotsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        for (CinderDisk cinderDisk : getParameters().getCinderDisksToRestore()) {
            ImagesContainterParametersBase params = getRestoreFromSnapshotParams(cinderDisk);
            restoreCinderDisk(cinderDisk, params);

            // In case we want to undo the previewed snapshot.
            if (getParameters().getSnapshot().getType() == Snapshot.SnapshotType.STATELESS) {
                Guid activeSnapshotId = snapshotDao.get(
                        getParameters().getVmId(), Snapshot.SnapshotType.ACTIVE).getId();
                updateCinderDiskSnapshot(cinderDisk.getId(), activeSnapshotId, cinderDisk.getVmSnapshotId());
            } else if (getParameters().getSnapshot().getType() != Snapshot.SnapshotType.REGULAR) {
                updateCinderDiskSnapshot(cinderDisk.getId(), getParameters().getSnapshot().getId(), null);
            }
        }
        List<CinderDisk> cinderDisksToRemove = getParameters().getCinderDisksToRemove();
        for (CinderDisk cinderDisk : cinderDisksToRemove) {
            RemoveCinderDiskParameters removeDiskParam =
                    new RemoveCinderDiskParameters(cinderDisk.getImageId());
            removeDiskParam.setRemovedVolume(cinderDisk);
            removeDiskParam.setParentCommand(getActionType());
            removeDiskParam.setStorageDomainId(cinderDisk.getStorageIds().get(0));
            removeDiskParam.setParentParameters(getParameters());
            removeDiskParam.setEndProcedure(EndProcedure.COMMAND_MANAGED);

            Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                    ActionType.RemoveCinderDisk,
                    removeDiskParam,
                    cloneContextAndDetachFromParent());
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error removing Cinder disk", e);
            }
        }
        List<CinderDisk> cinderVolumesToRemove = getParameters().getCinderVolumesToRemove();
        for (CinderDisk cinderVolume : cinderVolumesToRemove) {
            RemoveCinderDiskVolumeParameters removeDiskVolumeParam =
                    new RemoveCinderDiskVolumeParameters(cinderVolume);
            removeDiskVolumeParam.setParentCommand(getActionType());
            removeDiskVolumeParam.setParentParameters(getParameters());
            removeDiskVolumeParam.setEndProcedure(EndProcedure.COMMAND_MANAGED);

            Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                    ActionType.RemoveCinderDiskVolume,
                    removeDiskVolumeParam,
                    cloneContextAndDetachFromParent());
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error removing Cinder disk", e);
            }
        }
        setSucceeded(true);
    }

    private void updateCinderDiskSnapshot(Guid cinderDiskId, Guid snapshotId, Guid vmSnapshotId) {
        DiskImage diskFromSnapshot = diskImageDao.getDiskSnapshotForVmSnapshot(cinderDiskId, snapshotId);
        diskFromSnapshot.setActive(true);
        if (!Guid.isNullOrEmpty(vmSnapshotId)) {
            // Needed for stateless snapshot
            diskFromSnapshot.setVmSnapshotId(vmSnapshotId);
        }
        imageDao.update(diskFromSnapshot.getImage());
    }

    private ImagesContainterParametersBase getRestoreFromSnapshotParams(CinderDisk cinderDisk) {
        RemoveCinderDiskParameters params =
                new RemoveCinderDiskParameters(cinderDisk.getImageId());
        params.setRemovedVolume(cinderDisk);
        params.setParentCommand(getActionType());
        params.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        params.setParentParameters(getParameters());
        return params;
    }

    private ActionReturnValue restoreCinderDisk(CinderDisk cinderDisk, ImagesContainterParametersBase params) {
        Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                ActionType.RestoreFromCinderSnapshot,
                params,
                cloneContextAndDetachFromParent());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error restoring snapshot", e);
        }
        return null;
    }

    @Override
    protected void endSuccessfully() {
        removeRedundantVolumesForOrphanedDisks();
        if (!getParameters().isParentHasTasks()) {
            unlockSnapshot(getParameters().getSnapshot().getId());
            super.endSuccessfully();
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        removeRedundantVolumesForOrphanedDisks();
        if (!getParameters().isParentHasTasks()) {
            unlockSnapshot(getParameters().getSnapshot().getId());
            super.endWithFailure();
        }
        setSucceeded(true);
    }

    private void removeRedundantVolumesForOrphanedDisks() {
        List<CinderDisk> cinderVolumesToRemove = getParameters().getCinderVolumesToRemove();
        for (CinderDisk cinderVolume : cinderVolumesToRemove) {
            imageStorageDomainMapDao.remove(cinderVolume.getImageId());
            imageDao.remove(cinderVolume.getImageId());
        }
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
