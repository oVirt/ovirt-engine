package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VirtualNumaNode;
import org.ovirt.engine.api.model.VirtualNumaNodes;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmNumaNodeResource;
import org.ovirt.engine.api.resource.VmNumaNodesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNumaNodesResource
        extends AbstractBackendCollectionResource<VirtualNumaNode, VmNumaNode>
        implements VmNumaNodesResource {

    private ActionType addAction;
    private ActionType updateType;
    protected Class<VirtualNumaNodes> collectionType;
    protected Guid parentId;
    protected QueryType queryType;
    protected QueryParametersBase queryParams;

    public BackendVmNumaNodesResource(Guid parentId) {
        super(VirtualNumaNode.class, VmNumaNode.class);
        this.addAction = ActionType.AddVmNumaNodes;
        this.updateType = ActionType.UpdateVmNumaNodes;
        this.collectionType = VirtualNumaNodes.class;
        this.parentId = parentId;
        this.queryType = QueryType.GetVmNumaNodesByVmId;
        this.queryParams = new IdQueryParameters(parentId);
    }

    private boolean matchEntity(VmNumaNode entity, Guid id) {
        return id != null && id.equals(entity.getId());
    }

    private String[] getRequiredAddFields() {
        return new String[] { "index", "memory", "cpu" };
    }

    private String[] getRequiredUpdateFields() {
        return new String[0];
    }

    private ActionParametersBase getAddParameters(VmNumaNode entity, VirtualNumaNode device) {
        VmNumaNodeOperationParameters parameters = new VmNumaNodeOperationParameters(parentId, map(device, entity));
        return parameters;
    }

    private ParametersProvider<VirtualNumaNode, VmNumaNode> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    private class UpdateParametersProvider implements ParametersProvider<VirtualNumaNode, VmNumaNode> {
        @Override
        public ActionParametersBase getParameters(VirtualNumaNode incoming, VmNumaNode entity) {
            return new VmNumaNodeOperationParameters(parentId, map(incoming, entity));
        }
    }

    protected VmNumaNode lookupEntity(Guid id) {
        for (VmNumaNode entity : getBackendCollection(queryType, queryParams)) {
            if (matchEntity(entity, id)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public VirtualNumaNode addParents(VirtualNumaNode node) {
        node.setVm(new Vm());
        node.getVm().setId(parentId.toString());
        return node;
    }

    @Override
    public VirtualNumaNodes list() {
        return mapCollection(getBackendCollection(queryType, queryParams));
    }

    protected VirtualNumaNodes mapCollection(List<VmNumaNode> entities) {
        VirtualNumaNodes collection = instantiate(collectionType);
        List<VirtualNumaNode> list = collection.getVirtualNumaNodes();
        for (VmNumaNode entity : entities) {
            VirtualNumaNode candidate = populate(map(entity), entity);
            candidate = addLinks(candidate);
            list.add(candidate);
        }
        return collection;
    }

    @Override
    public VmNumaNodeResource getNodeResource(String id) {
        return inject(new BackendVmNumaNodeResource(id,
                this,
                updateType,
                getUpdateParametersProvider(),
                getRequiredUpdateFields()));
    }

    @Override
    public Response add(VirtualNumaNode node) {
        validateParameters(node, getRequiredAddFields());
        return performCreate(addAction,
                getAddParameters(map(node), node),
                getEntityIdResolver(node.getName()));
    }

    public EntityIdResolver<Guid> getEntityIdResolver(String name) {
        return new NodeIdResolver();
    }

    protected class NodeIdResolver extends EntityIdResolver<Guid> {

        @Override
        public VmNumaNode lookupEntity(Guid id) throws BackendFailureException {
            for (VmNumaNode entity : getBackendCollection(queryType, queryParams)) {
                if (matchEntity(entity, id)) {
                    return entity;
                }
            }
            return null;
        }
    }
}
