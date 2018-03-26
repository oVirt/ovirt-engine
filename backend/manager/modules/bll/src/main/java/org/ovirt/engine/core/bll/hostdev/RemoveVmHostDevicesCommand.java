package org.ovirt.engine.core.bll.hostdev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class RemoveVmHostDevicesCommand extends AbstractVmHostDevicesCommand<VmHostDevicesParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    public RemoveVmHostDevicesCommand(VmHostDevicesParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Set<HostDevice> affectedHostDevices = getAffectedHostDevices();
        Map<String, VmHostDevice> existingDevices = getExistingVmHostDevicesByName();

        Map<Integer, List<VmHostDevice>> existingDevicesByIommuGroup = new HashMap<>();

        for (HostDevice hostDevice : affectedHostDevices) {

            boolean shouldRemoveDevice = getPrimaryDeviceNames().contains(hostDevice.getDeviceName());
            boolean deviceExists = existingDevices.containsKey(hostDevice.getDeviceName());

            if (deviceExists) {
                VmHostDevice device = existingDevices.get(hostDevice.getDeviceName());
                existingDevicesByIommuGroup
                        .computeIfAbsent(getIommuGroupKey(hostDevice.getIommuGroup()), k -> new ArrayList<>())
                        .add(device);

                if (shouldRemoveDevice) {
                    // first just set the flag that this device is not required
                    device.setIommuPlaceholder(true);
                }
            }
        }

        List<VmDevice> devicesToRemove = new ArrayList<>();
        List<VmDevice> devicesToUpdate = new ArrayList<>();

        // go through all the affected IOMMU groups and remove those who only contain
        // no longer needed (placeholder) devices
        for (Map.Entry<Integer, List<VmHostDevice>> group : existingDevicesByIommuGroup.entrySet()) {
            List<VmHostDevice> devices = group.getValue();

            // devices without IOMMU group can be safely removed
            boolean noIommuDeviceGroup = group.getKey() == getIommuGroupKey(null);
            if (noIommuDeviceGroup || allPlaceholder(devices)) {
                // all devices in this group became unnecessary, so remove them
                devicesToRemove.addAll(devices);
            } else {
                // some devices in this group are still required so just update the placeholder flag
                devicesToUpdate.addAll(devices);
            }
        }

        vmDeviceDao.removeAllInBatch(devicesToRemove);
        vmDeviceDao.updateAllInBatch(devicesToUpdate);

        setSucceeded(true);
    }

    /**
     * Returns whether all passed in devices are just IOMMU placeholders - i.e. not required by the user
     */
    private static boolean allPlaceholder(Collection<VmHostDevice> devices) {
        return devices.stream().allMatch(VmHostDevice::isIommuPlaceholder);
    }

    private static int getIommuGroupKey(Integer iommuGroup) {
        return iommuGroup == null ? -1 : iommuGroup;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.VM_REMOVE_HOST_DEVICES;
    }

    public List<String> getNamesRemoved() {
        return new ArrayList<>(getPrimaryDeviceNames());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST_DEVICES);
    }
}
