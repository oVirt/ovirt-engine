package org.ovirt.engine.core.bll.hostdev;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddVmHostDevicesCommand extends AbstractVmHostDevicesCommand<VmHostDevicesParameters> {

    private List<String> namesAdded;

    public AddVmHostDevicesCommand(VmHostDevicesParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        List<VmDevice> addDevicesBatch = getAddDevicesBatch();
        getVmDeviceDao().saveAllInBatch(addDevicesBatch);
        setSucceeded(true);
        setActionReturnValue(namesAdded);
    }

    private List<VmDevice> getAddDevicesBatch() {
        namesAdded = new ArrayList<>();
        Set<HostDevice> affectedHostDevices = getAffectedHostDevices();
        Map<String, VmDevice> existingDevices = getExistingVmHostDevicesByName();
        List<VmDevice> devicesToAdd = new ArrayList<>();
        for (HostDevice hostDevice : affectedHostDevices) {
            if (!existingDevices.containsKey(hostDevice.getDeviceName())) {
                devicesToAdd.add(new VmHostDevice(getVmId(), hostDevice));
                namesAdded.add(hostDevice.getDeviceName());
            }
        }
        return devicesToAdd;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.VM_ADD_HOST_DEVICES;
    }

    public List<String> getNamesAdded() {
        return namesAdded;
    }
}

