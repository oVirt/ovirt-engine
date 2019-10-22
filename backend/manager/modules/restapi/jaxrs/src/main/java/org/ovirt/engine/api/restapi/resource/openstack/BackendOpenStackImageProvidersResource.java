/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.model.OpenStackImageProviders;
import org.ovirt.engine.api.resource.openstack.OpenstackImageProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImageProvidersResource;
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

public class BackendOpenStackImageProvidersResource
        extends AbstractBackendCollectionResource<OpenStackImageProvider, Provider>
        implements OpenstackImageProvidersResource {

    public BackendOpenStackImageProvidersResource() {
        super(OpenStackImageProvider.class, Provider.class);
    }

    @Override
    public OpenStackImageProviders list() {
        return mapCollection(getBackendCollection());
    }

    private OpenStackImageProviders mapCollection(List<Provider> entities) {
        OpenStackImageProviders collection = new OpenStackImageProviders();
        for (Provider entity : entities) {
            collection.getOpenStackImageProviders().add(addLinks(map(entity)));
        }
        return collection;
    }

    private List<Provider> getBackendCollection() {
        if (isFiltered()) {
            return getBackendCollection(
                QueryType.GetAllProviders,
                new GetAllProvidersParameters(ProviderType.OPENSTACK_IMAGE)
            );
        } else {
            return getBackendCollection(SearchType.Provider, getConstraint());
        }
    }

    private String getConstraint() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Providers: type=");
        buffer.append(ProviderType.OPENSTACK_IMAGE.name());
        String query = QueryHelper.getConstraint(httpHeaders, uriInfo, null, modelType, false);
        if (StringUtils.isNotBlank(query)) {
            buffer.append(String.format(" AND %1$s", query));
        }
        return buffer.toString();
    }

    @Override
    public Response add(OpenStackImageProvider provider) {
        validateParameters(provider, "name");
        return performCreate(
            ActionType.AddProvider,
            new ProviderParameters(map(provider)),
            new QueryIdResolver<Guid>(QueryType.GetProviderById, IdQueryParameters.class)
        );
    }

    @Override
    public OpenstackImageProviderResource getProviderResource(String id) {
        return inject(new BackendOpenStackImageProviderResource(id));
    }
}
