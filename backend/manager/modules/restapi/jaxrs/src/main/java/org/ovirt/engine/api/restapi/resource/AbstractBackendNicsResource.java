package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNicsResource
        extends AbstractBackendCollectionResource<Nic, VmNetworkInterface> {

    private Guid parentId;
    private QueryType queryType;

    public AbstractBackendNicsResource(Guid parentId, QueryType queryType) {
        super(Nic.class, VmNetworkInterface.class);
        this.parentId = parentId;
        this.queryType = queryType;
    }

    public VmNetworkInterface lookupEntity(Guid id) {
        for (VmNetworkInterface entity : getBackendCollection(queryType, new IdQueryParameters(parentId))) {
            if (matchEntity(entity, id)) {
                return entity;
            }
        }
        return null;
    }

    protected <T> boolean matchEntity(VmNetworkInterface entity, T id) {
        return id != null && id.equals(entity.getId());
    }

    protected boolean matchEntity(VmNetworkInterface entity, String name) {
        return name != null && name.equals(entity.getName());
    }

    protected class NicResolver extends EntityIdResolver<Guid> {
        private String name;

        NicResolver(String name) {
            this.name = name;
        }

        private VmNetworkInterface lookupEntity(Guid id, String name) {
            for (VmNetworkInterface entity : getBackendCollection(queryType, new IdQueryParameters(parentId))) {
                if (matchEntity(entity, id) || matchEntity(entity, name)) {
                    return entity;
                }
            }
            return null;
        }

        @Override
        public VmNetworkInterface lookupEntity(Guid id) throws BackendFailureException {
            return lookupEntity(id, name);
        }
    }
}
