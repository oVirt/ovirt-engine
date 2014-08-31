package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class VdsNumaNodeDAODbFacadeImpl extends BaseDAODbFacade implements VdsNumaNodeDAO {

    @Override
    public List<VdsNumaNode> getAllVdsNumaNodeByVdsId(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", vdsId);

        List<VdsNumaNode> vdsNumaNodes =
                getCallsHandler().executeReadList("GetNumaNodeByVdsId",
                        vdsNumaNodeRowMapper, parameterSource);

        List<Pair<Guid, Integer>> numaNodesCpus =
                getCallsHandler().executeReadList("GetNumaNodeCpuByVdsId",
                        vdsNumaNodeCpusRowMapper, parameterSource);

        Map<Guid, List<Integer>> numaNodesCpusMap = new HashMap<>();

        for (Pair<Guid, Integer> pair : numaNodesCpus) {
            if (!numaNodesCpusMap.containsKey(pair.getFirst())) {
                numaNodesCpusMap.put(pair.getFirst(), new ArrayList<Integer>());
            }
            numaNodesCpusMap.get(pair.getFirst()).add(pair.getSecond());
        }

        for (VdsNumaNode node : vdsNumaNodes) {
            if (numaNodesCpusMap.containsKey(node.getId())) {
                node.setCpuIds(numaNodesCpusMap.get(node.getId()));
            }
        }

        return vdsNumaNodes;
    }

    @Override
    public void massSaveNumaNode(List<VdsNumaNode> numaNodes, Guid vdsId, Guid vmId) {
        List<MapSqlParameterSource> numaNodeExecutions = new ArrayList<>(numaNodes.size());
        List<MapSqlParameterSource> numaNodeCpusExecutions = new ArrayList<>();
        List<MapSqlParameterSource> vNodeToPnodeExecutions = new ArrayList<>();

        for (VdsNumaNode node : numaNodes) {
            numaNodeExecutions.add(createNumaNodeParametersMapper(node)
                    .addValue("vds_id", vdsId)
                    .addValue("vm_id", vmId));
            for (Integer cpuId : node.getCpuIds()) {
                numaNodeCpusExecutions.add(createNumaNodeCpusParametersMapper(node, cpuId));
            }
            if (node instanceof VmNumaNode) {
                for (Pair<Guid, Pair<Boolean, Integer>> pair : ((VmNumaNode)node).getVdsNumaNodeList()) {
                    vNodeToPnodeExecutions.add(createVnodeToPnodeParametersMapper(pair, node.getId()));
                }
            }
        }

        getCallsHandler().executeStoredProcAsBatch("InsertNumaNode", numaNodeExecutions);
        getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeCpu", numaNodeCpusExecutions);
        if (!vNodeToPnodeExecutions.isEmpty()) {
            getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeMap", vNodeToPnodeExecutions);
        }
    }

    @Override
    public void massUpdateNumaNodeStatistics(List<VdsNumaNode> numaNodes) {
        List<MapSqlParameterSource> executions = new ArrayList<>(numaNodes.size());
        for (VdsNumaNode node : numaNodes) {
            executions.add(createNumaNodeStatisticsParametersMapper(node));
        }

        getCallsHandler().executeStoredProcAsBatch("UpdateNumaNodeStatistics", executions);
    }

    @Override
    public void massUpdateNumaNode(List<VdsNumaNode> numaNodes) {
        List<MapSqlParameterSource> executions = new ArrayList<>(numaNodes.size());
        List<MapSqlParameterSource> vdsCpuDeletions = new ArrayList<>();
        List<MapSqlParameterSource> vdsCpuInsertions = new ArrayList<>();
        List<MapSqlParameterSource> vNodeToPnodeDeletions = new ArrayList<>();
        List<MapSqlParameterSource> vNodeToPnodeInsertions = new ArrayList<>();
        for (VdsNumaNode node : numaNodes) {
            executions.add(createNumaNodeParametersMapper(node));
            vdsCpuDeletions.add(getCustomMapSqlParameterSource().addValue("numa_node_id", node.getId()));
            for (Integer cpuId : node.getCpuIds()) {
                vdsCpuInsertions.add(createNumaNodeCpusParametersMapper(node, cpuId));
            }
            if (node instanceof VmNumaNode) {
                vNodeToPnodeDeletions.add(getCustomMapSqlParameterSource().addValue("vm_numa_node_id", node.getId()));
                for (Pair<Guid, Pair<Boolean, Integer>> pair : ((VmNumaNode)node).getVdsNumaNodeList()) {
                    vNodeToPnodeInsertions.add(createVnodeToPnodeParametersMapper(pair, node.getId()));
                }
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
        List<MapSqlParameterSource> executions = new ArrayList<>(numaNodeIds.size());
        for (Guid id : numaNodeIds) {
            executions.add(getCustomMapSqlParameterSource().addValue("numa_node_id", id));
        }
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

    private static final RowMapper<VdsNumaNode> vdsNumaNodeRowMapper =
            new RowMapper<VdsNumaNode>() {
                @Override
                public VdsNumaNode mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    VdsNumaNode entity = new VdsNumaNode();
                    NumaNodeStatistics stat = new NumaNodeStatistics();
                    entity.setId(getGuid(rs, "numa_node_id"));
                    entity.setIndex(rs.getInt("numa_node_index"));
                    entity.setMemTotal(rs.getLong("mem_total"));
                    stat.setMemFree(rs.getLong("mem_free"));
                    stat.setMemUsagePercent(rs.getInt("usage_mem_percent"));
                    stat.setCpuSys(rs.getDouble("cpu_sys"));
                    stat.setCpuUser(rs.getDouble("cpu_user"));
                    stat.setCpuIdle(rs.getDouble("cpu_idle"));
                    stat.setCpuUsagePercent(rs.getInt("usage_cpu_percent"));
                    entity.setNumaNodeStatistics(stat);
                    entity.setNumaNodeDistances(getDistanceMap(rs.getString("distance")));
                    return entity;
                }
            };

    private static final RowMapper<Pair<Guid, Integer>> vdsNumaNodeCpusRowMapper =
            new RowMapper<Pair<Guid, Integer>>() {

                @Override
                public Pair<Guid, Integer> mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    return new Pair<Guid, Integer>(getGuid(rs, "numa_node_id"), rs.getInt("cpu_core_id"));
                }
            };

    // format: (<index_id>, <distance>);*, for example: "0, 10; 2, 16"
    private static Map<Integer, Integer> getDistanceMap(String distance) {
        Map<Integer, Integer> nodeDistance = new HashMap<>();
        if (StringUtils.isBlank(distance)) {
            return nodeDistance;
        }
        String[] distanceArray = distance.split(";");
        for (int i = 0; i < distanceArray.length; i++) {
            String[] nodeDistanceArray = distanceArray[i].split(",");
            nodeDistance.put(Integer.valueOf(nodeDistanceArray[0]), Integer.valueOf(nodeDistanceArray[1]));
        }
        return nodeDistance;
    }

    private static String getDistanceString(Map<Integer, Integer> distance) {
        if (distance == null || distance.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<Integer, Integer> entry : distance.entrySet()) {
            sb.append(entry.getKey());
            sb.append(",");
            sb.append(entry.getValue());
            sb.append(";");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

}
