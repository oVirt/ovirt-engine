package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ConsolePopupModel extends Model {

    private VmConsoles vmConsoles;

    public ConsolePopupModel() {
        setTitle(ConstantsManager.getInstance().getConstants().consoleOptions());
    }

    public VmConsoles getVmConsoles() {
        return vmConsoles;
    }

    public void setVmConsoles(VmConsoles vmConsoles) {
        this.vmConsoles = vmConsoles;
    }

}
