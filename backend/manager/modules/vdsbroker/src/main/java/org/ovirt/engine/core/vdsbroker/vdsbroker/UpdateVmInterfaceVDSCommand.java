package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class UpdateVmInterfaceVDSCommand extends VdsBrokerCommand<VmNicDeviceVDSParameters> {

    public UpdateVmInterfaceVDSCommand(VmNicDeviceVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().vmUpdateDevice(getParameters().getVm().getId().toString(), initDeviceStructure());
        ProceedProxyReturnValue();
    }

    protected XmlRpcStruct initDeviceStructure() {
        XmlRpcStruct deviceStruct = new XmlRpcStruct();
        deviceStruct.add(VdsProperties.DeviceType, getParameters().getVmDevice().getType());
        deviceStruct.add(VdsProperties.Alias, getParameters().getVmDevice().getAlias());

        VmNetworkInterface nic = getParameters().getNic();
        deviceStruct.add(VdsProperties.network, StringUtils.defaultString(nic.getNetworkName()));
        deviceStruct.add(VdsProperties.linkActive, String.valueOf(nic.isLinked()));
        deviceStruct.add(VdsProperties.portMirroring,
                nic.isPortMirroring() && nic.getNetworkName() != null
                        ? Collections.singletonList(nic.getNetworkName()) : Collections.<String> emptyList());

        return deviceStruct;
    }

}
