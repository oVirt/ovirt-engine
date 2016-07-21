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
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalHostProvidersResource
        extends AbstractBackendCollectionResource<ExternalHostProvider, Provider>
        implements ExternalHostProvidersResource {
    static final String[] SUB_COLLECTIONS = {
        "certificates",
        "computeresources",
        "discoveredhosts",
        "hostgroups",
        "hosts",
    };

    public BackendExternalHostProvidersResource() {
        super(ExternalHostProvider.class, Provider.class, SUB_COLLECTIONS);
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
                VdcQueryType.GetAllProviders,
                new GetAllProvidersParameters(ProviderType.FOREMAN), SearchType.Provider
            );
        }
        else {
            return getBackendCollection(SearchType.Provider, getConstraint());
        }
    }

    private String getConstraint() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Providers: type=");
        buffer.append(ProviderType.FOREMAN.name());
        String query = QueryHelper.getConstraint(httpHeaders, uriInfo, null, modelType, false);
        if (StringUtils.isNotBlank(query)) {
            buffer.append(" AND (");
            buffer.append(query);
            buffer.append(")");
        }
        return buffer.toString();
    }

    @Override
    public Response add(ExternalHostProvider provider) {
        validateParameters(provider, "name");
        return performCreate(
            VdcActionType.AddProvider,
            new ProviderParameters(map(provider)),
            new QueryIdResolver<Guid>(VdcQueryType.GetProviderById, IdQueryParameters.class)
        );
    }

    @Override
    public ExternalHostProviderResource getProviderResource(String id) {
        return inject(new BackendExternalHostProviderResource(id));
    }
}
