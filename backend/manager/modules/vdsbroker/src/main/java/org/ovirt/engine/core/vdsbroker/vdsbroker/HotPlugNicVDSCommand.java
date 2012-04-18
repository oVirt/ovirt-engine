package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.HotPlugUnplgNicVDSParameters;
import org.ovirt.engine.core.utils.StringUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class HotPlugNicVDSCommand<P extends HotPlugUnplgNicVDSParameters> extends VdsBrokerCommand<P> {

    protected XmlRpcStruct struct = new XmlRpcStruct();

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
        struct.add("vmId",getParameters().getVmId().toString());
        struct.add("nic", initNicStructure());
    }

    private XmlRpcStruct initNicStructure() {
        XmlRpcStruct map = new XmlRpcStruct();
        VmNetworkInterface nic = getParameters().getNic();
        VmDevice vmDevice = getParameters().getVmDevice();

        map.add("type", "interface");
        map.add("device", "bridge");
        map.add("macAddr", nic.getMacAddress());
        map.add("network", nic.getNetworkName());
        addAddress(map, vmDevice.getAddress());
        map.add("specParams", vmDevice.getSpecParams());
        map.add("nicModel", VmInterfaceType.forValue(nic.getType()).name());
        if (vmDevice.getBootOrder() > 0) {
            map.add("bootOrder", String.valueOf(vmDevice.getBootOrder()));
        }
        return map;
    }

    private void addAddress(XmlRpcStruct map, String address) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(address)) {
            map.add("address", StringUtils.string2Map(getParameters().getVmDevice().getAddress()));
        }
    }

}
