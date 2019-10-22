/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworkProviders;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProvidersResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackNetworkProvidersResource
        extends AbstractBackendCollectionResource<OpenStackNetworkProvider, Provider>
        implements OpenstackNetworkProvidersResource {

    public BackendOpenStackNetworkProvidersResource() {
        super(OpenStackNetworkProvider.class, Provider.class);
    }

    @Override
    public OpenStackNetworkProviders list() {
        return mapCollection(getBackendCollection());
    }

    private OpenStackNetworkProviders mapCollection(List<Provider> entities) {
        OpenStackNetworkProviders collection = new OpenStackNetworkProviders();
        for (Provider entity : entities) {
            collection.getOpenStackNetworkProviders().add(addLinks(map(entity)));
        }
        return collection;
    }

    private List<Provider> getBackendCollection() {
        if (isFiltered()) {
            return getBackendCollection(
                QueryType.GetAllProviders,
                new GetAllProvidersParameters(ProviderType.OPENSTACK_NETWORK, ProviderType.EXTERNAL_NETWORK)
            );
        } else {
            List<Provider> openstackCollection = getBackendCollection(SearchType.Provider, getConstraint(ProviderType.OPENSTACK_NETWORK.name()));
            List<Provider> externalCollection =  getBackendCollection(SearchType.Provider, getConstraint(ProviderType.EXTERNAL_NETWORK.name()));
            openstackCollection.addAll(externalCollection);
            return openstackCollection;
        }
    }

    private String getConstraint(String providerType) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Providers: type=");
        buffer.append(providerType);
        String query = QueryHelper.getConstraint(httpHeaders, uriInfo, null, modelType, false);
        if (StringUtils.isNotBlank(query)) {
            buffer.append(String.format(" AND %1$s", query));
        }
        return buffer.toString();
    }

    @Override
    public Response add(OpenStackNetworkProvider provider) {
        validateParameters(provider, "name");
        return performCreate(
            ActionType.AddProvider,
            new ProviderParameters(map(provider)),
            new QueryIdResolver<Guid>(QueryType.GetProviderById, IdQueryParameters.class)
        );
    }

    @Override
    public OpenstackNetworkProviderResource getProviderResource(String id) {
        return inject(new BackendOpenStackNetworkProviderResource(id));
    }
}
