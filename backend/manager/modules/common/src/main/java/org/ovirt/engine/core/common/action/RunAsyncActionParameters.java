package org.ovirt.engine.core.common.action;

public class RunAsyncActionParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -8078914032408357639L;

    private VdcActionType action;
    private VdcActionParametersBase actionParameters;

    public RunAsyncActionParameters() {
    }

    public RunAsyncActionParameters(VdcActionType action, VdcActionParametersBase actionParams) {
        this.action = action;
        this.actionParameters = actionParams;
    }

    public VdcActionType getAction() {
        return action;
    }

    public void setAction(VdcActionType action) {
        this.action = action;
    }

    public VdcActionParametersBase getActionParameters() {
        return actionParameters;
    }

    public void setActionParameters(VdcActionParametersBase actionParams) {
        this.actionParameters = actionParams;
    }
}

