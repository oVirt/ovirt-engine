package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VdsNumaNodeDaoImpl extends NumaNodeDaoBase<VdsNumaNode> implements VdsNumaNodeDao {

    @Override
    public List<VdsNumaNode> getAllVdsNumaNodeByVdsId(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", vdsId);

        return getCallsHandler().executeReadList("GetNumaNodeByVdsId",
                vdsNumaNodeRowMapper, parameterSource);
    }

    @Override
    public void massSaveNumaNode(List<VdsNumaNode> numaNodes, Guid vdsId) {
        insertNodes(numaNodes, node -> createNumaNodeParametersMapper(node).addValue("vds_id", vdsId));
        insertCpus(numaNodes);
    }

    @Override
    public void massUpdateNumaNode(List<VdsNumaNode> numaNodes) {
        updateNodes(numaNodes, node -> createNumaNodeParametersMapper(node));
        removeCpus(numaNodes);
        insertCpus(numaNodes);
    }

    @Override
    public void massUpdateNumaNodeStatistics(List<VdsNumaNode> numaNodes) {
        List<MapSqlParameterSource> executions = numaNodes.stream()
                .map(node -> createNumaNodeStatisticsParametersMapper(node))
                .collect(Collectors.toList());

        getCallsHandler().executeStoredProcAsBatch("UpdateNumaNodeStatistics", executions);
    }


    private static final RowMapper<VdsNumaNode> vdsNumaNodeRowMapper = (rs, rowNum) -> {
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
        stat.setHugePages(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("hugepages"), ArrayList.class));
        entity.setNumaNodeStatistics(stat);
        entity.setNumaNodeDistances(getDistanceMap(rs.getString("distance")));
        entity.setCpuIds(Arrays.asList((Integer[]) rs.getArray("cpu_core_ids").getArray()));
        return entity;
    };

    // format: (<index_id>, <distance>);*, for example: "0, 10; 2, 16"
    private static Map<Integer, Integer> getDistanceMap(String distance) {
        if (StringUtils.isBlank(distance)) {
            return new HashMap<>();
        }

        return Arrays.stream(distance.split(";")).map(d -> d.split(",")).collect(
                Collectors.toMap(k -> Integer.valueOf(k[0]), v -> Integer.valueOf(v[1])));
    }

    protected MapSqlParameterSource createNumaNodeParametersMapper(VdsNumaNode node) {
        return super.createNumaNodeParametersMapper(node)
                .addValue("distance", getDistanceString(node.getNumaNodeDistances()));
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

    private MapSqlParameterSource createNumaNodeStatisticsParametersMapper(VdsNumaNode node) {
        return getCustomMapSqlParameterSource()
                .addValue("numa_node_id", node.getId())
                .addValue("mem_free", node.getNumaNodeStatistics().getMemFree())
                .addValue("usage_mem_percent", node.getNumaNodeStatistics().getMemUsagePercent())
                .addValue("cpu_sys", node.getNumaNodeStatistics().getCpuSys())
                .addValue("cpu_user", node.getNumaNodeStatistics().getCpuUser())
                .addValue("cpu_idle", node.getNumaNodeStatistics().getCpuIdle())
                .addValue("usage_cpu_percent", node.getNumaNodeStatistics().getCpuUsagePercent())
                .addValue("hugepages", SerializationFactory.getSerializer().serialize(
                        node.getNumaNodeStatistics().getHugePages()));
    }
}
