package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

public class VmWatchdog implements Queryable {

    private static final long serialVersionUID = -4515288688595577429L;
    private static final String WATCHDOG_MODEL_PARAM_NAME = "model";
    private static final String WATCHDOG_ACTION_PARAM_NAME = "action";

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

    public VmWatchdog(VmWatchdogType model, VmWatchdogAction action) {
        this.model = model;
        this.action = action;
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

    public VmDevice createVmDevice() {
        Map<String, Object> specParams = getSpecParams();

        return new VmDevice(
                new VmDeviceId(null, id),
                VmDeviceGeneralType.WATCHDOG,
                VmDeviceType.WATCHDOG.getName(),
                "",
                specParams,
                true,
                true,
                false,
                "",
                null,
                null,
                null);
    }

    public Map<String, Object> getSpecParams() {
        Map<String, Object> specParams = new HashMap<>();

        specParams.put(WATCHDOG_MODEL_PARAM_NAME, model.name());
        specParams.put(WATCHDOG_ACTION_PARAM_NAME, action.name().toLowerCase());

        return specParams;
    }
}
