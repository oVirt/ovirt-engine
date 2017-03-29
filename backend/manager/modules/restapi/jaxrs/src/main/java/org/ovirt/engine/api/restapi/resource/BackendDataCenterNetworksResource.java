package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.resource.DataCenterNetworkResource;
import org.ovirt.engine.api.resource.DataCenterNetworksResource;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterNetworksResource
    extends AbstractBackendCollectionResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    implements DataCenterNetworksResource {

    private Guid dataCenterId;

    public BackendDataCenterNetworksResource(Guid dataCenterId) {
        super(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
        this.dataCenterId = dataCenterId;
    }

    @Override
    public Networks list() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = getBackendCollection(
            VdcQueryType.GetNetworksByDataCenterId,
            new IdQueryParameters(dataCenterId)
        );
        Networks networks = mapCollection(entities);
        for (Network network : networks.getNetworks()) {
            network.setDisplay(null);
        }
        return networks;
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
        validateParameters(network, "name");
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        AddNetworkStoragePoolParameters parameters = new AddNetworkStoragePoolParameters(
            dataCenterId,
            entity
        );
        if (network.isSetProfileRequired()) {
            parameters.setVnicProfileRequired(network.isProfileRequired());
        }
        return performCreate(
            VdcActionType.AddNetwork,
            parameters,
            new AddedNetworkResolver()
        );
    }

    private class AddedNetworkResolver extends EntityIdResolver<Guid> {
        @Override
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid id)
            throws BackendFailureException {
            return getEntity(
                org.ovirt.engine.core.common.businessentities.network.Network.class,
                VdcQueryType.GetNetworkById,
                new IdQueryParameters(id),
                id.toString()
            );
        }
    }

    @Override
    public DataCenterNetworkResource getNetworkResource(String id) {
        return inject(new BackendDataCenterNetworkResource(id));
    }

    @Override
    protected Network addParents(Network model) {
        Qos qos = model.getQos();
        if (qos != null) {
            qos.setDataCenter(model.getDataCenter());
        }
        return model;
    }
}
