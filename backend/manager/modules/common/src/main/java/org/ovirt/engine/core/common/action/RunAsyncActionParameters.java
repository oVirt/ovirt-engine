package org.ovirt.engine.core.common.action;

public class RunAsyncActionParameters extends ActionParametersBase {
    private static final long serialVersionUID = -8078914032408357639L;

    private ActionType action;
    private ActionParametersBase actionParameters;

    public RunAsyncActionParameters() {
    }

    public RunAsyncActionParameters(ActionType action, ActionParametersBase actionParams) {
        this.action = action;
        this.actionParameters = actionParams;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public ActionParametersBase getActionParameters() {
        return actionParameters;
    }

    public void setActionParameters(ActionParametersBase actionParams) {
        this.actionParameters = actionParams;
    }
}

