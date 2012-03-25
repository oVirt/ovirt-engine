package org.ovirt.engine.core.common;

import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.compat.Guid;

public class HotPlugUnplugVmNicParameters extends VmOperationParameterBase {

    private Guid nicId;
    private PlugAction action;

    public HotPlugUnplugVmNicParameters(Guid nicId, PlugAction action) {
        super();
        this.nicId = nicId;
        this.action = action;
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
