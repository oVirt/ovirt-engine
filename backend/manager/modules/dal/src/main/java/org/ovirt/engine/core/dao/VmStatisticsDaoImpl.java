package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmStatisticsDaoImpl extends MassOperationsGenericDao<VmStatistics, Guid>
        implements VmStatisticsDao {

    public VmStatisticsDaoImpl() {
        super("VmStatistics");
        setProcedureNameForGet("GetVmStatisticsByVmGuid");
    }

    @Override
    public List<VmStatistics> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vm_guid", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmStatistics statistics) {
        return createIdParameterMapper(statistics.getId())
                .addValue("cpu_sys", statistics.getCpuSys())
                .addValue("cpu_user", statistics.getCpuUser())
                .addValue("elapsed_time", statistics.getElapsedTime())
                .addValue("usage_cpu_percent",
                        statistics.getUsageCpuPercent())
                .addValue("usage_mem_percent",
                        statistics.getUsageMemPercent())
                .addValue("migration_progress_percent",
                        statistics.getMigrationProgressPercent())
                .addValue("usage_network_percent",
                        statistics.getUsageNetworkPercent())
                .addValue("disks_usage",
                                statistics.getDisksUsage())
                .addValue("memory_usage_history",
                        StringUtils.join(statistics.getMemoryUsageHistory(), ","))
                .addValue("cpu_usage_history",
                        StringUtils.join(statistics.getCpuUsageHistory(), ","))
                .addValue("network_usage_history",
                        StringUtils.join(statistics.getNetworkUsageHistory(), ","));
    }

    @Override
    protected RowMapper<VmStatistics> createEntityRowMapper() {
        return VmStatisticsRowMapper.instance;
    }

    private static class VmStatisticsRowMapper implements RowMapper<VmStatistics> {
        public static final VmStatisticsRowMapper instance = new VmStatisticsRowMapper();

        @Override
        public VmStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
            VmStatistics entity = new VmStatistics();
            entity.setCpuSys(rs.getDouble("cpu_sys"));
            entity.setCpuUser(rs.getDouble("cpu_user"));
            entity.setElapsedTime(rs.getDouble("elapsed_time"));
            entity.setUsageCpuPercent((Integer) rs.getObject("usage_cpu_percent"));
            entity.setUsageMemPercent((Integer) rs.getObject("usage_mem_percent"));
            entity.setMigrationProgressPercent(rs.getInt("migration_progress_percent"));
            entity.setUsageNetworkPercent((Integer) rs.getObject("usage_network_percent"));
            entity.setDisksUsage((String) rs.getObject("disks_usage"));
            entity.setMemoryUsageHistory(asIntList((String) rs.getObject("memory_usage_history")));
            entity.setCpuUsageHistory(asIntList((String) rs.getObject("cpu_usage_history")));
            entity.setNetworkUsageHistory(asIntList((String) rs.getObject("network_usage_history")));
            entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
            return entity;
        }
    }

    private static List<Integer> asIntList(String str) {
        if (str == null || "".equals(str)) {
            return Collections.emptyList();
        }

        List<Integer> res = new ArrayList<>();
        for (String s : StringUtils.split(str, ",")) {
            try {
                res.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // add nothing if malformed
            }
        }

        return res;
    }

    protected static RowMapper<VmStatistics> getRowMapper() {
        return VmStatisticsRowMapper.instance;
    }
}
