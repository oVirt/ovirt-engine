package org.ovirt.engine.core.bll.snapshots;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ColdMergeCommandParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskStep;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class ColdMergeSnapshotSingleDiskCommand<T extends RemoveSnapshotSingleDiskParameters>
        extends RemoveSnapshotSingleDiskCommandBase<T> implements SerialChildExecutingCommand, QuotaStorageDependent {

    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public ColdMergeSnapshotSingleDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        // Let doPolling() drive the execution; we don't have any guarantee that
        // executeCommand() will finish before doPolling() is called, and we don't
        // want to possibly run the first command twice.
        getParameters().setCommandStep(RemoveSnapshotSingleDiskStep.PREPARE_MERGE);
        getParameters().setChildCommands(new HashMap<>());
        setSucceeded(true);
    }

    @Override
    public void handleFailure() {
        log.error("Command '{}' id '{}' failed executing step '{}'", getActionType(), getCommandId(),
                getParameters().getCommandStep());
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        // Upon recovery or after invoking a new child command, our map may be missing an entry
        syncChildCommandList(getParameters());
        Guid currentChildId = getCurrentChildId(getParameters());

        if (currentChildId != null) {
            getParameters().setCommandStep(getParameters().getNextCommandStep());
        }

        log.info("Command '{}' id '{}' executing step '{}'", getActionType(), getCommandId(),
                getParameters().getCommandStep());

        Pair<ActionType, ? extends ActionParametersBase> nextCommand = null;
        switch (getParameters().getCommandStep()) {
            case PREPARE_MERGE:
                nextCommand = new Pair<>(ActionType.PrepareMerge,
                        buildColdMergeParameters(getImage(), getDestinationDiskImage()));
                getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.MERGE);
                break;
            case MERGE:
                nextCommand = new Pair<>(ActionType.ColdMerge,
                        buildColdMergeParameters(getImage(), getDestinationDiskImage()));
                getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.FINALIZE_MERGE);
                break;
            case FINALIZE_MERGE:
                nextCommand = new Pair<>(ActionType.FinalizeMerge,
                        buildColdMergeParameters(getImage(), getDestinationDiskImage()));
                getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.DESTROY_IMAGE);
                break;
            case DESTROY_IMAGE:
                nextCommand = buildDestroyCommand(ActionType.DestroyImage, getActionType(),
                        Collections.singletonList(getDestinationImageId()));
                getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.COMPLETE);
                break;
            case COMPLETE:
                setCommandStatus(CommandStatus.SUCCEEDED);
                break;
        }

        persistCommandIfNeeded();
        if (nextCommand != null) {
            runInternalActionWithTasksContext(nextCommand.getFirst(), nextCommand.getSecond());
            // Add the child, but wait, it's a race!  child will start, task may spawn, get polled, and we won't have the child id
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void endSuccessfully() {
        syncDbRecords(getImageInfoFromVdsm(getDiskImage()), Collections.singleton(getDestinationImageId()), true);

        if (getParameters().getVmSnapshotId() != null) {
            lockVmSnapshotsWithWait(getVm());
            Snapshot snapshot = snapshotDao.get(getParameters().getVmSnapshotId());
            Snapshot snapshotWithoutImage =
                    imagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot, getParameters().getImageId(),
                            ovfManager);
            snapshotDao.update(snapshotWithoutImage);
            if (getSnapshotsEngineLock() != null) {
                lockManager.releaseLock(getSnapshotsEngineLock());
            }
        }

        setSucceeded(true);
    }

    @Override
    public void endWithFailure() {
        setSucceeded(true);
        TransactionSupport.executeInNewTransaction(() -> {
            if (!getParameters().isLeaveLocked()) {
                DiskImage diskImage = getDestinationDiskImage();
                if (diskImage != null) {
                    imageDao.updateStatus(diskImage.getImage().getId(), ImageStatus.OK);
                }
            }
            return null;
        });
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return Collections.emptyList();
    }

    private ColdMergeCommandParameters buildColdMergeParameters(DiskImage baseVolume, DiskImage topVolume) {
        SubchainInfo subchainInfo = new SubchainInfo(getDiskImage().getStorageIds().get(0), baseVolume, topVolume);
        ColdMergeCommandParameters parameters = new ColdMergeCommandParameters(
                getDiskImage().getStoragePoolId(), subchainInfo);
        if (getParameters().getCommandStep() == RemoveSnapshotSingleDiskStep.MERGE
                && imagesHandler.shouldUseDiskBitmaps(getVm().getCompatibilityVersion(), getDiskImage())) {
            parameters.setMergeBitmaps(true);
        }
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        return parameters;
    }
}
