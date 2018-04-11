package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostDevListReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;

@Logged(executionLevel = Logged.LogLevel.DEBUG)
public class HostDevListByCapsVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    private HostDevListReturn hostDevListReturn;

    public HostDevListByCapsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        hostDevListReturn = getBroker().hostDevListByCaps();
        proceedProxyReturnValue();
        List<HostDevice> devices = vdsBrokerObjectsBuilder.buildHostDevices(hostDevListReturn.devices);
        attachHostIdToDevices(devices);
        setReturnValue(devices);
    }

    private void attachHostIdToDevices(List<HostDevice> devices) {
        for (HostDevice device : devices) {
            device.setHostId(getParameters().getVdsId());
        }
    }

    @Override
    protected Status getReturnStatus() {
        return hostDevListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return hostDevListReturn;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
