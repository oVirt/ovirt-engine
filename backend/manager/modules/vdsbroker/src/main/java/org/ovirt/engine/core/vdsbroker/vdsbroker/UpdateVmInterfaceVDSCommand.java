package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;

public class UpdateVmInterfaceVDSCommand extends VdsBrokerCommand<VmNicDeviceVDSParameters> {

    public UpdateVmInterfaceVDSCommand(VmNicDeviceVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().vmUpdateDevice(getParameters().getVm().getId().toString(), initDeviceStructure());
        ProceedProxyReturnValue();
    }

    protected Map<String, Object> initDeviceStructure() {
        Map<String, Object> deviceStruct = new HashMap<String, Object>();
        deviceStruct.put(VdsProperties.DeviceType, getParameters().getVmDevice().getType().getValue());
        deviceStruct.put(VdsProperties.Alias, getParameters().getVmDevice().getAlias());

        VmNetworkInterface nic = getParameters().getNic();
        deviceStruct.put(VdsProperties.NETWORK, StringUtils.defaultString(nic.getNetworkName()));
        deviceStruct.put(VdsProperties.LINK_ACTIVE, String.valueOf(nic.isLinked()));
        deviceStruct.put(VdsProperties.PORT_MIRRORING,
                nic.isPortMirroring() && nic.getNetworkName() != null
                        ? Collections.singletonList(nic.getNetworkName()) : Collections.<String> emptyList());

        return deviceStruct;
    }

}
