package org.ovirt.engine.core.dao;

import static org.ovirt.engine.core.dao.VmDaoImpl.vmRowMapper;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code VmPoolDaoImpl} provides an implementation of {@link VmPoolDao}.
 */
@Named
@Singleton
public class VmPoolDaoImpl extends BaseDao implements VmPoolDao {
    @Override
    public void removeVmFromVmPool(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        getCallsHandler().executeModification("DeleteVm_pool_map", parameterSource);
    }

    @Override
    public VmPool get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VmPool get(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeRead("GetVm_poolsByvm_pool_id", vmPoolFullRowMapper, parameterSource);
    }

    @Override
    public VmPool getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_name", name);
        return getCallsHandler().executeRead("GetVm_poolsByvm_pool_name",
                vmPoolNonFullRowMapper,
                parameterSource);
    }

    @Override
    public List<VmPool> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllFromVm_pools", vmPoolFullRowMapper, parameterSource);
    }

    @Override
    public List<VmPool> getAllForUser(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);
        return getCallsHandler().executeReadList("GetAllVm_poolsByUser_id_with_groups_and_UserRoles",
                vmPoolNonFullRowMapper, parameterSource);
    }

    @Override
    public List<VmPool> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, vmPoolFullRowMapper);
    }

    @Override
    public void save(VmPool pool) {
        Guid id = pool.getVmPoolId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            pool.setVmPoolId(id);
        }
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_description", pool.getVmPoolDescription())
                .addValue("vm_pool_comment", pool.getComment())
                .addValue("vm_pool_id", pool.getVmPoolId())
                .addValue("vm_pool_name", pool.getName())
                .addValue("vm_pool_type", pool.getVmPoolType())
                .addValue("stateful", pool.isStateful())
                .addValue("parameters", pool.getParameters())
                .addValue("prestarted_vms", pool.getPrestartedVms())
                .addValue("cluster_id", pool.getClusterId())
                .addValue("max_assigned_vms_per_user", pool.getMaxAssignedVmsPerUser())
                .addValue("spice_proxy", pool.getSpiceProxy())
                .addValue("is_being_destroyed", pool.isBeingDestroyed())
                .addValue("is_auto_storage_select", pool.isAutoStorageSelect());

        getCallsHandler().executeModification("InsertVm_pools", parameterSource);
    }

    @Override
    public void update(VmPool pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_description", pool.getVmPoolDescription())
                .addValue("vm_pool_comment", pool.getComment())
                .addValue("vm_pool_id", pool.getVmPoolId())
                .addValue("vm_pool_name", pool.getName())
                .addValue("vm_pool_type", pool.getVmPoolType())
                .addValue("stateful", pool.isStateful())
                .addValue("parameters", pool.getParameters())
                .addValue("prestarted_vms", pool.getPrestartedVms())
                .addValue("cluster_id", pool.getClusterId())
                .addValue("max_assigned_vms_per_user", pool.getMaxAssignedVmsPerUser())
                .addValue("spice_proxy", pool.getSpiceProxy())
                .addValue("is_being_destroyed", pool.isBeingDestroyed())
                .addValue("is_auto_storage_select", pool.isAutoStorageSelect());

        getCallsHandler().executeModification("UpdateVm_pools", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", id);

        getCallsHandler().executeModification("DeleteVm_pools", parameterSource);
    }

    @Override
    public void setBeingDestroyed(Guid vmPoolId, boolean beingDestroyed) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", vmPoolId)
                .addValue("is_being_destroyed", beingDestroyed);

        getCallsHandler().executeModification("SetVmPoolBeingDestroyed", parameterSource);
    }

    @Override
    public void addVmToPool(VmPoolMap map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_guid", map.getVmId())
                .addValue("vm_pool_id", map.getVmPoolId());

        getCallsHandler().executeModification("InsertVm_pool_map", parameterSource);
    }

    @Override
    public List<VmPoolMap> getVmPoolsMapByVmPoolId(Guid vmPoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_pool_id", vmPoolId);

        return getCallsHandler().executeReadList("GetVm_pool_mapByvm_pool_id", vmPoolMapRowMapper, parameterSource);
    }

    @Override
    public List<VmPoolMap> getVmMapsInVmPoolByVmPoolIdAndStatus(Guid vmPoolId, VMStatus vmStatus) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("vm_pool_id", vmPoolId).addValue("status",
                        vmStatus.getValue());

        return getCallsHandler().executeReadList("getVmMapsInVmPoolByVmPoolIdAndStatus", vmPoolMapRowMapper,
                parameterSource);
    }

    @Override
    public void boundVmPoolPrestartedVms(Guid vmPoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_pool_id", vmPoolId);

        getCallsHandler().executeModification("BoundVmPoolPrestartedVms", parameterSource);
    }

    private static final RowMapper<VmPool> vmPoolFullRowMapper = (rs, rowNum) -> {
        final VmPool entity = new VmPool();
        entity.setVmPoolDescription(rs.getString("vm_pool_description"));
        entity.setVmPoolId(getGuidDefaultEmpty(rs, "vm_pool_id"));
        entity.setComment(rs.getString("vm_pool_comment"));
        entity.setName(rs.getString("vm_pool_name"));
        entity.setVmPoolType(VmPoolType.forValue(rs.getInt("vm_pool_type")));
        entity.setStateful(rs.getBoolean("stateful"));
        entity.setParameters(rs.getString("parameters"));
        entity.setPrestartedVms(rs.getInt("prestarted_vms"));
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setClusterName(rs.getString("cluster_name"));
        entity.setAssignedVmsCount(rs.getInt("assigned_vm_count"));
        entity.setRunningVmsCount(rs.getInt("vm_running_count"));
        entity.setMaxAssignedVmsPerUser(rs.getInt("max_assigned_vms_per_user"));
        entity.setSpiceProxy(rs.getString("spice_proxy"));
        entity.setBeingDestroyed(rs.getBoolean("is_being_destroyed"));
        entity.setAutoStorageSelect(rs.getBoolean("is_auto_storage_select"));
        return entity;
    };

    private static final RowMapper<VmPool> vmPoolNonFullRowMapper = (rs, rowNum) -> {
        final VmPool entity = new VmPool();
        entity.setVmPoolDescription(rs.getString("vm_pool_description"));
        entity.setVmPoolId(getGuidDefaultEmpty(rs, "vm_pool_id"));
        entity.setComment(rs.getString("vm_pool_comment"));
        entity.setName(rs.getString("vm_pool_name"));
        entity.setVmPoolType(VmPoolType.forValue(rs.getInt("vm_pool_type")));
        entity.setStateful(rs.getBoolean("stateful"));
        entity.setParameters(rs.getString("parameters"));
        entity.setPrestartedVms(rs.getInt("prestarted_vms"));
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setClusterName(rs.getString("cluster_name"));
        entity.setMaxAssignedVmsPerUser(rs.getInt("max_assigned_vms_per_user"));
        entity.setSpiceProxy(rs.getString("spice_proxy"));
        entity.setBeingDestroyed(rs.getBoolean("is_being_destroyed"));
        entity.setAutoStorageSelect(rs.getBoolean("is_auto_storage_select"));
        return entity;
    };

    private static final RowMapper<VmPoolMap> vmPoolMapRowMapper = (rs, nowNum) -> {
        VmPoolMap entity = new VmPoolMap();
        entity.setVmId(getGuidDefaultEmpty(rs, "vm_guid"));
        entity.setVmPoolId(getGuidDefaultEmpty(rs, "vm_pool_id"));
        return entity;
    };

    @Override
    public VM getVmDataFromPoolByPoolGuid(Guid vmPoolId, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("pool_id", vmPoolId).addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeRead("GetVmDataFromPoolByPoolId", vmRowMapper, parameterSource);
    }

    @Override
    public List<VmPool> getAllVmPoolsFilteredAndSorted(Guid userID, int offset, int limit) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userID)
                .addValue("offset", offset)
                .addValue("limit", limit);
        return getCallsHandler().executeReadList("GetAllFromVmPoolsFilteredAndSorted",
                vmPoolNonFullRowMapper,
                parameterSource);
    }
}
