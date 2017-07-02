package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.NumaNode;
import org.ovirt.engine.api.model.NumaNodes;
import org.ovirt.engine.api.resource.HostNumaNodeResource;
import org.ovirt.engine.api.resource.HostNumaNodesResource;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNumaNodesResource
    extends AbstractBackendCollectionResource<NumaNode, VdsNumaNode>
        implements HostNumaNodesResource {

    private String hostId;

    public BackendHostNumaNodesResource(String hostId) {
        super(NumaNode.class, VdsNumaNode.class);
        this.hostId = hostId;
    }

    public String getHostId() {
        return hostId;
    }

    @Override
    public NumaNodes list() {
        NumaNodes ret = new NumaNodes();
        List<VdsNumaNode> nodes = getCollection();
        for (VdsNumaNode node : nodes) {
            NumaNode numanode = populate(map(node, null), node);
            ret.getNumaNodes().add(addLinks(numanode));
        }
        return ret;
    }

    @Override
    public HostNumaNodeResource getNodeResource(String id) {
        return inject(new BackendHostNumaNodeResource(id, this));
    }

    protected List<VdsNumaNode> getCollection() {
        return getBackendCollection(QueryType.GetVdsNumaNodesByVdsId, new IdQueryParameters(asGuid(hostId)));
    }

    @Override
    public NumaNode addParents(NumaNode node) {
        node.setHost(new Host());
        node.getHost().setId(hostId);
        return node;
    }

    public NumaNode lookupNumaNode(String id, boolean forcePopulate) {
        List<VdsNumaNode> nodes = getCollection();
        for (VdsNumaNode node : nodes) {
            if (node.getId().toString().equals(id)) {
                NumaNode numanode = map(node, null);
                if (forcePopulate) {
                    doPopulate(numanode, node);
                } else {
                    populate(numanode, node);
                }
                return addLinks(numanode);
            }
        }
        return notFound();
    }

    public VdsNumaNode lookupEntity(Guid id) {
        VdsNumaNode node = lookupEntityById(id);
        return node == null ? entityNotFound() : node;
    }

    private VdsNumaNode lookupEntityById(Guid id) {
        for (VdsNumaNode node : getCollection()) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }

}
