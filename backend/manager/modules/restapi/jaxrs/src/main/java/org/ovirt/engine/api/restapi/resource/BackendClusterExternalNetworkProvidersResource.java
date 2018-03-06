package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.model.ExternalProviders;
import org.ovirt.engine.api.resource.ClusterExternalProvidersResource;
import org.ovirt.engine.api.restapi.types.openstack.OpenStackNetworkProviderMapper;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterExternalNetworkProvidersResource extends AbstractBackendCollectionResource<ExternalProvider, Provider>
        implements ClusterExternalProvidersResource {

    private Guid clusterId;

    public BackendClusterExternalNetworkProvidersResource(Guid clusterId) {
        super(ExternalProvider.class, Provider.class);
        this.clusterId = clusterId;
    }

    @Override
    public ExternalProviders list() {
        return mapCollection(getBackendCollection());
    }

    private ExternalProviders mapCollection(List<Provider> entities) {
        ExternalProviders collection = new ExternalProviders();
        for (Provider entity : entities) {
            collection.getExternalProviders().add(addLinks(OpenStackNetworkProviderMapper.map(entity, null)));
        }
        return collection;
    }

    private List<Provider> getBackendCollection() {

        List<Provider> providers = new ArrayList<>();

        Cluster cluster = getEntity(Cluster.class, QueryType.GetClusterById,
                new IdQueryParameters(clusterId), clusterId.toString(), true);

        Guid defaultNetworkProviderId = cluster.getDefaultNetworkProviderId();
        if (cluster.isSetDefaultNetworkProviderId()) {
            providers.add(getEntity(Provider.class, QueryType.GetProviderById,
                    new IdQueryParameters(defaultNetworkProviderId), defaultNetworkProviderId.toString()));
        }
        return providers;
    }
}
