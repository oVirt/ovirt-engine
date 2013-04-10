package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmWatchdog extends IVdcQueryable implements Serializable {
    public Object getQueryableId() {
        return vmId;
    }

    /**
     *
     */
    private static final long serialVersionUID = -4515288688595577429L;
    Guid vmId;
    Guid id;
    VmWatchdogAction action;
    VmWatchdogType model;

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public VmWatchdogAction getAction() {
        return action;
    }

    public void setAction(VmWatchdogAction action) {
        this.action = action;
    }

    public VmWatchdogType getModel() {
        return model;
    }

    public void setModel(VmWatchdogType model) {
        this.model = model;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }
}
