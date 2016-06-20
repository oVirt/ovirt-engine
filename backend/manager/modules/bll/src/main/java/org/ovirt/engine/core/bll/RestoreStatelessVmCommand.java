package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class RestoreStatelessVmCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected RestoreStatelessVmCommand(Guid commandId) {
        super(commandId);
    }

    public RestoreStatelessVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VdcReturnValueBase result =
                runInternalActionWithTasksContext(
                        VdcActionType.UpdateVmVersion,
                        buildUpdateVmVersionParameters());

        // if it fail because of validate, its safe to restore the snapshot
        // and the vm will still be usable with previous version
        if (!result.getSucceeded() && !result.isValid()) {
            log.warn("Couldn't update VM '{}' ({}) version from it's template, continue with restoring stateless snapshot.",
                    getVm().getName(),
                    getVmId());

            setSucceeded(restoreInitialState());
        }
        else {
            setSucceeded(result.getSucceeded());
        }
    }

    private UpdateVmVersionParameters buildUpdateVmVersionParameters() {
        UpdateVmVersionParameters parameters = new UpdateVmVersionParameters(getVmId());
        // the VM is already locked by this command during the execute phase of UpdateVmVersion
        parameters.setLockVm(false);
        return parameters;
    }

    private boolean restoreInitialState() {
        Guid statelessVmSnapshotId = getVmSnapshotIdForType(SnapshotType.STATELESS);
        if (statelessVmSnapshotId == null) {
            return true;
        }

        List<DiskImage> statelessDiskSnapshots = getDiskSnapshotsForVmSnapshot(statelessVmSnapshotId);

        Guid activeVmSnapshotId = getVmSnapshotIdForType(SnapshotType.ACTIVE);
        List<DiskImage> activeDiskSnapshots = getDiskSnapshotsForVmSnapshot(activeVmSnapshotId);
        Set<Guid> disksWithStatelessSnapshot =
                statelessDiskSnapshots.stream().map(DiskImage::getId).collect(Collectors.toSet());
        for (DiskImage activeDiskSnapshot : activeDiskSnapshots) {
            if (!disksWithStatelessSnapshot.contains(activeDiskSnapshot.getId())) {
                VdcReturnValueBase returnValue = runInternalAction (
                        VdcActionType.DetachDiskFromVm,
                        new AttachDetachVmDiskParameters(new DiskVmElement(activeDiskSnapshot.getId(), getVmId()),
                                false, false));

                if (!returnValue.getSucceeded()) {
                    log.error("Could not restore stateless VM  {} due to a failure to detach Disk {}",
                            getVmId(), activeDiskSnapshot.getId());
                    return false;
                }
            }
        }

        if (!statelessDiskSnapshots.isEmpty()) {
            // restore all snapshots
            return runInternalActionWithTasksContext(VdcActionType.RestoreAllSnapshots,
                    buildRestoreAllSnapshotsParameters(statelessDiskSnapshots),
                    getLock()).getSucceeded();
        }
        return true;
    }

    private Guid getVmSnapshotIdForType(SnapshotType type) {
        return getSnapshotDao().getId(getVmId(), type);
    }

    private List<DiskImage> getDiskSnapshotsForVmSnapshot(Guid snapshotId) {
        List<DiskImage> images = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId);
        return images != null ? images : Collections.<DiskImage>emptyList();
    }

    private RestoreAllSnapshotsParameters buildRestoreAllSnapshotsParameters(List<DiskImage> imagesList) {
        RestoreAllSnapshotsParameters restoreParameters = new RestoreAllSnapshotsParameters(getVm().getId(), SnapshotActionEnum.RESTORE_STATELESS);
        restoreParameters.setShouldBeLogged(false);
        restoreParameters.setImages(imagesList);
        return restoreParameters;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

}
