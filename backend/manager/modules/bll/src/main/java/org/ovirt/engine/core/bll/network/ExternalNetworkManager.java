package org.ovirt.engine.core.bll.network;

import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Utility class to help manage external networks, such as deallocate NICs.
 */
public class ExternalNetworkManager {

    private VmNic nic;

    private Network network;

    /**
     * Create a manager for the specific vNIC.
     *
     * @param nic
     *            The vNIC to create a manager for.
     */
    public ExternalNetworkManager(VmNic nic) {
        this.nic = nic;
    }

    /**
     * Create a manager for the specific vNIC with the given network.
     *
     * @param nic
     *            The vNIC to create a manager for.
     * @param network
     *            The network to manage.
     */
    public ExternalNetworkManager(VmNic nic, Network network) {
        this.nic = nic;
        this.network = network;
    }

    private Network getNetwork() {
        if (network == null) {
            network = NetworkHelper.getNetworkByVnicProfileId(nic.getVnicProfileId());
        }

        return network;
    }

    /**
     * Deallocate the vNIC from the external network, if it's attached to a network and the network is indeed an
     * external network (otherwise, nothing is done).
     */
    public void deallocateIfExternal() {
        if (getNetwork() != null && getNetwork().isExternal()) {
            NetworkProviderProxy providerProxy = ProviderProxyFactory.getInstance().create(
                    DbFacade.getInstance().getProviderDao().get(getNetwork().getProvidedBy().getProviderId()));
            providerProxy.deallocate(nic);
        }
    }
}
