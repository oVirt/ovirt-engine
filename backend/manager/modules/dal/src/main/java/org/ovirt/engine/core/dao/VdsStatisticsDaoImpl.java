package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;


/**
 * <code>VdsDaoImpl</code> provides an implementation of {@link VdsDao} that uses previously written code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 *
 */
@Named
@Singleton
public class VdsStatisticsDaoImpl extends BaseDao implements VdsStatisticsDao {

    private static final class VdsStatisticsRowMapper implements RowMapper<VdsStatistics> {
        public static final VdsStatisticsRowMapper instance = new VdsStatisticsRowMapper();

        @Override
        public VdsStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
            VdsStatistics entity = new VdsStatistics();
            entity.setCpuIdle(rs.getDouble("cpu_idle"));
            entity.setCpuLoad(rs.getDouble("cpu_load"));
            entity.setCpuSys(rs.getDouble("cpu_sys"));
            entity.setCpuUser(rs.getDouble("cpu_user"));
            entity.setUsageCpuPercent((Integer) rs
                    .getObject("usage_cpu_percent"));
            entity.setUsageMemPercent((Integer) rs
                    .getObject("usage_mem_percent"));
            entity.setUsageNetworkPercent((Integer) rs
                    .getObject("usage_network_percent"));
            entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
            entity.setMemAvailable(rs.getLong("mem_available"));
            entity.setMemFree(rs.getLong("mem_free"));
            entity.setMemShared(rs.getLong("mem_shared"));
            entity.setSwapFree(rs.getLong("swap_free"));
            entity.setSwapTotal(rs.getLong("swap_total"));
            entity.setKsmCpuPercent((Integer) rs
                    .getObject("ksm_cpu_percent"));
            entity.setKsmPages(rs.getLong("ksm_pages"));
            entity.setKsmState((Boolean) rs.getObject("ksm_state"));
            entity.setAnonymousHugePages(rs.getInt("anonymous_hugepages"));
            entity.setBootTime((Long) rs.getObject("boot_time"));
            entity.setHighlyAvailableScore(rs.getInt("ha_score"));
            entity.setHighlyAvailableIsConfigured(rs.getBoolean("ha_configured"));
            entity.setHighlyAvailableIsActive(rs.getBoolean("ha_active"));
            entity.setHighlyAvailableGlobalMaintenance(rs.getBoolean("ha_global_maintenance"));
            entity.setHighlyAvailableLocalMaintenance(rs.getBoolean("ha_local_maintenance"));
            entity.setCpuOverCommitTimeStamp(DbFacadeUtils.fromDate(rs
                    .getTimestamp("cpu_over_commit_time_stamp")));
            return entity;
        }
    }

    @Override
    public VdsStatistics get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        return getCallsHandler().executeRead("GetVdsStatisticsByVdsId",
                VdsStatisticsRowMapper.instance,
                parameterSource);
    }

    @Override
    public void save(VdsStatistics stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cpu_idle", stats.getCpuIdle())
                .addValue("cpu_load", stats.getCpuLoad())
                .addValue("cpu_sys", stats.getCpuSys())
                .addValue("cpu_user", stats.getCpuUser())
                .addValue("usage_cpu_percent", stats.getUsageCpuPercent())
                .addValue("usage_mem_percent", stats.getUsageMemPercent())
                .addValue("usage_network_percent",
                        stats.getUsageNetworkPercent())
                .addValue("vds_id", stats.getId())
                .addValue("mem_available", stats.getMemAvailable())
                .addValue("mem_free" , stats.getMemFree())
                .addValue("mem_shared", stats.getMemShared())
                .addValue("swap_free", stats.getSwapFree())
                .addValue("swap_total", stats.getSwapTotal())
                .addValue("ksm_cpu_percent", stats.getKsmCpuPercent())
                .addValue("ksm_pages", stats.getKsmPages())
                .addValue("ksm_state", stats.getKsmState())
                .addValue("anonymous_hugepages", stats.getAnonymousHugePages())
                .addValue("boot_time", stats.getBootTime())
                .addValue("ha_score", stats.getHighlyAvailableScore())
                .addValue("ha_configured", stats.getHighlyAvailableIsConfigured())
                .addValue("ha_active", stats.getHighlyAvailableIsActive())
                .addValue("ha_global_maintenance", stats.getHighlyAvailableGlobalMaintenance())
                .addValue("ha_local_maintenance", stats.getHighlyAvailableLocalMaintenance())
                .addValue("cpu_over_commit_time_stamp", stats.getCpuOverCommitTimeStamp());

        getCallsHandler().executeModification("InsertVdsStatistics", parameterSource);
    }

    @Override
    public void update(VdsStatistics stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cpu_idle", stats.getCpuIdle())
                .addValue("cpu_load", stats.getCpuLoad())
                .addValue("cpu_sys", stats.getCpuSys())
                .addValue("cpu_user", stats.getCpuUser())
                .addValue("usage_cpu_percent", stats.getUsageCpuPercent())
                .addValue("usage_mem_percent", stats.getUsageMemPercent())
                .addValue("usage_network_percent",
                        stats.getUsageNetworkPercent())
                .addValue("vds_id", stats.getId())
                .addValue("mem_available", stats.getMemAvailable())
                .addValue("mem_free" , stats.getMemFree())
                .addValue("mem_shared", stats.getMemShared())
                .addValue("swap_free", stats.getSwapFree())
                .addValue("swap_total", stats.getSwapTotal())
                .addValue("ksm_cpu_percent", stats.getKsmCpuPercent())
                .addValue("ksm_pages", stats.getKsmPages())
                .addValue("ksm_state", stats.getKsmState())
                .addValue("anonymous_hugepages", stats.getAnonymousHugePages())
                .addValue("boot_time", stats.getBootTime())
                .addValue("ha_score", stats.getHighlyAvailableScore())
                .addValue("ha_configured", stats.getHighlyAvailableIsConfigured())
                .addValue("ha_active", stats.getHighlyAvailableIsActive())
                .addValue("ha_global_maintenance", stats.getHighlyAvailableGlobalMaintenance())
                .addValue("ha_local_maintenance", stats.getHighlyAvailableLocalMaintenance())
                .addValue("cpu_over_commit_time_stamp", stats.getCpuOverCommitTimeStamp());

        getCallsHandler().executeModification("UpdateVdsStatistics", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        getCallsHandler().executeModification("DeleteVdsStatistics", parameterSource);
    }

    @Override
    public List<VdsStatistics> getAll() {
        throw new UnsupportedOperationException();
    }
}
