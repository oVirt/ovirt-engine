/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.Map;
import java.util.Set;

import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworks;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworksResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackNetworksResource
        extends AbstractBackendCollectionResource<OpenStackNetwork, Network>
        implements OpenstackNetworksResource {

    private String providerId;

    public BackendOpenStackNetworksResource(String providerId) {
        super(OpenStackNetwork.class, Network.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackNetworks list() {
        return mapCollection(getBackendCollection());
    }

    private Map<Network, Set<Guid>> getBackendCollection() {
        try {
            IdQueryParameters parameters = new IdQueryParameters(asGuid(providerId));
            QueryReturnValue result = runQuery(QueryType.GetAllExternalNetworksOnProvider, parameters);
            if (!result.getSucceeded()) {
                backendFailure(result.getExceptionString());
            }
            return result.getReturnValue();
        } catch(Exception exception) {
            return handleError(exception, false);
        }
    }

    private OpenStackNetworks mapCollection(Map<Network, Set<Guid>> entities) {
        OpenStackNetworks collection = new OpenStackNetworks();
        if (entities != null) {
            for (Map.Entry<Network, Set<Guid>> entry : entities.entrySet()) {
                Network network = entry.getKey();
                collection.getOpenStackNetworks().add(addLinks(populate(map(network), network)));
            }
        }
        return collection;
    }

    @Override
    protected OpenStackNetwork addParents(OpenStackNetwork image) {
        OpenStackNetworkProvider provider = new OpenStackNetworkProvider();
        provider.setId(providerId);
        image.setOpenstackNetworkProvider(provider);
        return super.addParents(image);
    }

    @Override
    public OpenstackNetworkResource getNetworkResource(String id) {
        return inject(new BackendOpenStackNetworkResource(providerId, id));
    }
}
