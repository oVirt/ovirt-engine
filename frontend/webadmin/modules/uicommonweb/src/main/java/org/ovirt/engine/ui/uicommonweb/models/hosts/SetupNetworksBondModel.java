package org.ovirt.engine.ui.uicommonweb.models.hosts;

public class SetupNetworksBondModel extends HostBondInterfaceModel {

    public SetupNetworksBondModel() {
        getNetwork().setIsAvailable(false);
        getCheckConnectivity().setIsAvailable(false);
        getCommitChanges().setIsAvailable(false);
        getAddress().setIsAvailable(false);
        getSubnet().setIsAvailable(false);
        getGateway().setIsAvailable(false);
        setBootProtocolAvailable(false);
    }

}
