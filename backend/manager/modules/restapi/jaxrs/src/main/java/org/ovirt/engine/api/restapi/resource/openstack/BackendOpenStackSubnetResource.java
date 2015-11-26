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

import java.util.List;
import java.util.Objects;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.resource.openstack.OpenstackSubnetResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.action.ExternalSubnetParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.queries.GetExternalSubnetsOnProviderByExternalNetworkQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackSubnetResource
        extends AbstractBackendActionableResource<OpenStackSubnet, ExternalSubnet>
        implements OpenstackSubnetResource {
    private String providerId;
    private String networkId;
    private BackendOpenStackSubnetsResource parent;

    protected BackendOpenStackSubnetResource(String providerId,
            String networkId,
            String id,
            BackendOpenStackSubnetsResource parent) {
        super(id, OpenStackSubnet.class, ExternalSubnet.class);
        this.providerId = providerId;
        this.networkId = networkId;
        this.parent = parent;
    }

    @Override
    public OpenStackSubnet get() {
        GetExternalSubnetsOnProviderByExternalNetworkQueryParameters parameters =
                new GetExternalSubnetsOnProviderByExternalNetworkQueryParameters();
        parameters.setProviderId(asGuid(providerId));
        parameters.setNetworkId(networkId);
        List<ExternalSubnet> subnets = getBackendCollection(ExternalSubnet.class,
                VdcQueryType.GetExternalSubnetsOnProviderByExternalNetwork, parameters);
        if (subnets != null) {
            for (ExternalSubnet subnet : subnets) {
                if (Objects.equals(subnet.getId(), id)) {
                    return addLinks(populate(map(subnet), subnet));
                }
            }
        }
        return notFound();
    }

    @Override
    protected OpenStackSubnet addParents(OpenStackSubnet subnet) {
        OpenStackNetworkProvider provider = new OpenStackNetworkProvider();
        provider.setId(providerId);
        OpenStackNetwork network = new OpenStackNetwork();
        network.setId(networkId);
        network.setOpenstackNetworkProvider(provider);
        subnet.setOpenstackNetwork(network);
        return super.addParents(subnet);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier of an OpenStack subnet isn't a UUID.
        return null;
    }

    @Override
    public Response remove() {
        ExternalSubnet subnet = lookupSubnetById(id);
        if (subnet != null) {
            ExternalSubnetParameters parameters = new ExternalSubnetParameters();
            parameters.setSubnet(subnet);
            return performAction(VdcActionType.RemoveSubnetFromProvider, parameters);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private ExternalSubnet lookupSubnetById(String id) {
        for (ExternalSubnet subnet : parent.getSubnets()) {
            if (Objects.equals(subnet.getId(), id)) {
                return subnet;
            }
        }
        return null;
    }
}
