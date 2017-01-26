package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

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
                .addValue("usage_network_percent",
                        statistics.getUsageNetworkPercent())
                .addValue("disks_usage",
                                statistics.getDisksUsage())
                .addValue("guest_mem_buffered", statistics.getGuestMemoryBuffered())
                .addValue("guest_mem_cached", statistics.getGuestMemoryCached());
    }

    @Override
    protected RowMapper<VmStatistics> createEntityRowMapper() {
        return vmStatisticsRowMapper;
    }

    private static final RowMapper<VmStatistics> vmStatisticsRowMapper = (rs, rowNum) -> {
        VmStatistics entity = new VmStatistics();
        entity.setCpuSys(rs.getDouble("cpu_sys"));
        entity.setCpuUser(rs.getDouble("cpu_user"));
        entity.setElapsedTime(rs.getDouble("elapsed_time"));
        entity.setUsageCpuPercent((Integer) rs.getObject("usage_cpu_percent"));
        entity.setUsageMemPercent((Integer) rs.getObject("usage_mem_percent"));
        entity.setUsageNetworkPercent((Integer) rs.getObject("usage_network_percent"));
        entity.setDisksUsage((String) rs.getObject("disks_usage"));
        entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
        entity.setGuestMemoryBuffered(getLong(rs, "guest_mem_buffered"));
        entity.setGuestMemoryCached(getLong(rs, "guest_mem_cached"));
        return entity;
    };

    protected static RowMapper<VmStatistics> getRowMapper() {
        return vmStatisticsRowMapper;
    }
}
