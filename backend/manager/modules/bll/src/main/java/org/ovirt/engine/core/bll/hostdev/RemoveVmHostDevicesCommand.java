package org.ovirt.engine.core.bll.hostdev;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemoveVmHostDevicesCommand extends AbstractVmHostDevicesCommand<VmHostDevicesParameters> {

    private List<String> namesRemoved;

    public RemoveVmHostDevicesCommand(VmHostDevicesParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        List<VmDeviceId> removeDeviceBatch = getRemoveDeviceBatch();
        getVmDeviceDao().removeAll(removeDeviceBatch);
        setSucceeded(true);
        setActionReturnValue(namesRemoved);
    }

    private List<VmDeviceId> getRemoveDeviceBatch() {
        namesRemoved = new ArrayList<>();
        Set<HostDevice> affectedHostDevices = getAffectedHostDevices();
        Map<String, VmDevice> existingDevices = getExistingVmHostDevicesByName();
        List<VmDeviceId> devicesToRemove = new ArrayList<>();
        for (HostDevice hostDevice : affectedHostDevices) {
            if (existingDevices.containsKey(hostDevice.getDeviceName())) {
                devicesToRemove.add(existingDevices.get(hostDevice.getDeviceName()).getId());
                namesRemoved.add(hostDevice.getDeviceName());
            }
        }
        return devicesToRemove;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.VM_REMOVE_HOST_DEVICES;
    }

    public List<String> getNamesRemoved() {
        return namesRemoved;
    }
}
