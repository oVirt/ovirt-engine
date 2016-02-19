package org.ovirt.engine.core.dao.network;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.qos.QosDao;

public interface HostNetworkQosDao extends QosDao<HostNetworkQos> {


    /**
     * It may be null if there is no {@link HostNetworkQos} associated with the migration network of the cluster.
     */
    HostNetworkQos getHostNetworkQosOfMigrationNetworkByClusterId(Guid clusterId);

    void persistQosChanges(Guid qosId, HostNetworkQos qos);
}
