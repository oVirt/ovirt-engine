package org.ovirt.engine.core.bll.storage.backup;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DeleteAllVmCheckpointsParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class DeleteAllVmCheckpointsCommand<T extends DeleteAllVmCheckpointsParameters> extends VmCommand<T>
        implements SerialChildExecutingCommand {
    @Inject
    VmCheckpointDao vmCheckpointDao;
    @Inject
    DiskImageDao diskImageDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public DeleteAllVmCheckpointsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public DeleteAllVmCheckpointsCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        setVmId(getParameters().getVmId());
    }

    @Override
    protected void executeCommand() {
        persistCommandIfNeeded();
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getDiskImages().size() == getParameters().getCompletedDisksCount()) {
            log.info("All checkpoints removed for VM '{}'", getVmName());
            TransactionSupport.executeInNewTransaction(() -> {
                vmCheckpointDao.removeAllCheckpointsByVmId(getVmId());
                return null;
            });
            return false;
        }

        clearDiskBitmaps(getParameters().getDiskImages().get(getParameters().getCompletedDisksCount()));
        getParameters().setCompletedDisksCount(getParameters().getCompletedDisksCount() + 1);
        persistCommandIfNeeded();
        return true;
    }

    private void clearDiskBitmaps(DiskImage diskImage) {
        List<DiskImage> diskImages = diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId());
        // Remove the bitmap from all the disk snapshots.
        for (DiskImage image : diskImages) {
            // RAW volumes do not support bitmaps
            if (!image.isQcowFormat()) {
                continue;
            }

            log.info("Clear all bitmaps from VM '{}' volume '{}'.", getVmName(), image.getId());
            VdsmImageLocationInfo locationInfo = new VdsmImageLocationInfo(
                    image.getStorageIds().get(0),
                    image.getId(),
                    image.getImageId(),
                    null);

            VolumeBitmapCommandParameters parameters =
                    new VolumeBitmapCommandParameters(
                            getStoragePoolId(),
                            locationInfo,
                            null);
            parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
            parameters.setParentCommand(getActionType());
            parameters.setParentParameters(getParameters());

            ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.ClearVolumeBitmaps, parameters);
            if (!returnValue.getSucceeded()) {
                log.error("Failed to remove all bitmaps for VM '{}' volume '{}'", getVmName(), image.getId());
            }
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__CHECKPOINT);
    }

    @Override
    protected void endVmCommand() {
        endActionOnDisks();
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
    }
}
