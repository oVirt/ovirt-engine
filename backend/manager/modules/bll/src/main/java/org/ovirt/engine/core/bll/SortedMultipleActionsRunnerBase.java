package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

public abstract class SortedMultipleActionsRunnerBase extends MultipleActionsRunner {
    public SortedMultipleActionsRunnerBase(VdcActionType actionType,
                                           java.util.ArrayList<VdcActionParametersBase> parameters, boolean isInternal) {
        super(actionType, parameters, isInternal);
    }

    protected abstract void SortCommands();

    @Override
    protected void RunCommands() {
        SortCommands();
        super.RunCommands();
    }
}
