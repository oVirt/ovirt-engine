package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.NumaNode;
import org.ovirt.engine.api.resource.HostNumaNodeResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNumaNodeResource
    extends AbstractBackendSubResource<NumaNode, VdsNumaNode>
        implements HostNumaNodeResource {

    private BackendHostNumaNodesResource parent;

    public BackendHostNumaNodeResource(String id, BackendHostNumaNodesResource parent) {
        super(id, NumaNode.class, VdsNumaNode.class);
        this.parent = parent;
    }

    @Override
    protected NumaNode doPopulate(NumaNode model, VdsNumaNode entity) {
        return parent.doPopulate(model, entity);
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver<Guid> resolver = new EntityIdResolver<Guid>() {
            @Override
            public VdsNumaNode lookupEntity(Guid guid)
                    throws BackendFailureException {
                return parent.lookupEntity(guid);
            }
        };
        NumaStatisticalQuery query = new NumaStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
    }

    @Override
    public NumaNode get() {
        return parent.lookupNumaNode(id, false);
    }

    @Override
    protected NumaNode addParents(NumaNode nic) {
        return parent.addParents(nic);
    }
}
