/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
import org.ovirt.engine.core.common.queries.QueryType;

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
        return mapCollection(getBackendCollection(QueryType.GetHostGroupsFromExternalProvider, parameters));
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
