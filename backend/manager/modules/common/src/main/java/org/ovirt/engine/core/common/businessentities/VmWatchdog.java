package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public class VmWatchdog implements IVdcQueryable {

    private static final long serialVersionUID = -4515288688595577429L;
    Guid vmId;
    Guid id;
    VmWatchdogAction action;
    VmWatchdogType model;

    public VmWatchdog() {
    }

    public VmWatchdog(VmDevice device) {
        setId(device.getDeviceId());
        setVmId(device.getVmId());
        setAction(VmWatchdogAction.getByName((String) device.getSpecParams().get("action")));
        setModel(VmWatchdogType.getByName((String) device.getSpecParams().get("model")));
    }

    @Override
    public Object getQueryableId() {
        return vmId;
    }

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
