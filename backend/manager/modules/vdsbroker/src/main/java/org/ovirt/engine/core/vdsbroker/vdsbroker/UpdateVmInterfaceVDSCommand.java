package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
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

        VmNic nic = getParameters().getNic();
        VnicProfile vnicProfile = null;
        Network network = null;
        if (nic.getVnicProfileId() != null) {
            vnicProfile = getDbFacade().getVnicProfileDao().get(nic.getVnicProfileId());
            if (vnicProfile != null) {
                network = getDbFacade().getNetworkDao().get(vnicProfile.getNetworkId());
            }
        }
        deviceStruct.put(VdsProperties.NETWORK, network == null ? "" : network.getName());
        deviceStruct.put(VdsProperties.LINK_ACTIVE, String.valueOf(nic.isLinked()));
        deviceStruct.put(VdsProperties.PORT_MIRRORING, vnicProfile != null && vnicProfile.isPortMirroring()
                && network != null ? Collections.singletonList(network.getName()) : Collections.<String> emptyList());

        if (vnicProfile != null
                && FeatureSupported.deviceCustomProperties(getParameters().getVm().getVdsGroupCompatibilityVersion())) {
            deviceStruct.put(VdsProperties.Custom, vnicProfile.getCustomProperties());
        }

        return deviceStruct;
    }

}
