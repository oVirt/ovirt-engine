/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.model.ExternalHostProviders;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProviderResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProvidersResource;
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

public class BackendExternalHostProvidersResource
        extends AbstractBackendCollectionResource<ExternalHostProvider, Provider>
        implements ExternalHostProvidersResource {

    public BackendExternalHostProvidersResource() {
        super(ExternalHostProvider.class, Provider.class);
    }

    @Override
    public ExternalHostProviders list() {
        return mapCollection(getBackendCollection());
    }

    private ExternalHostProviders mapCollection(List<Provider> entities) {
        ExternalHostProviders collection = new ExternalHostProviders();
        for (Provider entity : entities) {
            collection.getExternalHostProviders().add(addLinks(map(entity)));
        }
        return collection;
    }

    private List<Provider> getBackendCollection() {
        if (isFiltered()) {
            return getBackendCollection(
                QueryType.GetAllProviders,
                new GetAllProvidersParameters(ProviderType.FOREMAN), SearchType.Provider
            );
        } else {
            return getBackendCollection(SearchType.Provider, getConstraint());
        }
    }

    private String getConstraint() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Providers: type=");
        buffer.append(ProviderType.FOREMAN.name());
        String query = QueryHelper.getConstraint(httpHeaders, uriInfo, null, modelType, false);
        if (StringUtils.isNotBlank(query)) {
            buffer.append(String.format(" AND %1$s", query));
        }
        return buffer.toString();
    }

    @Override
    public Response add(ExternalHostProvider provider) {
        validateParameters(provider, "name");
        return performCreate(
            ActionType.AddProvider,
            new ProviderParameters(map(provider)),
            new QueryIdResolver<Guid>(QueryType.GetProviderById, IdQueryParameters.class)
        );
    }

    @Override
    public ExternalHostProviderResource getProviderResource(String id) {
        return inject(new BackendExternalHostProviderResource(id));
    }
}
