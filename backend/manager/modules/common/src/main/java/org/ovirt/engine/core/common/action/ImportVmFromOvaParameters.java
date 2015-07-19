package org.ovirt.engine.core.common.action;

public class ImportVmFromOvaParameters extends ImportVmFromExternalProviderParameters {

    private String ovaPath;

    public String getOvaPath() {
        return ovaPath;
    }

    public void setOvaPath(String ovaPath) {
        this.ovaPath = ovaPath;
    }
}
