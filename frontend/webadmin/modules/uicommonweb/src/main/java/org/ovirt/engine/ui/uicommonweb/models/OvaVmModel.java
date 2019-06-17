package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.VM;

/**
 * Combines together ova file name and the VM read from that ova file
 *
 */
public class OvaVmModel extends EntityModel<VM> {

    private String ovaFileName;

    public OvaVmModel() {
        super();
    }

    public OvaVmModel(String ovaFileName, VM vm) {
        super(vm);
        this.ovaFileName = ovaFileName;
    }

    public void setOvaFileName(String ovaFileName) {
        this.ovaFileName = ovaFileName;
    }

    public String getOvaFileName() {
        return ovaFileName;
    }
}
