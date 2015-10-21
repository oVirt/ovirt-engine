package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

public class HotPlugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends VdsBrokerCommand<P> {

    protected Map<String, Object> struct = new HashMap<String, Object>();

    public HotPlugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        init();
        status = getBroker().hotPlugNic(struct);
        proceedProxyReturnValue();
    }

    protected void init() {
        struct.put(VdsProperties.vm_guid, getParameters().getVm().getId().toString());
        struct.put(VdsProperties.VM_NETWORK_INTERFACE, initNicStructure());
    }

    private Map<String, Object> initNicStructure() {
        Map<String, Object> map = new HashMap<String, Object>();
        VmNic nic = getParameters().getNic();
        VmDevice vmDevice = getParameters().getVmDevice();
        Version clusterVersion = getParameters().getVm().getVdsGroupCompatibilityVersion();
        map.put(VdsProperties.Type, vmDevice.getType().getValue());
        map.put(VdsProperties.Device, VmDeviceType.BRIDGE.getName());
        map.put(VdsProperties.MAC_ADDR, nic.getMacAddress());

        if (FeatureSupported.networkLinking(clusterVersion)) {
            map.put(VdsProperties.LINK_ACTIVE, String.valueOf(nic.isLinked()));
        }

        addAddress(map, vmDevice.getAddress());
        map.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
        map.put(VdsProperties.NIC_TYPE,
                VmInfoBuilder.evaluateInterfaceType(VmInterfaceType.forValue(nic.getType()), getParameters().getVm()
                        .getHasAgent()));
        map.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        VmInfoBuilder.addProfileDataToNic(map, getParameters().getVm(), vmDevice, nic);
        VmInfoBuilder.addNetworkFiltersToNic(map, clusterVersion);
        return map;
    }

    private void addAddress(Map<String, Object> map, String address) {
        if (StringUtils.isNotBlank(address)) {
            map.put(VdsProperties.Address, XmlRpcStringUtils.string2Map(getParameters().getVmDevice().getAddress()));
        }
    }

}
