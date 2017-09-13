package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.ExternalNetworkProviderConfiguration;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.ExternalNetworkProviderConfigurationResource;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostExternalNetworkProviderConfigurationResource
        extends AbstractBackendSubResource<ExternalNetworkProviderConfiguration, Provider>
        implements ExternalNetworkProviderConfigurationResource {
    private final Guid hostId;

    public BackendHostExternalNetworkProviderConfigurationResource(String id, Guid hostId) {
        super(id, ExternalNetworkProviderConfiguration.class, Provider.class);
        this.hostId = hostId;
    }

    @Override
    public ExternalNetworkProviderConfiguration get() {
        return performGet(QueryType.GetProviderById, new IdQueryParameters(asGuidOr404(id)), Host.class);
    }

    @Override
    protected ExternalNetworkProviderConfiguration addParents(ExternalNetworkProviderConfiguration model) {
        Host host = new Host();
        model.setHost(host);
        model.getHost().setId(hostId.toString());
        return model;
    }
}
