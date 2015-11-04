package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworkResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

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
        final Network network = LinqUtils.firstOrNull(networks.getNetworks(), new Predicate<Network>() {
            @Override
            public boolean eval(Network network) {
                return network.getId().equals(id);
            }
        });
        if (network == null) {
            notFound();
        }
        return network;
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNetwork() {
        return getEntity(org.ovirt.engine.core.common.businessentities.network.Network.class,
                VdcQueryType.GetNetworkById, new IdQueryParameters(guid), guid.toString());
    }

    public BackendVirtualFunctionAllowedNetworksResource getParent() {
        return parent;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVfsConfigNetwork,
                new VfsConfigLabelParameters(parent.getNicId(), id));
    }
}
