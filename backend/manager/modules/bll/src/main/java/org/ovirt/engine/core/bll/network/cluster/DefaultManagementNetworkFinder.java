package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;

public interface DefaultManagementNetworkFinder {

    /**
     * Finds the default management network for the given DC.
     *
     * @param dataCenterId
     *            data center id
     * @return the default management {@link Network}
     */
    Network findDefaultManagementNetwork(Guid dataCenterId);

}
