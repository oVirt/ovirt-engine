package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworkResource;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworksResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VfsConfigNetworkParameters;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.compat.Guid;

public class BackendVirtualFunctionAllowedNetworksResource
        extends AbstractBackendCollectionResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
        implements VirtualFunctionAllowedNetworksResource {

    private Guid nicId;
    private String hostId;

    protected BackendVirtualFunctionAllowedNetworksResource(Guid nicId, String hostId) {
        super(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);

        this.nicId = nicId;
        this.hostId = hostId;
    }

    @Override
    public Networks list() {
        final Set<Guid> networkIds = loadVfAllowedNetworks();
        final Networks networks = new Networks();
        for (Guid networkId : networkIds) {
            final Network network = new Network();
            network.setId(networkId.toString());
            networks.getNetworks().add(network);
            addLinks(network);
        }
        return networks;
    }

    private Set<Guid> loadVfAllowedNetworks() {
        final BackendHostNicsResource hostNicsResource = getParent();
        final HostNicVfsConfig vfsConfig = hostNicsResource.findVfsConfig(nicId);
        if (vfsConfig == null) {
            return Collections.emptySet();
        }
        final Set<Guid> networkIds = vfsConfig.getNetworks();
        return networkIds;
    }

    @Override
    public Response add(Network networkModel) {
        validateParameters(networkModel, "id|name");

        final org.ovirt.engine.core.common.businessentities.network.Network network = findNetwork(networkModel);

        return performAction(ActionType.AddVfsConfigNetwork, new VfsConfigNetworkParameters(nicId, network.getId()));
    }

    @Override
    public VirtualFunctionAllowedNetworkResource getNetworkResource(String id) {
        return inject(new BackendVirtualFunctionAllowedNetworkResource(id, this));
    }

    private org.ovirt.engine.core.common.businessentities.network.Network findNetwork(Network network) {
        return getParent().lookupNetwork(network);
    }

    private BackendHostNicsResource getParent() {
        return inject(new BackendHostNicsResource(hostId));
    }

    Guid getNicId() {
        return nicId;
    }

}
