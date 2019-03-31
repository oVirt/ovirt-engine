package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class FullListVDSCommand<P extends FullListVDSCommandParameters> extends VdsBrokerCommand<P> {
    private VMListReturn fullVmListReturn;

    public FullListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        fullVmListReturn = getBroker().fullList(getParameters().getVmIds()
                .stream()
                .map(Guid::toString)
                .collect(Collectors.toList()));
        proceedProxyReturnValue();
        Map<String, Object>[] struct = fullVmListReturn.vmList;
        Arrays.stream(struct).forEach(this::changeScsiDeviceToVirtioScsi);
        Arrays.stream(struct).forEach(this::processVmDevicesUpgradeAdjusting);
        setReturnValue(struct);
    }

    @Override
    protected Status getReturnStatus() {
        return fullVmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return fullVmListReturn;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }

    /**
     * Changes the name of 'scsi' controller to 'virtio-scsi' which engine expects.
     */
    private void changeScsiDeviceToVirtioScsi(Map<String, Object> vmStruct) {
        Arrays.stream((Object[]) vmStruct.get(VdsProperties.Devices))
                .map(o -> (Map<String, Object>)o)
                .filter(device -> VdsProperties.Controller.equals(device.get(VdsProperties.Type)))
                .filter(device -> VdsProperties.Scsi.equals(device.get(VdsProperties.Device)))
                .filter(device -> VdsProperties.VirtioScsi.equals(device.get(VdsProperties.Model)))
                .forEach(device -> {
                    device.put(VdsProperties.Device, VdsProperties.VirtioScsi);
                    device.remove(VdsProperties.Model);
                });
    }

    /**
     * Compensate for upgrading inconsistencies between pre domain xml spicevmc devices by ensuring that redir devices have
     * Addresses and by removing redirdev device Addresses.
     */
    private void processVmDevicesUpgradeAdjusting(Map<String, Object> vmStruct) {
        for (Object o: (Object[]) vmStruct.get(VdsProperties.Devices)) {
            Map<String, Object> vdsmDevice = (Map<String, Object>) o;

            if (vdsmDevice.get(VdsProperties.Address) == null &&
                    VmDeviceType.SPICEVMC.getName().equals(vdsmDevice.get(VdsProperties.Device)) &&
                    VmDeviceGeneralType.REDIR.getValue().equals(vdsmDevice.get(VdsProperties.Type))) {

                for (Object oDev: (Object[]) vmStruct.get(VdsProperties.Devices)) {
                    Map<String, Object> vdsmDeviceDev = (Map<String, Object>) oDev;

                    if (vdsmDeviceDev.get(VdsProperties.Address) != null &&
                            VmDeviceType.SPICEVMC.getName().equals(vdsmDevice.get(VdsProperties.Device)) &&
                            VmDeviceGeneralType.REDIRDEV.getValue().equals(vdsmDeviceDev.get(VdsProperties.Type))) {
                        vdsmDevice.put(VdsProperties.Address, vdsmDeviceDev.get(VdsProperties.Address));
                        vdsmDevice.put(VdsProperties.Alias, vdsmDeviceDev.get(VdsProperties.Alias));
                        vdsmDeviceDev.put(VdsProperties.Address, null);
                        break;
                    }
                }
            }
        }
    }
}
