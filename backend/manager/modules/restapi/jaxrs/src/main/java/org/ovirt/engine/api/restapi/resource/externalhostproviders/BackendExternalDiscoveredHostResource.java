/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import java.util.List;

import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalDiscoveredHostResource
        extends AbstractBackendActionableResource<ExternalDiscoveredHost, org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost>
        implements ExternalDiscoveredHostResource {
    private String providerId;

    protected BackendExternalDiscoveredHostResource(String id, String providerId) {
        super(id, ExternalDiscoveredHost.class, org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalDiscoveredHost get() {
        // Convert the resource identifier to the host group name:
        String name = hex2string(id);

        // The backend doesn't have a way to retrieve a host by ide, so we have to iterate them:
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        QueryReturnValue result = runQuery(QueryType.GetDiscoveredHostListFromExternalProvider, parameters);
        List<org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost> entities = result.getReturnValue();
        if (entities != null) {
            for (org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost entity : entities) {
                if (name.equals(entity.getName())) {
                    return addLinks(populate(map(entity), entity));
                }
            }
        }

        // No luck:
        return notFound();
    }

    @Override
    protected ExternalDiscoveredHost addParents(ExternalDiscoveredHost image) {
        ExternalHostProvider provider = new ExternalHostProvider();
        provider.setId(providerId);
        image.setExternalHostProvider(provider);
        return super.addParents(image);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier isn't a UUID.
        return null;
    }
}
