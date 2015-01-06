package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.businessentities.NumaNodeVmVds;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionalInterceptor;
import org.springframework.stereotype.Component;

@Interceptors({ TransactionalInterceptor.class })
@ApplicationScoped
@Component
public class VmNumaNodeDAODbFacadeImpl extends VdsNumaNodeDAODbFacadeImpl implements VmNumaNodeDAO {

    @Override
    public List<VmNumaNode> getAllVmNumaNodeByVmId(Guid vmId) {
        List<VmNumaNode> vmNumaNodes =
                multipleResults(entityManager.createNamedQuery("VmNumaNode.getAllVmNumaNodeByVmId",
                        VmNumaNode.class)
                        .setParameter("vmId", vmId));
        return vmNumaNodes;
    }

    @Override
    public List<VmNumaNode> getAllPinnedVmNumaNodeByVdsNumaNodeId(Guid vdsNumaNodeId) {
        List<VmNumaNode> vmNumaNodes =
                multipleResults(entityManager.createNativeQuery("select * "
                        + "from GetVmNumaNodeByVdsNumaNodeIdWithPinnedInfo(CAST( CAST (? AS text) AS uuid),?)",
                        VmNumaNode.class)
                        .setParameter(1, vdsNumaNodeId.toString())
                        .setParameter(2, true));

        return vmNumaNodes;
    }

    @Override
    public List<VmNumaNode> getAllVmNumaNodeByVdsNumaNodeId(Guid vdsNumaNodeId) {
        List<VmNumaNode> vmNumaNodes =
                multipleResults(entityManager.createNativeQuery("select * "
                        + "from GetVmNumaNodeByVdsNumaNodeId(CAST( CAST (? AS text) AS uuid))",
                        VmNumaNode.class)
                        .setParameter(1, vdsNumaNodeId.toString()));

        return vmNumaNodes;
    }

    @Override
    public List<Pair<Guid, VmNumaNode>> getVmNumaNodeInfoByVdsGroupId(Guid vdsGroupId) {
        List<Pair<Guid, VmNumaNode>> vmNumaNodes = new ArrayList<>();
        List<VmNumaNode> nodes =
                multipleResults(entityManager.createNativeQuery("select vm_numa_node_id as numa_node_id, "
                        + "vm_numa_node_index as numa_node_index,"
                        + "vm_numa_node_mem_total as mem_total, "
                        + "vm_numa_node_distance as distance, "
                        + "vm_numa_node_cpu_idle as cpu_idle, "
                        + "vm_numa_node_cpu_sys as cpu_sys, "
                        + "vm_numa_node_usage_cpu_percent as usage_cpu_percent, "
                        + "vm_numa_node_cpu_user as cpu_user, "
                        + "vm_numa_node_mem_free as mem_free, "
                        + "vm_numa_node_usage_mem_percent as usage_mem_percent, "
                        + "null as vds_id, "
                        + "vm_numa_node_vm_id as vm_id "
                        + "from GetVmNumaNodeByVdsGroup(CAST( CAST (? AS text) AS uuid))",
                        VmNumaNode.class)
                        .setParameter(1, vdsGroupId.toString()));

        for (VmNumaNode node : nodes) {
            vmNumaNodes.add(new Pair<Guid, VmNumaNode>(node.getVmId(), node));
        }

        return vmNumaNodes;
    }

    @Override
    public List<Pair<Guid, Integer>> getPinnedNumaNodeIndex(Guid vmId) {
        List<VmNumaNode> numaNodes = getAllVmNumaNodeByVmId(vmId);

        List<Pair<Guid, Integer>> result = new ArrayList<>();

        for (VmNumaNode node : numaNodes) {
            for (NumaNodeVmVds item : node.getNumaNodeVdsList()) {
                result.add(new Pair<Guid, Integer>(node.getVmId(), item.getNodeIndex()));
            }
        }

        return result;
    }

    @Override
    public void massUpdateVmNumaNodeRuntimePinning(List<VmNumaNode> vmNumaNodes) {
        for (VmNumaNode node : vmNumaNodes) {
            update(node);
        }
    }
}
