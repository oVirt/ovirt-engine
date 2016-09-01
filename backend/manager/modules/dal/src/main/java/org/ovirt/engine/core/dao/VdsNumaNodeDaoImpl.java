package org.ovirt.engine.core.dao;

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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VdsNumaNodeDaoImpl extends NumaNodeDaoImpl<VdsNumaNode> implements VdsNumaNodeDao {

    @Override
    public List<VdsNumaNode> getAllVdsNumaNodeByVdsId(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", vdsId);

        return getCallsHandler().executeReadList("GetNumaNodeByVdsId",
                vdsNumaNodeRowMapper, parameterSource);
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
}
