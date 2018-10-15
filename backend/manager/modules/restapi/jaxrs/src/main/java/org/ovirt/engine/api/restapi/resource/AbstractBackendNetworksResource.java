package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNetworksResource
    extends AbstractBackendCollectionResource<Network, org.ovirt.engine.core.common.businessentities.network.Network> {

    protected QueryType queryType;
    protected ActionType addAction;

    public AbstractBackendNetworksResource(QueryType queryType, ActionType addAction) {
        super(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
        this.queryType = queryType;
        this.addAction = addAction;
    }

    protected abstract QueryParametersBase getQueryParameters();

    protected abstract ActionParametersBase getAddParameters(Network network, org.ovirt.engine.core.common.businessentities.network.Network entity);

    public Networks list() {
        return mapCollection(getBackendCollection(queryType, getQueryParameters()));
    }

    protected Networks mapCollection(List<org.ovirt.engine.core.common.businessentities.network.Network> entities) {
        Networks collection = new Networks();
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : entities) {
            collection.getNetworks().add(addLinks(map(entity)));
        }
        return collection;
    }

    protected Networks mapCollection(List<org.ovirt.engine.core.common.businessentities.network.Network> entities, Class<? extends BaseResource> suggestedParentType) {
        Networks collection = new Networks();
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : entities) {
            collection.getNetworks().add(addLinks(map(entity), suggestedParentType));
        }
        return collection;
    }

    public org.ovirt.engine.core.common.businessentities.network.Network lookupNetwork(Guid id) {
        return lookupNetwork(id, null);
    }

    public org.ovirt.engine.core.common.businessentities.network.Network lookupNetwork(Guid id, String name) {
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : getBackendCollection(queryType, getQueryParameters())) {
            if ((id != null && id.equals(entity.getId())) ||
                (name != null && name.equals(entity.getName()))) {
                return entity;
            }
        }
        return null;
    }

    public org.ovirt.engine.core.common.businessentities.network.Network lookupNetwork(Guid id, String name, String dataCenterId) {
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : getBackendCollection(queryType, getQueryParameters())) {
            if ((id != null && id.equals(entity.getId())) ||
                (name != null && name.equals(entity.getName()))
                && (entity.getDataCenterId()!=null) && entity.getDataCenterId().toString().equals(dataCenterId)) {
                return entity;
            }
        }
        return null;
    }

    public EntityIdResolver<Guid> getNetworkIdResolver() {
        return new NetworkIdResolver();
    }

    protected class NetworkIdResolver extends EntityIdResolver<Guid> {

        protected String name;

        NetworkIdResolver() {}

        NetworkIdResolver(String name) {
            this.name = name;
        }

        @Override
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid id)
                throws BackendFailureException {
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
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid id)
                throws BackendFailureException {
            return lookupNetwork(id, name, dataCenterId);
        }
    }
}
