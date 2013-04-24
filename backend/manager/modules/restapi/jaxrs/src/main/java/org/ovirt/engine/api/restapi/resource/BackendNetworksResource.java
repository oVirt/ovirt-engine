package org.ovirt.engine.api.restapi.resource;


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
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworksResource extends AbstractBackendNetworksResource implements NetworksResource {

    static final String[] SUB_COLLECTIONS = {"permissions"};

    public BackendNetworksResource() {
        super(VdcQueryType.GetAllNetworks, VdcActionType.AddNetwork, VdcActionType.RemoveNetwork, SUB_COLLECTIONS);
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, getRequiredAddFields());
        validateEnums(Network.class, network);
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        AddNetworkStoragePoolParameters params = getActionParameters(network, entity);
        return performCreate(addAction,
                               params,
                               new DataCenterNetworkIdResolver(network.getName(), params.getStoragePoolId().toString()));
    }

    @Override
    public Networks list() {
        Networks networks;

        if (isFiltered()) {
            networks = mapCollection(getBackendCollection(queryType, getQueryParameters()));
        } else {
            networks = mapCollection(getBackendCollection(SearchType.Network));
        }

        for (Network network : networks.getNetworks()) {
            network.setDisplay(null);
        }
        return networks;
    }

    @Override
    protected VdcQueryParametersBase getQueryParameters() {
        return new IdQueryParameters(Guid.Empty);
    }

    @Override
    protected AddNetworkStoragePoolParameters getActionParameters(Network network, org.ovirt.engine.core.common.businessentities.network.Network entity) {
        if (namedDataCenter(network)) {
            entity.setDataCenterId(getDataCenterId(network));
        }
        return new AddNetworkStoragePoolParameters(entity.getDataCenterId().getValue(), entity);
    }

    protected String[] getRequiredAddFields() {
        return new String[] { "name", "dataCenter.name|id" };
    }

    @Override
    @SingleEntityResource
    public NetworkResource getNetworkSubResource(String id) {
        return inject(new BackendNetworkResource(id, this));
    }

    protected boolean namedDataCenter(Network network) {
        return network != null && network.isSetDataCenter() && network.getDataCenter().isSetName() && !network.getDataCenter().isSetId();
    }

    protected Guid getDataCenterId(Network network) {
        return getEntity(StoragePool.class,
                         SearchType.StoragePool,
                         "Datacenter: name=" + network.getDataCenter().getName()).getId();
    }

    @Override
    protected Network doPopulate(Network model, org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return model;
    }
}
