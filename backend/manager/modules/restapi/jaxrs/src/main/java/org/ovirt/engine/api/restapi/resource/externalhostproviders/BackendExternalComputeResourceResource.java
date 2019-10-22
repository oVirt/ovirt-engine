/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import java.util.List;

import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourceResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalComputeResourceResource
        extends AbstractBackendActionableResource<ExternalComputeResource, org.ovirt.engine.core.common.businessentities.ExternalComputeResource>
        implements ExternalComputeResourceResource {
    private String providerId;

    protected BackendExternalComputeResourceResource(String id, String providerId) {
        super(id, ExternalComputeResource.class, org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalComputeResource get() {
        // Convert the resource identifier to the host group name:
        String name = hex2string(id);

        // The backend query that retrieves the list of hosts groups needs a complete provider instance, the id isn't
        // enough:
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);

        // The backend doesn't have a way to retrieve a host by ide, so we have to iterate them:
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        QueryReturnValue result = runQuery(QueryType.GetComputeResourceFromExternalProvider, parameters);
        List<org.ovirt.engine.core.common.businessentities.ExternalComputeResource> entities =
            result.getReturnValue();
        if (entities != null) {
            for (org.ovirt.engine.core.common.businessentities.ExternalComputeResource entity : entities) {
                if (name.equals(entity.getName())) {
                    return addLinks(populate(map(entity), entity));
                }
            }
        }

        // No luck:
        return notFound();
    }

    @Override
    protected ExternalComputeResource addParents(ExternalComputeResource model) {
        ExternalHostProvider provider = new ExternalHostProvider();
        provider.setId(providerId);
        model.setExternalHostProvider(provider);
        return super.addParents(model);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier isn't a UUID.
        return null;
    }
}
