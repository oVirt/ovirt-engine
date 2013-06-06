package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

public class HotPlugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends VdsBrokerCommand<P> {

    protected Map<String, Object> struct = new HashMap<String, Object>();

    public HotPlugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        init();
        status = getBroker().hotPlugNic(struct);
        ProceedProxyReturnValue();
    }

    protected void init() {
        struct.put(VdsProperties.vm_guid, getParameters().getVm().getId().toString());
        struct.put(VdsProperties.VM_NETWORK_INTERFACE, initNicStructure());
    }

    private Map<String, Object> initNicStructure() {
        Map<String, Object> map = new HashMap<String, Object>();
        VmNetworkInterface nic = getParameters().getNic();
        VmDevice vmDevice = getParameters().getVmDevice();

        map.put(VdsProperties.Type, vmDevice.getType().getValue());
        map.put(VdsProperties.Device, VmDeviceType.BRIDGE.getName());
        map.put(VdsProperties.MAC_ADDR, nic.getMacAddress());
        map.put(VdsProperties.NETWORK, StringUtils.defaultString(nic.getNetworkName()));

        if (FeatureSupported.networkLinking(getParameters().getVm().getVdsGroupCompatibilityVersion())) {
            map.put(VdsProperties.LINK_ACTIVE, String.valueOf(nic.isLinked()));
        }
        addAddress(map, vmDevice.getAddress());
        map.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
        map.put(VdsProperties.NIC_TYPE, VmInterfaceType.forValue(nic.getType()).name());
        map.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        if (vmDevice.getBootOrder() > 0) {
            map.put(VdsProperties.BootOrder, String.valueOf(vmDevice.getBootOrder()));
        }

        if (nic.isPortMirroring()) {
            map.put(VdsProperties.PORT_MIRRORING, nic.getNetworkName() == null
                    ? Collections.<String> emptyList() : Collections.singletonList(nic.getNetworkName()));
        }

        if (FeatureSupported.deviceCustomProperties(getParameters().getVm().getVdsGroupCompatibilityVersion())) {
            map.put(VdsProperties.Custom, vmDevice.getCustomProperties());
        }

        VmInfoBuilder.addNetworkFiltersToNic(map, getParameters().getVm().getVdsGroupCompatibilityVersion());
        return map;
    }

    private void addAddress(Map<String, Object> map, String address) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(address)) {
            map.put(VdsProperties.Address, XmlRpcStringUtils.string2Map(getParameters().getVmDevice().getAddress()));
        }
    }

}
