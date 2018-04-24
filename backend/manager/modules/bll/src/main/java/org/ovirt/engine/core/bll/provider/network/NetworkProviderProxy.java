package org.ovirt.engine.core.bll.provider.network;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;

public interface NetworkProviderProxy extends ProviderProxy {

    /**
     * Add the network to the provider, who is responsible to provide the requested network.
     *
     * @param network
     *            The network to add to the provider.
     * @return The external ID of the added network.
     */
    String add(Network network);

    /**
     * Remove the network from the external provider.
     *
     * @param id
     *            The external ID of the network to remove.
     */
    void remove(String id);

    /**
     * Retrieve a list of all the networks that this provider can provide.
     *
     * @return A list of the networks available from the provider.
     */
    List<Network> getAll();

    /**
     * Retrieve a network associated with given network id.
     *
     * @param id External id of the network
     * @return External network with given id.
     */
    Network get(String id);

    /**
     * Retrieve a list of all subnets that are associated with the given network.
     *
     * @param network
     *            The network to get the subnets for.
     * @return A list of all subnets for the network.
     */
    List<ExternalSubnet> getAllSubnets(ProviderNetwork network);

    /**
     * Add the subnet to be managed by the provider.
     *
     * @param subnet
     *            The subnet to add.
     */
    void addSubnet(ExternalSubnet subnet);

    /**
     * Remove the subnet from the provider.
     *
     * @param id
     *            The ID of the subnet to remove.
     */
    void removeSubnet(String id);

    /**
     * Allocate the vNIC on the network in the provider.
     *
     * @param network
     *            The network to allocate the vNIC on.
     * @param vnicProfile
     *            The vNIC profile that connects the vNIC to the network.
     * @param nic
     *            The vNIC to allocate.
     * @param host
     *            The host to schedule the vm on
     * @return A map of custom properties to forward for the vNIC device. The correct driver will know how to handle
     *         these properties, and connect the vNIC correctly.
     */
    Map<String, String> allocate(Network network, VnicProfile vnicProfile, VmNic nic, VDS host,
                                 boolean ignoreSecurityGroups, String hostBindingId);

    /**
     * Deallocate the vNIC from the provider. If the vNIC is not on the provider anymore, don't throw an exception.
     *
     * @param nic
     *            The vNIC to deallocate.
     */
    void deallocate(VmNic nic);
}
