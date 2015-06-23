package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VirtualNumaNode;
import org.ovirt.engine.api.resource.VmNumaNodeResource;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNumaNodeResource
    extends AbstractBackendActionableResource<VirtualNumaNode, VmNumaNode>
        implements VmNumaNodeResource {

    private VdcActionType updateType;
    private ParametersProvider<VirtualNumaNode, VmNumaNode> updateParametersProvider;
    private EntityIdResolver<Guid> entityResolver;
    private String[] requiredUpdateFields;
    private BackendVmNumaNodesResource collection;
    private VdcActionType removeAction;

    public BackendVmNumaNodeResource(String id,
            final BackendVmNumaNodesResource collection,
            VdcActionType updateType,
            AbstractBackendSubResource.ParametersProvider<VirtualNumaNode, VmNumaNode> updateParametersProvider,
            String[] requiredUpdateFields) {
        super(id, VirtualNumaNode.class, VmNumaNode.class);
        this.updateType = updateType;
        this.updateParametersProvider = updateParametersProvider;
        this.requiredUpdateFields = requiredUpdateFields;
        this.collection = collection;
        this.removeAction = VdcActionType.RemoveVmNumaNodes;
        entityResolver = new EntityIdResolver<Guid>() {
            @Override
            public VmNumaNode lookupEntity(Guid id) throws BackendFailureException {
                return collection.lookupEntity(guid);
            }
        };
    }

    @Override
    public VirtualNumaNode update(VirtualNumaNode node) {
        validateParameters(node, requiredUpdateFields);
        return performUpdate(node, entityResolver, updateType, updateParametersProvider);
    }

    @Override
    public VirtualNumaNode get() {
        VmNumaNode entity = collection.lookupEntity(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(populate(map(entity), entity));
    }

    @Override
    public VirtualNumaNode addParents(VirtualNumaNode node) {
        return collection.addParents(node);
    }

    @Override
    public Response remove() {
        get();
        return performAction(removeAction, getRemoveParameters());
    }

    private VdcActionParametersBase getRemoveParameters() {
        VmNumaNode entity = new VmNumaNode();
        entity.setId(guid);
        VmNumaNodeOperationParameters parameters = new VmNumaNodeOperationParameters(collection.parentId, entity);
        return parameters;
    }
}
