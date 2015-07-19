package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ConvertOvaParameters extends ConvertVmParameters {

    private String ovaPath;

    public ConvertOvaParameters() {
    }

    public ConvertOvaParameters(Guid vmId) {
        super(vmId);
    }

    public String getOvaPath() {
        return ovaPath;
    }

    public void setOvaPath(String ovaPath) {
        this.ovaPath = ovaPath;
    }
}
