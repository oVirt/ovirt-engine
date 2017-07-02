package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.UnmanagedNetwork;
import org.ovirt.engine.api.resource.UnmanagedNetworkResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.UnmanagedNetworkParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendUnmanagedNetworkResource extends AbstractBackendSubResource<UnmanagedNetwork,
        org.ovirt.engine.core.common.businessentities.UnmanagedNetwork> implements UnmanagedNetworkResource {

    private final Guid hostId;

    public BackendUnmanagedNetworkResource(String id, Guid hostId) {
        super(id, UnmanagedNetwork.class, org.ovirt.engine.core.common.businessentities.UnmanagedNetwork.class);
        this.hostId = hostId;
    }


    @Override
    public UnmanagedNetwork get() {
        return performGet(QueryType.GetUnmanagedNetworkByHostIdAndName, new UnmanagedNetworkParameters(hostId, id), Host.class);
    }

    @Override
    public Response remove() {
        get();

        HostSetupNetworksParameters parameters = new HostSetupNetworksParameters(hostId);
        parameters.setRemovedUnmanagedNetworks(Collections.singleton(id));
        return performAction(ActionType.HostSetupNetworks, parameters);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier isn't a UUID.
        return null;
    }

    @Override
    protected UnmanagedNetwork addParents(UnmanagedNetwork model) {
        Host host = new Host();
        model.setHost(host);
        model.getHost().setId(hostId.toString());
        if (model.isSetHostNic()) {
            model.getHostNic().setHost(host);
        }

        return model;
    }
}
