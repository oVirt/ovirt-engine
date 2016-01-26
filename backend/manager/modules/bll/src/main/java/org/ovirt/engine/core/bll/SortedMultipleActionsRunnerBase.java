package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

public abstract class SortedMultipleActionsRunnerBase extends PrevalidatingMultipleActionsRunner {
    public SortedMultipleActionsRunnerBase(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    protected abstract void sortCommands();

    @Override
    protected void runCommands() {
        sortCommands();
        super.runCommands();
    }
}
