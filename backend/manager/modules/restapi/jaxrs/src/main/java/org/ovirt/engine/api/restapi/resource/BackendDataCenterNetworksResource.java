package org.ovirt.engine.api.restapi.resource;


import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterNetworksResource extends BackendNetworksResource {

    protected Guid dataCenterId;

    public BackendDataCenterNetworksResource(String dataCenterId) {
        super(VdcQueryType.GetNetworksByDataCenterId);
        this.dataCenterId = asGuid(dataCenterId);
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, getRequiredAddFields());
        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(dataCenterId.toString());
        network.setDataCenter(dataCenter);
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        AddNetworkStoragePoolParameters params = getAddParameters(network, entity);
        return performCreate(addAction,
                               params,
                               new DataCenterNetworkIdResolver(network.getName(), params.getStoragePoolId().toString()));
    }

    @Override
    public Networks list() {
        Networks networks = mapCollection(getNetworks());

        for (Network network : networks.getNetworks()) {
            network.setDisplay(null);
        }
        return networks;
    }

    protected List<org.ovirt.engine.core.common.businessentities.network.Network> getNetworks() {
        return getBackendCollection(VdcQueryType.GetNetworksByDataCenterId, getQueryParameters());
    }

    @Override
    protected VdcQueryParametersBase getQueryParameters() {
        return new IdQueryParameters(dataCenterId);
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "name"};
    }

    @Override
    public NetworkResource getNetworkResource(String id) {
        return inject(new BackendDataCenterNetworkResource(id, this));
    }

}
