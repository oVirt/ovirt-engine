package org.ovirt.engine.core.bll.snapshots;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskStep;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class ColdMergeSnapshotSingleDiskCommand<T extends RemoveSnapshotSingleDiskParameters>
        extends RemoveSnapshotSingleDiskCommandBase<T> implements SerialChildExecutingCommand, QuotaStorageDependent {

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

        Pair<VdcActionType, ? extends VdcActionParametersBase> nextCommand = null;
        switch (getParameters().getCommandStep()) {
            case PREPARE_MERGE:
                break;
            case MERGE:
                break;
            case FINALIZE_MERGE:
                break;
            case DESTROY_IMAGE:
                break;
            case DESTROY_IMAGE_CHECK:
                break;
        }

        persistCommandIfNeeded();
        return false;
    }

    @Override
    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return Collections.emptyList();
    }
}
