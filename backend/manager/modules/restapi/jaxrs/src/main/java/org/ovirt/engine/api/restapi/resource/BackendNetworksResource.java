package org.ovirt.engine.api.restapi.resource;


import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworksResource extends AbstractBackendNetworksResource implements NetworksResource {

    static final String[] SUB_COLLECTIONS = { "permissions", "vnicprofiles", "labels" };

    public BackendNetworksResource() {
        this(VdcQueryType.GetAllNetworks);
    }

    public BackendNetworksResource(VdcQueryType queryType) {
        super(queryType, VdcActionType.AddNetwork, VdcActionType.RemoveNetwork, SUB_COLLECTIONS);
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, getRequiredAddFields());
        validateEnums(Network.class, network);
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        AddNetworkStoragePoolParameters params = getAddParameters(network, entity);
        return performCreate(addAction,
                               params,
                               new DataCenterNetworkIdResolver(network.getName(), params.getStoragePoolId().toString()));
    }

    @Override
    public Networks list() {
        Networks networks;

        if (isFiltered()) {
            networks = mapCollection(getBackendCollection(queryType, getQueryParameters(), SearchType.Network));
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
    protected AddNetworkStoragePoolParameters getAddParameters(Network network,
            org.ovirt.engine.core.common.businessentities.network.Network entity) {
        if (namedDataCenter(network)) {
            entity.setDataCenterId(getDataCenterId(network));
        }

        AddNetworkStoragePoolParameters parameters =
                new AddNetworkStoragePoolParameters(entity.getDataCenterId(), entity);
        if (network != null && network.isSetProfileRequired()) {
            parameters.setVnicProfileRequired(network.isProfileRequired());
        }

        return parameters;
    }

    @Override
    protected VdcActionParametersBase getRemoveParameters(org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new RemoveNetworkParameters(entity.getId());
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
        String networkName = network.getDataCenter().getName();
        return getEntity(StoragePool.class, VdcQueryType.GetStoragePoolByDatacenterName,
                new NameQueryParameters(networkName), "Datacenter: name="
                        + networkName).getId();

    }

    @Override
    protected Network doPopulate(Network model, org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return model;
    }
}
