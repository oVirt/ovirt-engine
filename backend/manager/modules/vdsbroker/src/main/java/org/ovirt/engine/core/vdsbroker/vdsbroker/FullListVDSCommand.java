package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
}
