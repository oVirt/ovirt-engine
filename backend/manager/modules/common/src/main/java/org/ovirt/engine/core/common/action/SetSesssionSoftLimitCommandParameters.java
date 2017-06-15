package org.ovirt.engine.core.common.action;

public class SetSesssionSoftLimitCommandParameters extends ActionParametersBase {

    private static final long serialVersionUID = 6853776483034717926L;

    private int softLimit;

    SetSesssionSoftLimitCommandParameters() {
    }

    public SetSesssionSoftLimitCommandParameters(String engineSessionId, int softLimit) {
        super(engineSessionId);
        this.softLimit = softLimit;
    }

    public int getSoftLimit() {
        return softLimit;
    }

}
