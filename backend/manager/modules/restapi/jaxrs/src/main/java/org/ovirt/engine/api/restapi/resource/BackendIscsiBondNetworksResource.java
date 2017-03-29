package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendIscsiBondNetworksResource
    extends AbstractBackendCollectionResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    // TODO: Replace this with a specific interface for iSCSI bond networks.
    implements NetworksResource {

    private Guid bondId;

    public BackendIscsiBondNetworksResource(Guid bondId) {
        super(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
        this.bondId = bondId;
    }

    @Override
    public Networks list() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = getBackendCollection(
            VdcQueryType.GetNetworksByIscsiBondId,
            new IdQueryParameters(bondId)
        );
        return mapCollection(entities);
    }

    private Networks mapCollection(List<org.ovirt.engine.core.common.businessentities.network.Network> entities) {
        Networks collection = new Networks();
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : entities) {
            collection.getNetworks().add(addLinks(map(entity)));
        }
        return collection;
    }

    @Override
    public Response add(Network network) {
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        Guid networkId = entity.getId();
        IscsiBond bond = getBond();
        bond.getNetworkIds().add(networkId);
        return performCreate(
            VdcActionType.EditIscsiBond,
            new EditIscsiBondParameters(bond),
            new AddedNetworkResolver(networkId)
        );
    }

    private class AddedNetworkResolver extends EntityIdResolver<Guid> {
        private Guid guid;

        public AddedNetworkResolver(Guid guid) {
            this.guid = guid;
        }

        @Override
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid ignore)
            throws BackendFailureException {
            return getEntity(
                org.ovirt.engine.core.common.businessentities.network.Network.class,
                VdcQueryType.GetNetworkById,
                new IdQueryParameters(guid),
                guid.toString()
            );
        }
    }

    @Override
    public NetworkResource getNetworkResource(String id) {
        return inject(new BackendIscsiBondNetworkResource(bondId, id));
    }

    @Override
    protected Network addParents(Network model) {
        return model;
    }

    private IscsiBond getBond() {
        return getEntity(
            IscsiBond.class,
            VdcQueryType.GetIscsiBondById,
            new IdQueryParameters(bondId),
            bondId.toString(),
            true
        );
    }
}
