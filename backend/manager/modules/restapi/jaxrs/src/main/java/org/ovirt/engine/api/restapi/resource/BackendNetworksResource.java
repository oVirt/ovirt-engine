package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworksResource
    extends AbstractBackendNetworksResource
    implements NetworksResource {

    public BackendNetworksResource() {
        this(QueryType.GetAllNetworks);
    }

    public BackendNetworksResource(QueryType queryType) {
        super(queryType, ActionType.AddNetwork);
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, getRequiredAddFields());
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        AddNetworkStoragePoolParameters params = getAddParameters(network, entity);
        return performCreate(network.isSetExternalProvider() ? ActionType.AddNetworkOnProvider : addAction,
                               params,
                               new DataCenterNetworkIdResolver(network.getName(), params.getStoragePoolId().toString()));
    }

    @Override
    public Networks list() {
        Networks networks;
        // Specifying LinkHelper.NO_PARENT to explicitly point link to API root:
        //   <network href=".../api/networks/xxx">
        // rather than under datacenter:
        //   <network href=".../api/datacenters/yyy/networks/xxx">
        //
        // If LinkHelper.NO_PARENT were not specified, the href including the
        // datacenter would be seleced by default because the the network spec
        // includes the datacenter-id.
        if (isFiltered()) {
            networks = mapCollection(getBackendCollection(queryType, getQueryParameters(), SearchType.Network), LinkHelper.NO_PARENT);
        } else {
            networks = mapCollection(getBackendCollection(SearchType.Network), LinkHelper.NO_PARENT);
        }

        for (Network network : networks.getNetworks()) {
            network.setDisplay(null);
        }
        return networks;
    }

    @Override
    protected Network addParents(Network model) {
        Qos qos = model.getQos();
        if (qos != null) {
            qos.setDataCenter(model.getDataCenter());
        }

        return model;
    }

    @Override
    protected QueryParametersBase getQueryParameters() {
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

    protected String[] getRequiredAddFields() {
        return new String[] { "name", "dataCenter.name|id" };
    }

    @Override
    public NetworkResource getNetworkResource(String id) {
        return inject(new BackendNetworkResource(id, this));
    }

    protected boolean namedDataCenter(Network network) {
        return network != null && network.isSetDataCenter() && network.getDataCenter().isSetName() && !network.getDataCenter().isSetId();
    }

    protected Guid getDataCenterId(Network network) {
        String networkName = network.getDataCenter().getName();
        return getEntity(StoragePool.class, QueryType.GetStoragePoolByDatacenterName,
                new NameQueryParameters(networkName), "Datacenter: name="
                        + networkName).getId();

    }
}
