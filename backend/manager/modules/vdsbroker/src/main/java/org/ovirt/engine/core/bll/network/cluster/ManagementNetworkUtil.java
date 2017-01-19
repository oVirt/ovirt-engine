package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;

// TODO: move the interface to more suitable for that module as soon as it will be possible (CDI cross module issue
// is solved).
public interface ManagementNetworkUtil {

    /**
     * The method retrieves the management network for the given cluster.
     *
     * @param clusterId
     *            the given cluster id
     * @return {@link Network} that is defined as the management one in the given cluster
     */
    Network getManagementNetwork(Guid clusterId);

    /**
     * The method checks if the given network is defined as the management network for any cluster.
     *
     * @param networkId
     *            the given network id
     * @return true if exists a cluster where the network is defined as the management one,
     *         false otherwise
     */
    boolean isManagementNetwork(Guid networkId);

    /**
     * The method checks if the given network is defined as the management network for the given cluster.
     *
     * @param networkId
     *            the given network id
     * @param clusterId
     *            the given cluster id
     * @return true if the network is defined as the management one for the given cluster,
     *         false otherwise
     */
    boolean isManagementNetwork(Guid networkId, Guid clusterId);

    /**
     * The method checks if the given network is defined as the management network for the given cluster.
     *
     * @param networkName
     *            the given network name
     * @param clusterId
     *            the given cluster id
     * @return true if the network is defined as the management one for the given cluster,
     *         false otherwise
     */
    boolean isManagementNetwork(String networkName, Guid clusterId);

    /**
     * Retrieves the default management network name from the {@link org.ovirt.engine.core.common.config.Config}.
     * Should be used in very rare cases - {@link #getManagementNetwork} and {@link #isManagementNetwork} methods
     * provide more appropriate solution for vast majority of the cases.
     *
     * @return the default management network name.
     */
    String getDefaultManagementNetworkName();

    /**
     *
     * @param networks all networks related to clusterId
     * @param clusterId id of cluster
     * @return management network found among passed networks
     */
    Network getManagementNetwork(List<Network> networks, Guid clusterId);
}
