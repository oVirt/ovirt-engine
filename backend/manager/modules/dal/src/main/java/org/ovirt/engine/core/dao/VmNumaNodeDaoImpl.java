package org.ovirt.engine.core.dao;

import static org.ovirt.engine.core.utils.CollectionUtils.pairsToMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmNumaNodeDaoImpl extends NumaNodeDaoBase<VmNumaNode> implements VmNumaNodeDao {

    @Override
    public List<VmNumaNode> getAllVmNumaNodeByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        List<VmNumaNode> vmNumaNodes =
                getCallsHandler().executeReadList("GetNumaNodeByVmId",
                        vmNumaNodeCpuRowMapper, parameterSource);

        Map<Guid, List<Integer>> vmNumaNodesPinMap = getAllVmNumaNodePinInfo();

        vmNumaNodes.stream().filter(node -> vmNumaNodesPinMap.containsKey(node.getId())).forEach(
                node -> node.setVdsNumaNodeList(vmNumaNodesPinMap.get(node.getId())));

        return vmNumaNodes;
    }

    @Override
    public Map<Guid, List<VmNumaNode>> getVmNumaNodeInfoByClusterId(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId);

        List<Pair<Guid, VmNumaNode>> vmNumaNodes =
                getCallsHandler().executeReadList("GetVmNumaNodeByCluster",
                        vmNumaNodeInfoWithClusterRowMapper, parameterSource);

        Map<Guid, List<Integer>> numaNodesCpusMap = getAllNumaNodeCpuMap();

        Map<Guid, List<Integer>> vmNumaNodesPinMap = getAllVmNumaNodePinInfo();

        for (Pair<Guid, VmNumaNode> pair : vmNumaNodes) {
            if (numaNodesCpusMap.containsKey(pair.getSecond().getId())) {
                pair.getSecond().setCpuIds(numaNodesCpusMap.get(pair.getSecond().getId()));
            }
            if (vmNumaNodesPinMap.containsKey(pair.getSecond().getId())) {
                pair.getSecond().setVdsNumaNodeList(vmNumaNodesPinMap.get(pair.getSecond().getId()));
            }
        }

        return pairsToMap(vmNumaNodes);
    }

    @Override
    public void massSaveNumaNode(List<VmNumaNode> numaNodes, Guid vmId) {
        insertNodes(numaNodes, node -> createNumaNodeParametersMapper(node)
                    .addValue("vm_id", vmId)
                    .addValue("numa_tune_mode", node.getNumaTuneMode().getValue()));
        insertNumaNodeMap(numaNodes);
        insertCpus(numaNodes);
    }

    @Override
    public void massUpdateNumaNode(List<VmNumaNode> numaNodes) {
        updateNodes(numaNodes, node -> createNumaNodeParametersMapper(node)
                .addValue("numa_tune_mode", node.getNumaTuneMode().getValue()));
        removeCpus(numaNodes);
        insertCpus(numaNodes);

        getCallsHandler().executeStoredProcAsBatch("DeleteNumaNodeMapByVmNumaNodeId", numaNodes.stream()
                .map(node -> getCustomMapSqlParameterSource().addValue("vm_numa_node_id", node.getId()))
                .collect(Collectors.toList()));

        insertNumaNodeMap(numaNodes);
    }


    private Map<Guid, List<Integer>> getAllNumaNodeCpuMap() {
        List<Pair<Guid, Integer>> numaNodesCpus =
                getCallsHandler().executeReadList("GetAllFromNumaNodeCpuMap",
                        vmNumaNodeCpusRowMapper, getCustomMapSqlParameterSource());

        return pairsToMap(numaNodesCpus);
    }

    private Map<Guid, List<Integer>> getAllVmNumaNodePinInfo() {
        List<Pair<Guid, Integer>> numaNodesAssign =
                getCallsHandler().executeReadList("GetAllAssignedNumaNodeInfomation",
                        vmNumaNodeAssignmentRowMapper, getCustomMapSqlParameterSource());

        return pairsToMap(numaNodesAssign);
    }

    private static final RowMapper<VmNumaNode> vmNumaNodeRowMapper = (rs, rowNum) -> {
        VmNumaNode entity = new VmNumaNode();
        entity.setId(getGuid(rs, "numa_node_id"));
        entity.setIndex(rs.getInt("numa_node_index"));
        entity.setMemTotal(rs.getLong("mem_total"));
        entity.setNumaTuneMode(NumaTuneMode.forValue(rs.getString("numa_tune_mode")));
        return entity;
    };

    private static final RowMapper<VmNumaNode> vmNumaNodeCpuRowMapper =  (rs, rowNum) -> {
        VmNumaNode entity = vmNumaNodeRowMapper.mapRow(rs, rowNum);
        // We need to copy the array to a normal ArrayList to be GWT compatible. GWT has deserialization
        // problems with the Arrays.asList implementation returned by getArray() (Java8 related?)
        entity.setCpuIds(new ArrayList<>(Arrays.asList((Integer[]) rs.getArray("cpu_core_ids").getArray())));
        return entity;
    };

    private static final RowMapper<Pair<Guid, VmNumaNode>> vmNumaNodeInfoWithClusterRowMapper = (rs, rowNum) -> {
        VmNumaNode entity = new VmNumaNode();
        entity.setId(getGuid(rs, "vm_numa_node_id"));
        entity.setIndex(rs.getInt("vm_numa_node_index"));
        entity.setMemTotal(rs.getLong("vm_numa_node_mem_total"));
        entity.setNumaTuneMode(NumaTuneMode.forValue(rs.getString("vm_numa_node_numa_tune_mode")));
        return new Pair<>(getGuid(rs, "vm_numa_node_vm_id"), entity);
    };

    private void insertNumaNodeMap(List<VmNumaNode> numaNodes) {
        List<MapSqlParameterSource> vNodeToPnodeExecutions = new ArrayList<>();

        for (VmNumaNode node : numaNodes) {
            node.getVdsNumaNodeList().stream()
                    .map(index -> createVnodeToPnodeParametersMapper(index, node.getId()))
                    .forEach(vNodeToPnodeExecutions::add);
        }

        getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeMap", vNodeToPnodeExecutions);
    }

    private static final RowMapper<Pair<Guid, Integer>> vmNumaNodeCpusRowMapper =
            (rs, rowNum) -> new Pair<>(getGuid(rs, "numa_node_id"), rs.getInt("cpu_core_id"));

    private static final RowMapper<Pair<Guid, Integer>> vmNumaNodeAssignmentRowMapper =
            (rs, rowNum) -> new Pair<>(getGuid(rs, "assigned_vm_numa_node_id"),
                    rs.getInt("run_in_vds_numa_node_index"));

    private MapSqlParameterSource createVnodeToPnodeParametersMapper(
            Integer pinnedIndex, Guid vNodeId) {
        return getCustomMapSqlParameterSource()
                .addValue("id", Guid.newGuid())
                .addValue("vm_numa_node_id", vNodeId)
                .addValue("vds_numa_node_index", pinnedIndex);
    }
}
