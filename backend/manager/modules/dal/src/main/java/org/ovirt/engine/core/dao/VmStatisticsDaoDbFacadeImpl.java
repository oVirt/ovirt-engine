package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class VmStatisticsDaoDbFacadeImpl extends MassOperationsGenericDaoDbFacade<VmStatistics, Guid>
        implements VmStatisticsDAO {

    public VmStatisticsDaoDbFacadeImpl() {
        super("VmStatistics");
        setProcedureNameForGet("GetVmStatisticsByVmGuid");
    }

    @Override
    public List<VmStatistics> getAll() {
        throw new NotImplementedException();
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vm_guid", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmStatistics statistics) {
        return createIdParameterMapper(statistics.getId())
                .addValue("cpu_sys", statistics.getcpu_sys())
                .addValue("cpu_user", statistics.getcpu_user())
                .addValue("elapsed_time", statistics.getelapsed_time())
                .addValue("usage_cpu_percent",
                        statistics.getusage_cpu_percent())
                .addValue("usage_mem_percent",
                        statistics.getusage_mem_percent())
                .addValue("migration_progress_percent",
                        statistics.getMigrationProgressPercent())
                .addValue("usage_network_percent",
                        statistics.getusage_network_percent())
                .addValue("disks_usage",
                                statistics.getDisksUsage());
    }

    @Override
    protected RowMapper<VmStatistics> createEntityRowMapper() {
        return new RowMapper<VmStatistics>() {
            @Override
            public VmStatistics mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VmStatistics entity = new VmStatistics();
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
                entity.setelapsed_time(rs.getDouble("elapsed_time"));
                entity.setusage_cpu_percent((Integer) rs
                        .getObject("usage_cpu_percent"));
                entity.setusage_mem_percent((Integer) rs
                        .getObject("usage_mem_percent"));
                entity.setMigrationProgressPercent(rs
                        .getInt("migration_progress_percent"));
                entity.setusage_network_percent((Integer) rs
                        .getObject("usage_network_percent"));
                entity.setDisksUsage((String) rs
                        .getObject("disks_usage"));
                entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
                return entity;
            }
        };
    }
}
