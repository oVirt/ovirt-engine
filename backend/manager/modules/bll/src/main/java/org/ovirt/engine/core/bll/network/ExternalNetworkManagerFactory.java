package org.ovirt.engine.core.bll.network;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.di.Injector;

@Singleton
public class ExternalNetworkManagerFactory {

    /**
     * Create a manager for the specific vNIC.
     *
     * @param nic
     *            The vNIC to create a manager for.
     */
    public ExternalNetworkManager create(VmNic nic) {
        return Injector.injectMembers(new ExternalNetworkManager(nic));
    }

    /**
     * Create a manager for the specific vNIC with the given network.
     *
     * @param nic
     *            The vNIC to create a manager for.
     * @param network
     *            The network to manage.
     */
    public ExternalNetworkManager create(VmNic nic, Network network) {
        return Injector.injectMembers(new ExternalNetworkManager(nic, network));
    }
}
