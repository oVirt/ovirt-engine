package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class ManagementNetworkFinder {

    private final BackendResource backendResource;

    public ManagementNetworkFinder(BackendResource backendResource) {
        this.backendResource = backendResource;
    }

    Guid getManagementNetworkId(Cluster cluster, Guid dataCenterId) {
        Guid managementNetworkId = null;
        if (cluster.isSetManagementNetwork()) {
            backendResource.validateParameters(cluster.getManagementNetwork(), "id|name");
            final Network rawManagementNetwork = cluster.getManagementNetwork();
            if (rawManagementNetwork.isSetId()) {
                managementNetworkId = GuidUtils.asGuid(rawManagementNetwork.getId());
            } else {
                final org.ovirt.engine.core.common.businessentities.network.Network managementNetwork =
                        backendResource.getEntity(org.ovirt.engine.core.common.businessentities.network.Network.class,
                                QueryType.GetNetworkByNameAndDataCenter,
                                new IdAndNameQueryParameters(dataCenterId, rawManagementNetwork.getName()),
                                String.format("Network: %s", rawManagementNetwork.getName()));

                managementNetworkId = managementNetwork.getId();
            }
        }
        return managementNetworkId;
    }
}
