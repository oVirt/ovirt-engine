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
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackNetworksResource
        extends AbstractBackendCollectionResource<OpenStackNetwork, Network>
        implements OpenstackNetworksResource {
    static final String[] SUB_COLLECTIONS = {
        "subnets"
    };

    private String providerId;

    public BackendOpenStackNetworksResource(String providerId) {
        super(OpenStackNetwork.class, Network.class, SUB_COLLECTIONS);
        this.providerId = providerId;
    }

    @Override
    public OpenStackNetworks list() {
        return mapCollection(getBackendCollection());
    }

    private Map<Network, Set<Guid>> getBackendCollection() {
        try {
            IdQueryParameters parameters = new IdQueryParameters(asGuid(providerId));
            VdcQueryReturnValue result = runQuery(VdcQueryType.GetAllExternalNetworksOnProvider, parameters);
            if (!result.getSucceeded()) {
                backendFailure(result.getExceptionString());
            }
            return result.getReturnValue();
        }
        catch (Exception exception) {
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
