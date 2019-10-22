/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import java.util.List;

import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalHostResource
        extends AbstractBackendActionableResource<ExternalHost, VDS>
        implements ExternalHostResource {
    private String providerId;

    protected BackendExternalHostResource(String id, String providerId) {
        super(id, ExternalHost.class, VDS.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalHost get() {
        // Convert the resource identifier to the host group name:
        String name = hex2string(id);

        // The backend doesn't have a way to retrieve a host by ide, so we have to iterate them:
        GetHostListFromExternalProviderParameters parameters = new GetHostListFromExternalProviderParameters();
        parameters.setFilterOutExistingHosts(true);
        parameters.setProviderId(asGuid(providerId));
        QueryReturnValue result = runQuery(QueryType.GetHostListFromExternalProvider, parameters);
        List<VDS> entities = result.getReturnValue();
        if (entities != null) {
            for (VDS entity : entities) {
                if (name.equals(entity.getName())) {
                    return addLinks(populate(map(entity), entity));
                }
            }
        }

        // No luck:
        return notFound();
    }

    @Override
    protected ExternalHost addParents(ExternalHost image) {
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
