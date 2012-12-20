package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class UpdateVmConsoleDataParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = -389743388009091714L;
    private String consoleUserName;

    public UpdateVmConsoleDataParameters(Guid vmId, String consoleUserName) {
        super(vmId);
        this.consoleUserName = consoleUserName;
    }

    public String getConsoleUserName() {
        return consoleUserName;
    }

    public void setConsoleUserName(String consoleUserName) {
        this.consoleUserName = consoleUserName;
    }

}
