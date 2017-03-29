package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworksResource
    extends AbstractBackendCollectionResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    implements NetworksResource {

    public BackendNetworksResource() {
        super(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
    }

    @Override
    public Networks list() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities;
        if (isFiltered()) {
            IdQueryParameters parameters = new IdQueryParameters(Guid.Empty);
            entities = getBackendCollection(VdcQueryType.GetAllNetworks, parameters, SearchType.Network);
        }
        else {
            entities = getBackendCollection(SearchType.Network);
        }
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
        validateParameters(network, "name", "dataCenter.name|id");
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        if (namedDataCenter(network)) {
            entity.setDataCenterId(getDataCenterId(network));
        }
        AddNetworkStoragePoolParameters parameters = new AddNetworkStoragePoolParameters(
            entity.getDataCenterId(),
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
    public NetworkResource getNetworkResource(String id) {
        return inject(new BackendNetworkResource(id));
    }

    @Override
    protected Network addParents(Network model) {
        return BackendNetworkHelper.addParents(model);
    }

    private boolean namedDataCenter(Network network) {
        return network != null && network.isSetDataCenter() && network.getDataCenter().isSetName() && !network.getDataCenter().isSetId();
    }

    private Guid getDataCenterId(Network network) {
        String networkName = network.getDataCenter().getName();
        StoragePool dataCenter = getEntity(
            StoragePool.class,
            VdcQueryType.GetStoragePoolByDatacenterName,
            new NameQueryParameters(networkName),
            networkName,
            true
        );
        return dataCenter.getId();
    }
}
