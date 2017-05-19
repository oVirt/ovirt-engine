package org.ovirt.engine.core.common.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class VmHostDevicesParameters extends VmOperationParameterBase {

    private List<String> deviceNames;

    public VmHostDevicesParameters(Guid vmId, List<String> deviceNames) {
        super(vmId);
        this.deviceNames = deviceNames;
    }

    public VmHostDevicesParameters(Guid vmId, String... deviceNames) {
        this(vmId, Arrays.asList(deviceNames));
    }

    public VmHostDevicesParameters() {
        this(Guid.Empty, Collections.emptyList());
    }

    public List<String> getDeviceNames() {
        return deviceNames;
    }

    public void setDeviceNames(List<String> deviceNames) {
        this.deviceNames = deviceNames;
    }
}
