package org.ovirt.engine.core.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;
import org.ovirt.engine.core.utils.transaction.TransactionalInterceptor;
import org.springframework.stereotype.Component;

@Interceptors({ TransactionalInterceptor.class })
@ApplicationScoped
@Component
public class VdsNumaNodeDAODbFacadeImpl extends AbstractJpaDao<VdsNumaNode, Guid> implements VdsNumaNodeDAO {

    public VdsNumaNodeDAODbFacadeImpl() {
        super(VdsNumaNode.class);
    }

    @Override
    public List<VdsNumaNode> getAllVdsNumaNodeByVdsId(Guid vdsId) {
        List<VdsNumaNode> vdsNumaNodes =
                multipleResults(entityManager.createNamedQuery("VdsNumaNode.getAllVdsNumaNodeByVdsId",
                        VdsNumaNode.class)
                        .setParameter("vdsId", vdsId));

        return vdsNumaNodes;
    }

    @Override
    public void massSaveNumaNode(List<VdsNumaNode> numaNodes, Guid vdsId, Guid vmId) {
        for (VdsNumaNode node : numaNodes) {
            node.setVdsId(vdsId);
            node.setVmId(vmId);
            save(node);
        }
    }

    @Override
    public void massUpdateNumaNodeStatistics(List<VdsNumaNode> numaNodes) {
        for (VdsNumaNode node : numaNodes) {
            update(node);
        }
    }

    @Override
    public void massUpdateNumaNode(List<VdsNumaNode> numaNodes) {
        for (VdsNumaNode node : numaNodes) {
            update(node);
        }
    }

    @Override
    public void massRemoveNumaNodeByNumaNodeId(List<Guid> numaNodeIds) {
        for (Guid id : numaNodeIds) {
            remove(id);
        }
    }
}
