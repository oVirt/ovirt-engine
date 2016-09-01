package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;


public abstract class NumaNodeDaoImpl<T extends VdsNumaNode> extends BaseDao implements NumaNodeDao<T> {

    @Override
    public void massSaveNumaNode(List<T> numaNodes, Guid vdsId, Guid vmId) {
        List<MapSqlParameterSource> numaNodeExecutions = new ArrayList<>(numaNodes.size());
        List<MapSqlParameterSource> numaNodeCpusExecutions = new ArrayList<>();
        List<MapSqlParameterSource> vNodeToPnodeExecutions = new ArrayList<>();

        for (VdsNumaNode node : numaNodes) {
            numaNodeExecutions.add(createNumaNodeParametersMapper(node)
                    .addValue("vds_id", vdsId)
                    .addValue("vm_id", vmId));
            numaNodeCpusExecutions.addAll(node.getCpuIds()
                            .stream()
                            .map(cpuId -> createNumaNodeCpusParametersMapper(node, cpuId))
                            .collect(Collectors.toList()));
            if (node instanceof VmNumaNode) {
                vNodeToPnodeExecutions.addAll(((VmNumaNode) node).getVdsNumaNodeList()
                        .stream()
                        .map(pair -> createVnodeToPnodeParametersMapper(pair, node.getId()))
                        .collect(Collectors.toList()));
            }
        }

        getCallsHandler().executeStoredProcAsBatch("InsertNumaNode", numaNodeExecutions);
        getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeCpu", numaNodeCpusExecutions);
        if (!vNodeToPnodeExecutions.isEmpty()) {
            getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeMap", vNodeToPnodeExecutions);
        }
    }

    @Override
    public void massUpdateNumaNodeStatistics(List<T> numaNodes) {
        List<MapSqlParameterSource> executions =
                numaNodes.stream().map(this::createNumaNodeStatisticsParametersMapper).collect(Collectors.toList());

        getCallsHandler().executeStoredProcAsBatch("UpdateNumaNodeStatistics", executions);
    }

    @Override
    public void massUpdateNumaNode(List<T> numaNodes) {
        List<MapSqlParameterSource> executions = new ArrayList<>(numaNodes.size());
        List<MapSqlParameterSource> vdsCpuDeletions = new ArrayList<>();
        List<MapSqlParameterSource> vdsCpuInsertions = new ArrayList<>();
        List<MapSqlParameterSource> vNodeToPnodeDeletions = new ArrayList<>();
        List<MapSqlParameterSource> vNodeToPnodeInsertions = new ArrayList<>();
        for (VdsNumaNode node : numaNodes) {
            executions.add(createNumaNodeParametersMapper(node));
            vdsCpuDeletions.add(getCustomMapSqlParameterSource().addValue("numa_node_id", node.getId()));
            vdsCpuInsertions.addAll(node.getCpuIds()
                    .stream()
                    .map(cpuId -> createNumaNodeCpusParametersMapper(node, cpuId))
                    .collect(Collectors.toList()));
            if (node instanceof VmNumaNode) {
                vNodeToPnodeDeletions.add(getCustomMapSqlParameterSource().addValue("vm_numa_node_id", node.getId()));
                vNodeToPnodeInsertions.addAll(((VmNumaNode) node).getVdsNumaNodeList()
                        .stream()
                        .map(pair -> createVnodeToPnodeParametersMapper(pair, node.getId()))
                        .collect(Collectors.toList()));
            }
        }
        getCallsHandler().executeStoredProcAsBatch("UpdateNumaNode", executions);
        getCallsHandler().executeStoredProcAsBatch("DeleteNumaNodeCpuByNumaNodeId", vdsCpuDeletions);
        getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeCpu", vdsCpuInsertions);
        if (!vNodeToPnodeDeletions.isEmpty()) {
            getCallsHandler().executeStoredProcAsBatch("DeleteNumaNodeMapByVmNumaNodeId", vNodeToPnodeDeletions);
        }
        if (!vNodeToPnodeInsertions.isEmpty()) {
            getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeMap", vNodeToPnodeInsertions);
        }
    }

    @Override
    public void massRemoveNumaNodeByNumaNodeId(List<Guid> numaNodeIds) {
        List<MapSqlParameterSource> executions = numaNodeIds
                .stream()
                .map(id -> getCustomMapSqlParameterSource().addValue("numa_node_id", id))
                .collect(Collectors.toList());
        getCallsHandler().executeStoredProcAsBatch("DeleteNumaNode", executions);
    }


    private MapSqlParameterSource createNumaNodeParametersMapper(VdsNumaNode node) {
        return getCustomMapSqlParameterSource()
                .addValue("numa_node_id", node.getId())
                .addValue("numa_node_index", node.getIndex())
                .addValue("mem_total", node.getMemTotal())
                .addValue("cpu_count", node.getCpuIds().size())
                .addValue("distance", getDistanceString(node.getNumaNodeDistances()));
    }

    private MapSqlParameterSource createNumaNodeStatisticsParametersMapper(VdsNumaNode node) {
        return getCustomMapSqlParameterSource()
                .addValue("numa_node_id", node.getId())
                .addValue("mem_free", node.getNumaNodeStatistics().getMemFree())
                .addValue("usage_mem_percent", node.getNumaNodeStatistics().getMemUsagePercent())
                .addValue("cpu_sys", node.getNumaNodeStatistics().getCpuSys())
                .addValue("cpu_user", node.getNumaNodeStatistics().getCpuUser())
                .addValue("cpu_idle", node.getNumaNodeStatistics().getCpuIdle())
                .addValue("usage_cpu_percent", node.getNumaNodeStatistics().getCpuUsagePercent());
    }

    private MapSqlParameterSource createNumaNodeCpusParametersMapper(VdsNumaNode node, Integer cpuId) {
        return getCustomMapSqlParameterSource()
                .addValue("id", Guid.newGuid())
                .addValue("numa_node_id", node.getId())
                .addValue("cpu_core_id", cpuId);
    }

    protected MapSqlParameterSource createVnodeToPnodeParametersMapper(
            Pair<Guid, Pair<Boolean, Integer>> pNode, Guid vNodeId) {
        return getCustomMapSqlParameterSource()
                .addValue("id", Guid.newGuid())
                .addValue("vm_numa_node_id", vNodeId)
                .addValue("vds_numa_node_id", pNode.getFirst())
                .addValue("vds_numa_node_index", pNode.getSecond().getSecond())
                .addValue("is_pinned", pNode.getSecond().getFirst());
    }



    private static String getDistanceString(Map<Integer, Integer> distance) {
        if (distance == null || distance.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : distance.entrySet()) {
            sb.append(entry.getKey());
            sb.append(",");
            sb.append(entry.getValue());
            sb.append(";");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

}
