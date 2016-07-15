package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmNumaNodeDaoImpl extends NumaNodeDaoImpl<VmNumaNode> implements VmNumaNodeDao {

    @Override
    public List<VmNumaNode> getAllVmNumaNodeByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        List<VmNumaNode> vmNumaNodes =
                getCallsHandler().executeReadList("GetNumaNodeByVmId",
                        vmNumaNodeCpuRowMapper, parameterSource);

        Map<Guid, List<Pair<Guid, Pair<Boolean, Integer>>>> vmNumaNodesPinMap = getAllVmNumaNodePinInfo();

        vmNumaNodes.stream().filter(node -> vmNumaNodesPinMap.containsKey(node.getId())).forEach(
                node -> node.setVdsNumaNodeList(vmNumaNodesPinMap.get(node.getId())));

        return vmNumaNodes;
    }

    private Map<Guid, List<Integer>> getAllNumaNodeCpuMap() {
        List<Pair<Guid, Integer>> numaNodesCpus =
                getCallsHandler().executeReadList("GetAllFromNumaNodeCpuMap",
                        vmNumaNodeCpusRowMapper, null);

        Map<Guid, List<Integer>> numaNodesCpusMap = new HashMap<>();

        for (Pair<Guid, Integer> pair : numaNodesCpus) {
            if (!numaNodesCpusMap.containsKey(pair.getFirst())) {
                numaNodesCpusMap.put(pair.getFirst(), new ArrayList<>());
            }

            numaNodesCpusMap.get(pair.getFirst()).add(pair.getSecond());
        }
        return numaNodesCpusMap;
    }

    private Map<Guid, List<Pair<Guid, Pair<Boolean, Integer>>>> getAllVmNumaNodePinInfo() {
        List<Pair<Guid, Pair<Guid, Pair<Boolean, Integer>>>> numaNodesAssign =
                getCallsHandler().executeReadList("GetAllAssignedNumaNodeInfomation",
                        vmNumaNodeAssignmentRowMapper, null);

        Map<Guid, List<Pair<Guid, Pair<Boolean, Integer>>>> vmNumaNodesPinMap = new HashMap<>();

        for (Pair<Guid, Pair<Guid, Pair<Boolean, Integer>>> pair : numaNodesAssign) {
            if (!vmNumaNodesPinMap.containsKey(pair.getFirst())) {
                vmNumaNodesPinMap.put(pair.getFirst(), new ArrayList<>());
            }

            vmNumaNodesPinMap.get(pair.getFirst()).add(pair.getSecond());
        }

        return vmNumaNodesPinMap;
    }

    private static final RowMapper<VmNumaNode> vmNumaNodeRowMapper = (rs, rowNum) -> {
        VmNumaNode entity = new VmNumaNode();
        entity.setId(getGuid(rs, "numa_node_id"));
        entity.setIndex(rs.getInt("numa_node_index"));
        entity.setMemTotal(rs.getLong("mem_total"));
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
        return new Pair<>(getGuid(rs, "vm_numa_node_vm_id"), entity);
    };

    private static final RowMapper<Pair<Guid, Integer>> vmNumaNodeCpusRowMapper =
            (rs, rowNum) -> new Pair<>(getGuid(rs, "numa_node_id"), rs.getInt("cpu_core_id"));

    private static final RowMapper<Pair<Guid, Pair<Guid, Pair<Boolean, Integer>>>> vmNumaNodeAssignmentRowMapper =
            (rs, rowNum) -> new Pair<>(getGuid(rs, "assigned_vm_numa_node_id"),
                    new Pair<>(getGuid(rs, "run_in_vds_numa_node_id"),
                            new Pair<>(rs.getBoolean("is_pinned"),
                            rs.getInt("run_in_vds_numa_node_index"))));

    @Override
    public List<Pair<Guid, VmNumaNode>> getVmNumaNodeInfoByClusterId(Guid cluster) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", cluster);

        List<Pair<Guid, VmNumaNode>> vmNumaNodes =
                getCallsHandler().executeReadList("GetVmNumaNodeByCluster",
                        vmNumaNodeInfoWithClusterRowMapper, parameterSource);

        Map<Guid, List<Integer>> numaNodesCpusMap = getAllNumaNodeCpuMap();

        Map<Guid, List<Pair<Guid, Pair<Boolean, Integer>>>> vmNumaNodesPinMap = getAllVmNumaNodePinInfo();

        for (Pair<Guid, VmNumaNode> pair : vmNumaNodes) {
            if (numaNodesCpusMap.containsKey(pair.getSecond().getId())) {
                pair.getSecond().setCpuIds(numaNodesCpusMap.get(pair.getSecond().getId()));
            }
            if (vmNumaNodesPinMap.containsKey(pair.getSecond().getId())) {
                pair.getSecond().setVdsNumaNodeList(vmNumaNodesPinMap.get(pair.getSecond().getId()));
            }
        }

        return vmNumaNodes;
    }

    @Override
    public Map<Guid, List<VmNumaNode>> getVmNumaNodeInfoByClusterIdAsMap(Guid vdsGroupId) {
        Map<Guid, List<VmNumaNode>> map = new HashMap<>();
        for (Pair<Guid, VmNumaNode> pair : getVmNumaNodeInfoByClusterId(vdsGroupId)){
            if (!map.containsKey(pair.getFirst())){
                map.put(pair.getFirst(), new ArrayList<>());
            }
            map.get(pair.getFirst()).add(pair.getSecond());
        }
        return map;
    }
}
