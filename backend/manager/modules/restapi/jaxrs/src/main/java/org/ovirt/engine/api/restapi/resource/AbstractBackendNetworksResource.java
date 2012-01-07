package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNetworksResource extends AbstractBackendCollectionResource<Network, network>
{

    protected VdcQueryType queryType;
    protected VdcActionType addAction;
    protected VdcActionType removeAction;

    public AbstractBackendNetworksResource(VdcQueryType queryType, VdcActionType addAction, VdcActionType removeAction) {
        super(Network.class, network.class);
        this.queryType = queryType;
        this.addAction = addAction;
        this.removeAction = removeAction;
    }

    protected abstract VdcQueryParametersBase getQueryParameters();

    protected abstract VdcActionParametersBase getActionParameters(Network network, network entity);

    public Networks list() {
        return mapCollection(getBackendCollection(queryType, getQueryParameters()));
    }

    public Response performRemove(String id) {
        network entity = lookupNetwork(asGuidOr404(id));
        if (entity == null) {
            notFound();
            return null;
        }
        return performAction(removeAction, getActionParameters(null, entity));
    }

    protected Networks mapCollection(List<network> entities) {
        Networks collection = new Networks();
        for (network entity : entities) {
            collection.getNetworks().add(addLinks(map(entity)));
        }
        return collection;
    }

    public network lookupNetwork(Guid id) {
        return lookupNetwork(id, null);
    }

    public network lookupNetwork(Guid id, String name) {
        for (network entity : getBackendCollection(queryType, getQueryParameters())) {
            if ((id != null && id.equals(entity.getId())) ||
                (name != null && name.equals(entity.getname()))) {
                return entity;
            }
        }
        return null;
    }

    public network lookupNetwork(Guid id, String name, String dataCenterId) {
        for (network entity : getBackendCollection(queryType, getQueryParameters())) {
            if ((id != null && id.equals(entity.getId())) ||
                (name != null && name.equals(entity.getname()))
                && (entity.getstorage_pool_id()!=null) && (entity.getstorage_pool_id().toString().equals(dataCenterId))) {
                return entity;
            }
        }
        return null;
    }

    public EntityIdResolver getNetworkIdResolver() {
        return new NetworkIdResolver();
    }

    protected class NetworkIdResolver extends EntityIdResolver {

        protected String name;

        NetworkIdResolver() {}

        NetworkIdResolver(String name) {
            this.name = name;
        }

        @Override
        public network lookupEntity(Guid id) throws BackendFailureException {
            return lookupNetwork(id, name);
        }
    }

    protected class DataCenterNetworkIdResolver extends NetworkIdResolver {

        private String dataCenterId;

        DataCenterNetworkIdResolver(String name, String dataCenterId) {
            super(name);
            this.dataCenterId = dataCenterId;
        }

        @Override
        public network lookupEntity(Guid id) throws BackendFailureException {
            return lookupNetwork(id, name, dataCenterId);
        }
    }
}
