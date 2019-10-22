/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.model.OpenStackSubnets;
import org.ovirt.engine.api.resource.openstack.OpenstackSubnetResource;
import org.ovirt.engine.api.resource.openstack.OpenstackSubnetsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.queries.GetExternalSubnetsOnProviderByExternalNetworkQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackSubnetsResource
        extends AbstractBackendCollectionResource<OpenStackSubnet, ExternalSubnet>
        implements OpenstackSubnetsResource {
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
        return getBackendCollection(QueryType.GetExternalSubnetsOnProviderByExternalNetwork, parameters);
    }

    private OpenStackSubnets mapCollection(List<ExternalSubnet> entities) {
        OpenStackSubnets collection = new OpenStackSubnets();
        for (ExternalSubnet entity : entities) {
            collection.getOpenStackSubnets().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
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
        AddExternalSubnetParameters parameters = new AddExternalSubnetParameters();
        parameters.setSubnet(map(subnet));
        parameters.setProviderId(asGuid(providerId));
        parameters.setNetworkId(networkId);
        return performCreate(ActionType.AddSubnetToProvider, parameters, new SubnetNameResolver(subnet.getName()));
    }

    protected List<ExternalSubnet> getSubnets() {
        GetExternalSubnetsOnProviderByExternalNetworkQueryParameters parameters =
                new GetExternalSubnetsOnProviderByExternalNetworkQueryParameters();
        parameters.setProviderId(asGuid(providerId));
        parameters.setNetworkId(networkId);
        return getBackendCollection(ExternalSubnet.class, QueryType.GetExternalSubnetsOnProviderByExternalNetwork,
                parameters);
    }

    private ExternalSubnet lookupSubnetByName(String name) {
        for (ExternalSubnet subnet : getSubnets()) {
            if (Objects.equals(subnet.getName(), name)) {
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
    public OpenstackSubnetResource getSubnetResource(String id) {
        return inject(new BackendOpenStackSubnetResource(providerId, networkId, id, this));
    }
}
