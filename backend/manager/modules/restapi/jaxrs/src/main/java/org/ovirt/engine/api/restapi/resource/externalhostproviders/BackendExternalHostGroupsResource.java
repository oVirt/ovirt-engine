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

import org.ovirt.engine.api.model.ExternalHostGroup;
import org.ovirt.engine.api.model.ExternalHostGroups;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalHostGroupsResource
        extends AbstractBackendCollectionResource<ExternalHostGroup, org.ovirt.engine.core.common.businessentities.ExternalHostGroup>
        implements ExternalHostGroupsResource {
    private String providerId;

    public BackendExternalHostGroupsResource(String providerId) {
        super(ExternalHostGroup.class,  org.ovirt.engine.core.common.businessentities.ExternalHostGroup.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalHostGroups list() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        return mapCollection(getBackendCollection(VdcQueryType.GetHostGroupsFromExternalProvider, parameters));
    }

    protected ExternalHostGroups mapCollection(
            List<org.ovirt.engine.core.common.businessentities.ExternalHostGroup> entities) {
        ExternalHostGroups collection = new ExternalHostGroups();
        for (org.ovirt.engine.core.common.businessentities.ExternalHostGroup entity : entities) {
            collection.getExternalHostGroups().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected ExternalHostGroup addParents(ExternalHostGroup model) {
        ExternalHostProvider provider = new ExternalHostProvider();
        provider.setId(providerId);
        model.setExternalHostProvider(provider);
        return super.addParents(model);
    }

    @Override
    public ExternalHostGroupResource getGroupResource(String id) {
        return inject(new BackendExternalHostGroupResource(id, providerId));
    }
}
