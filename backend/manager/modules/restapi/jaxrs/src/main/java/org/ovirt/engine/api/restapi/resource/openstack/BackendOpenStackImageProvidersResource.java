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
import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.model.OpenStackImageProviders;
import org.ovirt.engine.api.resource.openstack.OpenStackImageProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenStackImageProvidersResource;
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

public class BackendOpenStackImageProvidersResource
        extends AbstractBackendCollectionResource<OpenStackImageProvider, Provider>
        implements OpenStackImageProvidersResource {
    static final String[] SUB_COLLECTIONS = {
        "images",
        "certificates"
    };

    public BackendOpenStackImageProvidersResource() {
        super(OpenStackImageProvider.class, Provider.class, SUB_COLLECTIONS);
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
                VdcQueryType.GetAllProviders,
                new GetAllProvidersParameters(ProviderType.OPENSTACK_IMAGE)
            );
        }
        else {
            return getBackendCollection(SearchType.Provider, getConstraint());
        }
    }

    private String getConstraint() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Providers: type=");
        buffer.append(ProviderType.OPENSTACK_IMAGE.name());
        String query = QueryHelper.getConstraint(getUriInfo(), null, modelType, false);
        if (StringUtils.isNotBlank(query)) {
            buffer.append(" AND (");
            buffer.append(query);
            buffer.append(")");
        }
        return buffer.toString();
    }

    @Override
    public Response add(OpenStackImageProvider provider) {
        validateParameters(provider, "name");
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
    protected OpenStackImageProvider doPopulate(OpenStackImageProvider model, Provider entity) {
        return model;
    }

    @Override
    @SingleEntityResource
    public OpenStackImageProviderResource getOpenStackImageProvider(@PathParam("id") String id) {
        return inject(new BackendOpenStackImageProviderResource(id));
    }
}
