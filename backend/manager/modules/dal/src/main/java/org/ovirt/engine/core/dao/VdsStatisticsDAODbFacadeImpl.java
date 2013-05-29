package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;


/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@link VdsDAO} that uses previously written code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 *
 */
public class VdsStatisticsDAODbFacadeImpl extends BaseDAODbFacade implements VdsStatisticsDAO {

    private static final class VdsStatisticsRowMapper implements RowMapper<VdsStatistics> {
        public static final VdsStatisticsRowMapper instance = new VdsStatisticsRowMapper();

        @Override
        public VdsStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
            VdsStatistics entity = new VdsStatistics();
            entity.setcpu_idle(rs.getDouble("cpu_idle"));
            entity.setcpu_load(rs.getDouble("cpu_load"));
            entity.setcpu_sys(rs.getDouble("cpu_sys"));
            entity.setcpu_user(rs.getDouble("cpu_user"));
            entity.setusage_cpu_percent((Integer) rs
                    .getObject("usage_cpu_percent"));
            entity.setusage_mem_percent((Integer) rs
                    .getObject("usage_mem_percent"));
            entity.setusage_network_percent((Integer) rs
                    .getObject("usage_network_percent"));
            entity.setId(Guid.createGuidFromStringDefaultEmpty(rs
                    .getString("vds_id")));
            entity.setmem_available(rs.getLong("mem_available"));
            entity.setmem_shared(rs.getLong("mem_shared"));
            entity.setswap_free(rs.getLong("swap_free"));
            entity.setswap_total(rs.getLong("swap_total"));
            entity.setksm_cpu_percent((Integer) rs
                    .getObject("ksm_cpu_percent"));
            entity.setksm_pages(rs.getLong("ksm_pages"));
            entity.setksm_state((Boolean) rs.getObject("ksm_state"));
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
                .addValue("cpu_idle", stats.getcpu_idle())
                .addValue("cpu_load", stats.getcpu_load())
                .addValue("cpu_sys", stats.getcpu_sys())
                .addValue("cpu_user", stats.getcpu_user())
                .addValue("usage_cpu_percent", stats.getusage_cpu_percent())
                .addValue("usage_mem_percent", stats.getusage_mem_percent())
                .addValue("usage_network_percent",
                        stats.getusage_network_percent())
                .addValue("vds_id", stats.getId())
                .addValue("mem_available", stats.getmem_available())
                .addValue("mem_shared", stats.getmem_shared())
                .addValue("swap_free", stats.getswap_free())
                .addValue("swap_total", stats.getswap_total())
                .addValue("ksm_cpu_percent", stats.getksm_cpu_percent())
                .addValue("ksm_pages", stats.getksm_pages())
                .addValue("ksm_state", stats.getksm_state());

        getCallsHandler().executeModification("InsertVdsStatistics", parameterSource);
    }

    @Override
    public void update(VdsStatistics stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cpu_idle", stats.getcpu_idle())
                .addValue("cpu_load", stats.getcpu_load())
                .addValue("cpu_sys", stats.getcpu_sys())
                .addValue("cpu_user", stats.getcpu_user())
                .addValue("usage_cpu_percent", stats.getusage_cpu_percent())
                .addValue("usage_mem_percent", stats.getusage_mem_percent())
                .addValue("usage_network_percent",
                        stats.getusage_network_percent())
                .addValue("vds_id", stats.getId())
                .addValue("mem_available", stats.getmem_available())
                .addValue("mem_shared", stats.getmem_shared())
                .addValue("swap_free", stats.getswap_free())
                .addValue("swap_total", stats.getswap_total())
                .addValue("ksm_cpu_percent", stats.getksm_cpu_percent())
                .addValue("ksm_pages", stats.getksm_pages())
                .addValue("ksm_state", stats.getksm_state());

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
        throw new NotImplementedException();
    }
}
