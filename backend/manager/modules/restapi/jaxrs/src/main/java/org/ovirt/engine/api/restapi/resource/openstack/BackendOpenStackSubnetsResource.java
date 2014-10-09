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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.model.OpenStackSubnets;
import org.ovirt.engine.api.resource.openstack.OpenStackSubnetResource;
import org.ovirt.engine.api.resource.openstack.OpenStackSubnetsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.SingleEntityResource;
import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.action.ExternalSubnetParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.queries.GetExternalSubnetsOnProviderByExternalNetworkQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackSubnetsResource
        extends AbstractBackendCollectionResource<OpenStackSubnet, ExternalSubnet>
        implements OpenStackSubnetsResource {
    private String providerId;
    private String networkId;

    public BackendOpenStackSubnetsResource(String providerId, String networkId) {
        super(OpenStackSubnet.class, ExternalSubnet.class);
        this.providerId = providerId;
        this.networkId = networkId;
    }

    @Override
    public OpenStackSubnets list() {
        return mapCollection(getBackendCollection());
    }

    private List<ExternalSubnet> getBackendCollection() {
        GetExternalSubnetsOnProviderByExternalNetworkQueryParameters parameters =
                new GetExternalSubnetsOnProviderByExternalNetworkQueryParameters();
        parameters.setProviderId(asGuid(providerId));
        parameters.setNetworkId(networkId);
        return getBackendCollection(VdcQueryType.GetExternalSubnetsOnProviderByExternalNetwork, parameters);
    }

    private OpenStackSubnets mapCollection(List<ExternalSubnet> entities) {
        OpenStackSubnets collection = new OpenStackSubnets();
        for (ExternalSubnet entity : entities) {
            collection.getOpenStackSubnets().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected OpenStackSubnet doPopulate(OpenStackSubnet model, ExternalSubnet entity) {
        return model;
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
    public Response add(OpenStackSubnet subnet) {
        validateEnums(OpenStackSubnet.class, subnet);
        AddExternalSubnetParameters parameters = new AddExternalSubnetParameters();
        parameters.setSubnet(map(subnet));
        parameters.setProviderId(asGuid(providerId));
        parameters.setNetworkId(networkId);
        return performCreate(VdcActionType.AddSubnetToProvider, parameters, new SubnetNameResolver(subnet.getName()));
    }

    private List<ExternalSubnet> getSubnets() {
        GetExternalSubnetsOnProviderByExternalNetworkQueryParameters parameters =
                new GetExternalSubnetsOnProviderByExternalNetworkQueryParameters();
        parameters.setProviderId(asGuid(providerId));
        parameters.setNetworkId(networkId);
        return getBackendCollection(ExternalSubnet.class, VdcQueryType.GetExternalSubnetsOnProviderByExternalNetwork,
                parameters);
    }

    private ExternalSubnet lookupSubnetByName(String name) {
        for (ExternalSubnet subnet : getSubnets()) {
            if (ObjectUtils.equals(subnet.getName(), name)) {
                return subnet;
            }
        }
        return null;
    }

    private ExternalSubnet lookupSubnetById(String id) {
        for (ExternalSubnet subnet : getSubnets()) {
            if (ObjectUtils.equals(subnet.getId(), id)) {
                return subnet;
            }
        }
        return null;
    }

    private class SubnetNameResolver extends EntityResolver<Guid> {
        private final String name;

        public SubnetNameResolver(String name) {
            this.name = name;
        }

        @Override
        public ExternalSubnet lookupEntity(Guid id) throws BackendFailureException {
            return lookupSubnetByName(name);
        }
    }

    @Override
    protected Response performRemove(String id) {
        ExternalSubnet subnet = lookupSubnetById(id);
        if (subnet != null) {
            ExternalSubnetParameters parameters = new ExternalSubnetParameters();
            parameters.setSubnet(subnet);
            return performAction(VdcActionType.RemoveSubnetFromProvider, parameters);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @Override
    @SingleEntityResource
    public OpenStackSubnetResource getOpenStackSubnet(String id) {
        return inject(new BackendOpenStackSubnetResource(providerId, networkId, id));
    }
}
