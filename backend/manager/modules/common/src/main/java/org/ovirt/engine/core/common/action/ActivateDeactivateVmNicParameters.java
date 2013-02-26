package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ActivateDeactivateVmNicParameters extends VmOperationParameterBase {

    private Guid nicId;
    private PlugAction action;
    private String oldMacAddress;

    public ActivateDeactivateVmNicParameters(Guid nicId, PlugAction action, String oldMacAddress) {
        super();
        this.nicId = nicId;
        this.action = action;
        this.oldMacAddress = oldMacAddress;
    }

    public String getOldMacAddress() {
        return oldMacAddress;
    }

    public void setOldMacAddress(String oldMacAddress) {
        this.oldMacAddress = oldMacAddress;
    }

    public Guid getNicId() {
        return nicId;
    }

    public void setNicId(Guid nicId) {
        this.nicId = nicId;
    }

    public PlugAction getAction() {
        return action;
    }

    public void setAction(PlugAction action) {
        this.action = action;
    }

}
