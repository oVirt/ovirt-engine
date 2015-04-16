package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

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

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb.append("runAsync", runAsync);
    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
