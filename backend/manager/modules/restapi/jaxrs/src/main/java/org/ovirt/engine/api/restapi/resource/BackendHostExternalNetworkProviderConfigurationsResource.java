package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.ExternalNetworkProviderConfiguration;
import org.ovirt.engine.api.model.ExternalNetworkProviderConfigurations;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.ExternalNetworkProviderConfigurationResource;
import org.ovirt.engine.api.resource.ExternalNetworkProviderConfigurationsResource;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostExternalNetworkProviderConfigurationsResource extends AbstractBackendCollectionResource<
        ExternalNetworkProviderConfiguration, Provider>
        implements ExternalNetworkProviderConfigurationsResource {
    private final Guid hostId;
    public BackendHostExternalNetworkProviderConfigurationsResource(Guid hostId) {
        super(ExternalNetworkProviderConfiguration.class, Provider.class);
        this.hostId = hostId;
    }

    @Override
    public ExternalNetworkProviderConfigurations list() {
        return mapCollection(getBackendCollection());
    }

    private List<Provider> getBackendCollection() {

        List<Provider> externalNetworkProviders = new ArrayList<>();

        VDS host = getEntity(VDS.class, QueryType.GetVdsByVdsId, new IdQueryParameters(hostId), hostId.toString(),
                true);
        QueryReturnValue result = runQuery(QueryType.GetClusterById, new IdQueryParameters(host.getClusterId()));
        if (result != null && result.getSucceeded() && result.getReturnValue() != null) {
            Cluster cluster = result.getReturnValue();
            Guid externalNetworkProviderId = cluster.getDefaultNetworkProviderId();
            if (externalNetworkProviderId != null) {
                externalNetworkProviders.add(getEntity(Provider.class, QueryType.GetProviderById,
                        new IdQueryParameters(externalNetworkProviderId), externalNetworkProviderId.toString()));
            }
        }

        return externalNetworkProviders;
    }

    private ExternalNetworkProviderConfigurations mapCollection(List<Provider> externalNetworkProviders) {
        ExternalNetworkProviderConfigurations result = new ExternalNetworkProviderConfigurations();
        for(Provider externalNetworkProvider: externalNetworkProviders) {
            result.getExternalNetworkProviderConfigurations().add(addLinks(populate(map(externalNetworkProvider), null), Host.class));
        }
        return result;
    }

    @Override
    protected ExternalNetworkProviderConfiguration addParents(ExternalNetworkProviderConfiguration model) {
        Host host = new Host();
        model.setHost(host);
        model.getHost().setId(hostId.toString());
        return model;
    }

    @Override
    public ExternalNetworkProviderConfigurationResource getConfigurationResource(String id) {
        return inject(new BackendHostExternalNetworkProviderConfigurationResource(HexUtils.hex2string(id), hostId));
    }
}
