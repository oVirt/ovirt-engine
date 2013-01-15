package org.ovirt.engine.core.bll.provider.network;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public interface NetworkProviderProxy extends ProviderProxy {

    /**
     * Retrieve a list of all the networks that this provider can provide.
     *
     * @return A list of the networks available from the provider.
     */
    List<Network> getAll();

    /**
     * Allocate the vNIC on the network in the provider.
     *
     * @param network
     *            The network to allocate the vNIC on.
     * @param nic
     *            The vNIC to allocate.
     *
     * @return A map of custom properties to forward for the vNIC device. The correct driver will know how to handle
     *         these properties, and connect the vNIC correctly.
     */
    Map<String, String> allocate(Network network, VmNetworkInterface nic);

    /**
     * Deallocate the vNIC from the provider. If the vNIC is not on the provider anymore, don't throw an exception.
     *
     * @param nic
     *            The vNIC to deallocate.
     */
    void deallocate(VmNetworkInterface nic);
}
