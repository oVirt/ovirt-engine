package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworkResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VfsConfigNetworkParameters;

public class BackendVirtualFunctionAllowedNetworkResource
        extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
        implements VirtualFunctionAllowedNetworkResource {

    private final BackendVirtualFunctionAllowedNetworksResource parent;

    public BackendVirtualFunctionAllowedNetworkResource(String id, BackendVirtualFunctionAllowedNetworksResource parent) {
        super(id, Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);

        this.parent = parent;
    }

    @Override
    public Network get() {
        final Networks networks = getParent().list();
        final Network network = networks.getNetworks().stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
        if (network == null) {
            notFound();
        }
        return network;
    }

    public BackendVirtualFunctionAllowedNetworksResource getParent() {
        return parent;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveVfsConfigNetwork,
                new VfsConfigNetworkParameters(parent.getNicId(), guid));
    }
}
