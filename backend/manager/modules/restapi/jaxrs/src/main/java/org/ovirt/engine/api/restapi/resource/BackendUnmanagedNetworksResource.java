package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.UnmanagedNetwork;
import org.ovirt.engine.api.model.UnmanagedNetworks;
import org.ovirt.engine.api.resource.UnmanagedNetworkResource;
import org.ovirt.engine.api.resource.UnmanagedNetworksResource;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendUnmanagedNetworksResource extends AbstractBackendCollectionResource<UnmanagedNetwork, org.ovirt.engine.core.common.businessentities.UnmanagedNetwork>
        implements UnmanagedNetworksResource {

    private Guid hostId;

    public BackendUnmanagedNetworksResource(Guid hostId) {
        super(UnmanagedNetwork.class, org.ovirt.engine.core.common.businessentities.UnmanagedNetwork.class);
        this.hostId = hostId;
    }

    @Override
    public UnmanagedNetworks list() {
        verifyIfHostExistsToHandle404StatusCode();
        return mapCollection(getAllUnmanagedNetworksByVdsId());
    }

    private void verifyIfHostExistsToHandle404StatusCode() {
        getEntity(VDS.class, QueryType.GetVdsByVdsId, new IdQueryParameters(hostId), hostId.toString(), true);
    }

    private List<org.ovirt.engine.core.common.businessentities.UnmanagedNetwork> getAllUnmanagedNetworksByVdsId() {
        return getBackendCollection(QueryType.GetAllUnmanagedNetworksByHostId, new IdQueryParameters(hostId));
    }

    private UnmanagedNetworks mapCollection(List<org.ovirt.engine.core.common.businessentities.UnmanagedNetwork> unmanagedNetworks) {
        UnmanagedNetworks result = new UnmanagedNetworks();
        for (org.ovirt.engine.core.common.businessentities.UnmanagedNetwork unmanagedNetwork : unmanagedNetworks) {
            result.getUnmanagedNetworks().add(addLinks(populate(map(unmanagedNetwork), null), Host.class));
        }
        return result;
    }

    @Override
    public UnmanagedNetworkResource getUnmanagedNetworkResource(String id) {
        return inject(new BackendUnmanagedNetworkResource(HexUtils.hex2string(id), hostId));
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
