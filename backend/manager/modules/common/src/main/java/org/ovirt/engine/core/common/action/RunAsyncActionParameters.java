package org.ovirt.engine.core.common.action;

public class RunAsyncActionParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -8078914032408357639L;

    private ActionType action;
    private VdcActionParametersBase actionParameters;

    public RunAsyncActionParameters() {
    }

    public RunAsyncActionParameters(ActionType action, VdcActionParametersBase actionParams) {
        this.action = action;
        this.actionParameters = actionParams;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public VdcActionParametersBase getActionParameters() {
        return actionParameters;
    }

    public void setActionParameters(VdcActionParametersBase actionParams) {
        this.actionParameters = actionParams;
    }
}

