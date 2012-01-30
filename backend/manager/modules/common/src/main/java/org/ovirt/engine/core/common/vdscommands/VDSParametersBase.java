package org.ovirt.engine.core.common.vdscommands;

public class VDSParametersBase {

    private boolean runAsync = true;

    public VDSParametersBase() {
    }

    public boolean getRunAsync() {
        return runAsync;
    }

    public void setRunAsync(boolean value) {
        runAsync = value;
    }

}
