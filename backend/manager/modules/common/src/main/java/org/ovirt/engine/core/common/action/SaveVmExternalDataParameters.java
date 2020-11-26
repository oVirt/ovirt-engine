package org.ovirt.engine.core.common.action;

import java.util.function.Supplier;

import org.ovirt.engine.core.compat.Guid;

public class SaveVmExternalDataParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 1L;

    private Supplier<Integer> incrementMethod;
    private boolean forceUpdate;

    public SaveVmExternalDataParameters() {
    }

    public SaveVmExternalDataParameters(Guid vmId, Supplier<Integer> incrementMethod, boolean forceUpdate) {
        super(vmId);
        this.incrementMethod = incrementMethod;
        this.forceUpdate = forceUpdate;
    }

    public Supplier<Integer> getIncrementMethod() {
        return incrementMethod;
    }

    public void setIncrementMethod(Supplier<Integer> incrementMethod) {
        this.incrementMethod = incrementMethod;
    }

    public boolean getForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
