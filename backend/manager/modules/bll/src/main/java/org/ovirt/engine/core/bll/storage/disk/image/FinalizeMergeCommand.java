package org.ovirt.engine.core.bll.storage.disk.image;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ColdMergeCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class FinalizeMergeCommand<T extends ColdMergeCommandParameters> extends MergeSPMBaseCommand<T> {

    public FinalizeMergeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        executeSPMMergeCommand(VDSCommandType.FinalizeMerge);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.finalizeMerge;
    }
}
