package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;

public class UpdateVmInterfaceVDSCommand extends VdsBrokerCommand<VmNicDeviceVDSParameters> {

    public UpdateVmInterfaceVDSCommand(VmNicDeviceVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().vmUpdateDevice(getParameters().getVm().getId().toString(), initDeviceStructure());
        proceedProxyReturnValue();
    }

    protected Map<String, Object> initDeviceStructure() {
        Map<String, Object> deviceStruct = new HashMap<>();
        deviceStruct.put(VdsProperties.DeviceType, getParameters().getVmDevice().getType().getValue());
        deviceStruct.put(VdsProperties.Alias, getParameters().getVmDevice().getAlias());

        VmNic nic = getParameters().getNic();
        deviceStruct.put(VdsProperties.LINK_ACTIVE, String.valueOf(nic.isLinked()));
        VmDevice vmDevice = getParameters().getVmDevice();

        VmInfoBuilder.addProfileDataToNic(deviceStruct, getParameters().getVm(), vmDevice, nic);

        return deviceStruct;
    }

}
