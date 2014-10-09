/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworkProviders;
import org.ovirt.engine.api.resource.openstack.OpenStackNetworkProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenStackNetworkProvidersResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.SingleEntityResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackNetworkProvidersResource
        extends AbstractBackendCollectionResource<OpenStackNetworkProvider, Provider>
        implements OpenStackNetworkProvidersResource {
    static final String[] SUB_COLLECTIONS = {
        "networks",
        "certificates"
    };

    public BackendOpenStackNetworkProvidersResource() {
        super(OpenStackNetworkProvider.class, Provider.class, SUB_COLLECTIONS);
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
                VdcQueryType.GetAllProviders,
                new GetAllProvidersParameters(ProviderType.OPENSTACK_NETWORK)
            );
        }
        else {
            return getBackendCollection(SearchType.Provider, getConstraint());
        }
    }

    private String getConstraint() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Providers: type=");
        buffer.append(ProviderType.OPENSTACK_NETWORK.name());
        String query = QueryHelper.getConstraint(getUriInfo(), null, modelType, false);
        if (StringUtils.isNotBlank(query)) {
            buffer.append(" AND (");
            buffer.append(query);
            buffer.append(")");
        }
        return buffer.toString();
    }

    @Override
    public Response add(OpenStackNetworkProvider provider) {
        validateParameters(provider, "name");
        validateEnums(OpenStackNetworkProvider.class, provider);
        return performCreate(
            VdcActionType.AddProvider,
            new ProviderParameters(map(provider)),
            new QueryIdResolver<Guid>(VdcQueryType.GetProviderById, IdQueryParameters.class)
        );
    }

    @Override
    protected Response performRemove(String id) {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(VdcActionType.RemoveProvider, parameters);
    }

    @Override
    protected OpenStackNetworkProvider doPopulate(OpenStackNetworkProvider model, Provider entity) {
        return model;
    }

    @Override
    @SingleEntityResource
    public OpenStackNetworkProviderResource getOpenStackNetworkProvider(@PathParam("id") String id) {
        return inject(new BackendOpenStackNetworkProviderResource(id));
    }
}
