package org.ovirt.engine.core.common.vdscommands;

public class VDSParametersBase {

    private boolean runAsync;

    public VDSParametersBase() {
        runAsync = true;
    }

    public boolean getRunAsync() {
        return runAsync;
    }

    public void setRunAsync(boolean value) {
        runAsync = value;
    }

}
