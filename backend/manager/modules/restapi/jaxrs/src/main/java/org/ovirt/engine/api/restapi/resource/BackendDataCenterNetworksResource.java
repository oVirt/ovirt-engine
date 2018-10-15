package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.DataCenterNetworkResource;
import org.ovirt.engine.api.resource.DataCenterNetworksResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterNetworksResource
    extends AbstractBackendNetworksResource
    implements DataCenterNetworksResource {

    protected Guid dataCenterId;

    public BackendDataCenterNetworksResource(String dataCenterId) {
        super(QueryType.GetNetworksByDataCenterId, ActionType.AddNetwork);
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
        return performCreate(network.isSetExternalProvider() ? ActionType.AddNetworkOnProvider : addAction,
                               params,
                               new DataCenterNetworkIdResolver(network.getName(), params.getStoragePoolId().toString()));
    }

    @Override
    public Networks list() {
        Networks networks = mapCollection(getNetworks(), LinkHelper.NO_PARENT);

        for (Network network : networks.getNetworks()) {
            network.setDisplay(null);
        }
        return networks;
    }

    protected List<org.ovirt.engine.core.common.businessentities.network.Network> getNetworks() {
        return getBackendCollection(QueryType.GetNetworksByDataCenterId, getQueryParameters());
    }

    @Override
    protected QueryParametersBase getQueryParameters() {
        return new IdQueryParameters(dataCenterId);
    }

    @Override
    protected AddNetworkStoragePoolParameters getAddParameters(
            Network network,
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

    protected String[] getRequiredAddFields() {
        return new String[] { "name"};
    }

    @Override
    public DataCenterNetworkResource getNetworkResource(String id) {
        return inject(new BackendDataCenterNetworkResource(id, this));
    }

    protected boolean namedDataCenter(Network network) {
        return network != null && network.isSetDataCenter() && network.getDataCenter().isSetName() && !network.getDataCenter().isSetId();
    }

    protected Guid getDataCenterId(Network network) {
        String networkName = network.getDataCenter().getName();
        return getEntity(
            StoragePool.class,
            QueryType.GetStoragePoolByDatacenterName,
            new NameQueryParameters(networkName),
            "Datacenter: name=" + networkName
        ).getId();
    }
}
