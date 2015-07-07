package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostDevListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;

public class HostDevListByCapsVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VdsBrokerCommand<P> {

    private HostDevListReturnForXmlRpc hostDevListReturnForXmlRpc;

    public HostDevListByCapsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        hostDevListReturnForXmlRpc = getBroker().hostDevListByCaps();
        proceedProxyReturnValue();
        List<HostDevice> devices = VdsBrokerObjectsBuilder.buildHostDevices(hostDevListReturnForXmlRpc.devices);
        attachHostIdToDevices(devices);
        setReturnValue(devices);
    }

    private void attachHostIdToDevices(List<HostDevice> devices) {
        for (HostDevice device : devices) {
            device.setHostId(getParameters().getVdsId());
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return hostDevListReturnForXmlRpc.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return hostDevListReturnForXmlRpc;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
