package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VmPoolDAODbFacadeImpl</code> provides an implementation of {@link VmPoolDAO} based on implementation code from
 * {@link DbFacade}.
 *
 */
public class VmPoolDAODbFacadeImpl extends BaseDAODbFacade implements VmPoolDAO {

    private final static VmPoolFullRowMapper vmPoolFullRowMapper = new VmPoolFullRowMapper();
    private final static VmPoolNonFullRowMapper vmPoolNonFullRowMapper = new VmPoolNonFullRowMapper();

    private class TimeLeaseVmPoolRowMapper implements ParameterizedRowMapper<time_lease_vm_pool_map> {
        @Override
        public time_lease_vm_pool_map mapRow(ResultSet rs, int rowNum) throws SQLException {
            time_lease_vm_pool_map entity = new time_lease_vm_pool_map();
            entity.setend_time(DbFacadeUtils.fromDate(rs.getTimestamp("end_time")));
            entity.setid(Guid.createGuidFromString(rs.getString("id")));
            entity.setstart_time(DbFacadeUtils.fromDate(rs.getTimestamp("start_time")));
            entity.settype(rs.getInt("type"));
            entity.setvm_pool_id(Guid.createGuidFromString(rs.getString("vm_pool_id")));
            return entity;
        }
    }

    @Override
    public void removeVmFromVmPool(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        getCallsHandler().executeModification("DeleteVm_pool_map", parameterSource);
    }

    @Override
    public vm_pools get(NGuid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", id);
        return getCallsHandler().executeRead("GetVm_poolsByvm_pool_id", vmPoolFullRowMapper , parameterSource);
    }

    @Override
    public vm_pools getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_name", name);
        return getCallsHandler().executeRead("GetVm_poolsByvm_pool_name", vmPoolNonFullRowMapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<vm_pools> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllFromVm_pools", vmPoolFullRowMapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<vm_pools> getAllForUser(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);
        return getCallsHandler().executeReadList("GetAllVm_poolsByUser_id_with_groups_and_UserRoles",
                vmPoolNonFullRowMapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<vm_pools> getAllForAdGroup(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_group_id", id);
        return getCallsHandler().executeReadList("GetVm_poolsByAdGroup_id", vmPoolNonFullRowMapper, parameterSource);
    }

    @Override
    public List<vm_pools> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, vmPoolFullRowMapper);
    }

    @Override
    public void save(vm_pools pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_description", pool.getvm_pool_description())
                .addValue("vm_pool_id", pool.getvm_pool_id())
                .addValue("vm_pool_name", pool.getvm_pool_name())
                .addValue("vm_pool_type", pool.getvm_pool_type())
                .addValue("parameters", pool.getparameters())
                .addValue("prestarted_vms", pool.getPrestartedVms())
                .addValue("vds_group_id", pool.getvds_group_id());

        Map<String, Object> result = getCallsHandler().executeModification("InsertVm_pools", parameterSource);
        pool.setvm_pool_id(new Guid(result.get("vm_pool_id").toString()));
    }

    @Override
    public void update(vm_pools pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_description", pool.getvm_pool_description())
                .addValue("vm_pool_id", pool.getvm_pool_id())
                .addValue("vm_pool_name", pool.getvm_pool_name())
                .addValue("vm_pool_type", pool.getvm_pool_type())
                .addValue("parameters", pool.getparameters())
                .addValue("prestarted_vms", pool.getPrestartedVms())
                .addValue("vds_group_id", pool.getvds_group_id());

        getCallsHandler().executeModification("UpdateVm_pools", parameterSource);
    }

    @Override
    public void remove(NGuid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", id);

        getCallsHandler().executeModification("DeleteVm_pools", parameterSource);
    }

    @Override
    public vm_pool_map getVmPoolMapByVmGuid(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_guid", vmId);

        ParameterizedRowMapper<vm_pool_map> mapper = new ParameterizedRowMapper<vm_pool_map>() {
            @Override
            public vm_pool_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                vm_pool_map entity = new vm_pool_map();
                entity.setvm_guid(Guid.createGuidFromString(rs.getString("vm_guid")));
                entity.setvm_pool_id(Guid.createGuidFromString(rs.getString("vm_pool_id")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetVm_pool_mapByvm_guid", mapper, parameterSource);
    }

    @Override
    public void addVmToPool(vm_pool_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_guid", map.getvm_guid())
                .addValue("vm_pool_id", map.getvm_pool_id());

        getCallsHandler().executeModification("InsertVm_pool_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<vm_pool_map> getVmPoolsMapByVmPoolId(NGuid vmPoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_pool_id", vmPoolId);

        ParameterizedRowMapper<vm_pool_map> mapper = new ParameterizedRowMapper<vm_pool_map>() {
            @Override
            public vm_pool_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                vm_pool_map entity = new vm_pool_map();
                entity.setvm_guid(Guid.createGuidFromString(rs.getString("vm_guid")));
                entity.setvm_pool_id(Guid.createGuidFromString(rs.getString("vm_pool_id")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVm_pool_mapByvm_pool_id", mapper, parameterSource);
    }

    @Override
    public time_lease_vm_pool_map getTimeLeasedVmPoolMapByIdForVmPool(Guid id, NGuid vmPoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("id", id).addValue(
                "vm_pool_id", vmPoolId);

        return getCallsHandler().executeRead("Gettime_lease_vm_pool_mapByidAndByvm_pool_id",
                new TimeLeaseVmPoolRowMapper(),
                parameterSource);
    }

    @Override
    public void addTimeLeasedVmPoolMap(time_lease_vm_pool_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("end_time", map.getend_time())
                .addValue("id", map.getid())
                .addValue("start_time", map.getstart_time())
                .addValue("type", map.gettype())
                .addValue("vm_pool_id", map.getvm_pool_id());

        getCallsHandler().executeModification("Inserttime_lease_vm_pool_map", parameterSource);
    }

    @Override
    public void updateTimeLeasedVmPoolMap(time_lease_vm_pool_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("end_time",
                map.getend_time()).addValue("id", map.getid()).addValue("start_time", map.getstart_time()).addValue(
                "type", map.gettype()).addValue("vm_pool_id", map.getvm_pool_id());

        getCallsHandler().executeModification("Updatetime_lease_vm_pool_map", parameterSource);

    }

    @Override
    public void removeTimeLeasedVmPoolMap(Guid id, Guid vmPoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("id", id).addValue(
                "vm_pool_id", vmPoolId);

        getCallsHandler().executeModification("Deletetime_lease_vm_pool_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<time_lease_vm_pool_map> getAllTimeLeasedVmPoolMaps() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromtime_lease_vm_pool_map",
                new TimeLeaseVmPoolRowMapper(),
                parameterSource);
    }

    public List<vm_pool_map> getVmMapsInVmPoolByVmPoolIdAndStatus(NGuid vmPoolId, VMStatus vmStatus) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_pool_id", vmPoolId).addValue("status", vmStatus.getValue());

        ParameterizedRowMapper<vm_pool_map> mapper = new ParameterizedRowMapper<vm_pool_map>() {
            @Override
            public vm_pool_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                vm_pool_map entity = new vm_pool_map();
                entity.setvm_guid(Guid.createGuidFromString(rs.getString("vm_guid")));
                entity.setvm_pool_id(Guid.createGuidFromString(rs.getString("vm_pool_id")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("getVmMapsInVmPoolByVmPoolIdAndStatus", mapper,
                parameterSource);
    }

    static final class VmPoolFullRowMapper implements ParameterizedRowMapper<vm_pools> {
        @Override
        public vm_pools mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final vm_pools entity = new vm_pools();
            entity.setvm_pool_description(rs
                    .getString("vm_pool_description"));
            entity.setvm_pool_id(Guid.createGuidFromString(rs
                    .getString("vm_pool_id")));
            entity.setvm_pool_name(rs.getString("vm_pool_name"));
            entity.setvm_pool_type(VmPoolType.forValue(rs
                    .getInt("vm_pool_type")));
            entity.setparameters(rs.getString("parameters"));
            entity.setPrestartedVms(rs.getInt("prestarted_vms"));
            entity.setvds_group_id(Guid.createGuidFromString(rs
                    .getString("vds_group_id")));
            entity.setvds_group_name(rs.getString("vds_group_name"));
            entity.setvm_assigned_count(rs.getInt("assigned_vm_count"));
            entity.setvm_running_count(rs.getInt("vm_running_count"));
            return entity;
        }
    }

    static final class VmPoolNonFullRowMapper implements ParameterizedRowMapper<vm_pools> {
        @Override
        public vm_pools mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final vm_pools entity = new vm_pools();
            entity.setvm_pool_description(rs
                    .getString("vm_pool_description"));
            entity.setvm_pool_id(Guid.createGuidFromString(rs
                    .getString("vm_pool_id")));
            entity.setvm_pool_name(rs.getString("vm_pool_name"));
            entity.setvm_pool_type(VmPoolType.forValue(rs
                    .getInt("vm_pool_type")));
            entity.setparameters(rs.getString("parameters"));
            entity.setPrestartedVms(rs.getInt("prestarted_vms"));
            entity.setvds_group_id(Guid.createGuidFromString(rs
                    .getString("vds_group_id")));
            entity.setvds_group_name(rs.getString("vds_group_name"));
            return entity;
        }
    }
}
