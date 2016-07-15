package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.NumaNode;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;


public abstract class NumaNodeDaoBase<T extends NumaNode> extends BaseDao {

    public void massRemoveNumaNodeByNumaNodeId(List<Guid> numaNodeIds) {
        getCallsHandler().executeStoredProcAsBatch("DeleteNumaNode", numaNodeIds.stream()
                .map(id -> getCustomMapSqlParameterSource().addValue("numa_node_id", id))
                .collect(Collectors.toList()));
    }

    protected void insertNodes(List<T> nodes, Function<T, MapSqlParameterSource> paramFunc) {
        getCallsHandler().executeStoredProcAsBatch("InsertNumaNode", nodes.stream()
                .map(paramFunc::apply)
                .collect(Collectors.toList()));
    }

    protected void updateNodes(List<T> nodes, Function<T, MapSqlParameterSource> paramFunc) {
        getCallsHandler().executeStoredProcAsBatch("UpdateNumaNode", nodes.stream()
            .map(paramFunc::apply)
            .collect(Collectors.toList()));
    }

    protected void insertCpus(List<T> nodes) {
        List<MapSqlParameterSource> executions = new ArrayList<>();

        for (NumaNode node : nodes) {
            node.getCpuIds().stream()
                .map(cpuId -> createNumaNodeCpusParametersMapper(node, cpuId))
                .forEach(executions::add);
        }

        getCallsHandler().executeStoredProcAsBatch("InsertNumaNodeCpu", executions);
    }

    protected void removeCpus(List<T> nodes) {
        getCallsHandler().executeStoredProcAsBatch("DeleteNumaNodeCpuByNumaNodeId", nodes.stream()
            .map(node -> getCustomMapSqlParameterSource().addValue("numa_node_id", node.getId()))
            .collect(Collectors.toList()));
    }

    protected MapSqlParameterSource createNumaNodeParametersMapper(NumaNode node) {
        return getCustomMapSqlParameterSource()
                .addValue("numa_node_id", node.getId())
                .addValue("numa_node_index", node.getIndex())
                .addValue("mem_total", node.getMemTotal())
                .addValue("cpu_count", node.getCpuIds().size());
    }

    protected MapSqlParameterSource createNumaNodeCpusParametersMapper(NumaNode node, Integer cpuId) {
        return getCustomMapSqlParameterSource()
                .addValue("id", Guid.newGuid())
                .addValue("numa_node_id", node.getId())
                .addValue("cpu_core_id", cpuId);
    }
}
