package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;

public class RemoveNetworkVDSCommand<P extends NetworkVdsmVDSCommandParameters> extends VdsBrokerCommand<P> {
    public RemoveNetworkVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        String network = (getParameters().getNetworkName() == null) ? "" : getParameters()
                .getNetworkName();
        String vlanId = (getParameters().getVlanId() != null) ? getParameters().getVlanId().toString()
                : "";
        String bond = (getParameters().getBondName() == null) ? "" : getParameters().getBondName();
        String[] nics = (getParameters().getNics() == null) ? new String[] {} : getParameters().getNics();

        status = getBroker().delNetwork(network, vlanId, bond, nics);
        ProceedProxyReturnValue();
    }
}
