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

import static org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackNetworksResource.SUB_COLLECTIONS;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.resource.openstack.OpenStackNetworkResource;
import org.ovirt.engine.api.resource.openstack.OpenStackSubnetsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackNetworkResource
        extends AbstractBackendActionableResource<OpenStackNetwork, Network>
        implements OpenStackNetworkResource {
    private String providerId;

    protected BackendOpenStackNetworkResource(String providerId, String id) {
        super(id, OpenStackNetwork.class, Network.class, SUB_COLLECTIONS);
        this.providerId = providerId;
    }

    @Override
    public OpenStackNetwork get() {
        // The backend doesn't provide a mechanism to get a specific network from a provider, so we have to get all and
        // then iterate to find the requested one:
        Network network = null;
        try {
            IdQueryParameters parameters = new IdQueryParameters(asGuid(providerId));
            VdcQueryReturnValue result = runQuery(VdcQueryType.GetAllExternalNetworksOnProvider, parameters);
            if (!result.getSucceeded()) {
                backendFailure(result.getExceptionString());
            }
            Map<Network, Set<Guid>> networks = result.getReturnValue();
            if (networks != null) {
                for (Map.Entry<Network, Set<Guid>> entry : networks.entrySet()) {
                    Network current = entry.getKey();
                    ProviderNetwork providedBy = current.getProvidedBy();
                    if (ObjectUtils.equals(providedBy.getExternalId(), id)) {
                        network = current;
                        break;
                    }
                }
            }
        }
        catch (Exception exception) {
            return handleError(exception, false);
        }
        if (network == null) {
            notFound();
        }
        return addLinks(populate(map(network), network));
    }

    @Override
    protected OpenStackNetwork doPopulate(OpenStackNetwork model, Network entity) {
        return model;
    }

    @Override
    protected OpenStackNetwork addParents(OpenStackNetwork network) {
        OpenStackNetworkProvider provider = new OpenStackNetworkProvider();
        provider.setId(providerId);
        network.setOpenstackNetworkProvider(provider);
        return super.addParents(network);
    }

    @Override
    public OpenStackSubnetsResource getOpenStackSubnets() {
        return inject(new BackendOpenStackSubnetsResource(providerId, id));
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier of an OpenStack image isn't a UUID.
        return null;
    }
}
