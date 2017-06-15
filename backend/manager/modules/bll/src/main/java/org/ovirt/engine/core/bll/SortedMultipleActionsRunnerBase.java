package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;

public abstract class SortedMultipleActionsRunnerBase extends PrevalidatingMultipleActionsRunner {
    public SortedMultipleActionsRunnerBase(ActionType actionType,
            List<ActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    protected abstract void sortCommands();

    @Override
    protected void runCommands() {
        sortCommands();
        super.runCommands();
    }
}
