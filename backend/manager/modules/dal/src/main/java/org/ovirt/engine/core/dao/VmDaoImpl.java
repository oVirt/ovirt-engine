package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * <code>VmDaoImpl</code> provides a concrete implementation of {@link VmDao}. The functionality is code
 * refactored out of {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
@Named
@Singleton
public class VmDaoImpl extends BaseDao implements VmDao {

    @Override
    public VM get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VM get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmByVmGuid", VMRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("vm_guid", id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public VM getByNameForDataCenter(Guid dataCenterId, String name, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmByVmNameForDataCenter", VMRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("data_center_id", dataCenterId).addValue("vm_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public Map<Boolean, List<VM>> getForDisk(Guid id, boolean includeVmsSnapshotAttachedTo) {
        Map<Boolean, List<VM>> result = new HashMap<>();
        List<Pair<VM, VmDevice>> vms = getVmsWithPlugInfo(id);
        for (Pair<VM, VmDevice> pair : vms) {
            VmDevice device = pair.getSecond();
            if (includeVmsSnapshotAttachedTo || device.getSnapshotId() == null) {
                MultiValueMapUtils.addToMap(device.getIsPlugged(), pair.getFirst(), result);
            }
        }
        return result;
    }

    @Override
    public List<VM> getAllVMsWithDisksOnOtherStorageDomain(Guid storageDomainGuid) {
        return getCallsHandler().executeReadList("GetAllVMsWithDisksOnOtherStorageDomain",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainGuid));
    }

    @Override
    public List<VM> getVmsListForDisk(Guid id, boolean includeVmsSnapshotAttachedTo) {
        List<VM> result = new ArrayList<>();
        List<Pair<VM, VmDevice>> vms = getVmsWithPlugInfo(id);
        for (Pair<VM, VmDevice> pair : vms) {
            if (includeVmsSnapshotAttachedTo || pair.getSecond().getSnapshotId() == null) {
                result.add(pair.getFirst());
            }
        }
        return result;
    }

    @Override
    public List<VM> getVmsListByInstanceType(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByInstanceTypeId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("instance_type_id", id));
    }

    public List<Pair<VM, VmDevice>> getVmsWithPlugInfo(Guid id) {
        return getCallsHandler().executeReadList
                ("GetVmsByDiskId",
                        VMWithPlugInfoRowMapper.instance,
                        getCustomMapSqlParameterSource().addValue("disk_guid", id));
    }

    @Override
    public List<VM> getAllForUser(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByUserId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", id));
    }

    @Override
    public List<VM> getAllForUserWithGroupsAndUserRoles(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByUserIdWithGroupsAndUserRoles", VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", id));
    }

    @Override
    public List<VM> getAllForAdGroupByName(String name) {
        return getCallsHandler().executeReadList("GetVmsByAdGroupNames",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("ad_group_names", name));
    }

    @Override
    public List<VM> getAllWithTemplate(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByVmtGuid",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vmt_guid", id));
    }

    @Override
    public List<VM> getAllRunningForVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsRunningOnVds",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public List<VM> getAllRunningOnOrMigratingToVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsRunningOnOrMigratingToVds",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public Map<Guid, VM> getAllRunningByVds(Guid id) {
        HashMap<Guid, VM> map = new HashMap<>();

        for (VM vm : getAllRunningForVds(id)) {
            map.put(vm.getId(), vm);
        }

        return map;
    }

    @Override
    public List<VM> getAllUsingQuery(String query) {
        return getJdbcTemplate().query(query, VMRowMapper.instance);
    }

    @Override
    public List<VM> getAllForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByStorageDomainId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", id));
    }

    @Override
    public List<VM> getAllVmsRelatedToQuotaId(Guid quotaId) {
        return getCallsHandler().executeReadList("getAllVmsRelatedToQuotaId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("quota_id", quotaId));
    }

    @Override
    public List<VM> getVmsByIds(List<Guid> vmsIds) {
        return getCallsHandler().executeReadList("GetVmsByIds",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vms_ids", createArrayOfUUIDs(vmsIds)));
    }

    @Override
    public List<VM> getAllActiveForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("GetActiveVmsByStorageDomainId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", id));
    }

    @Override
    public List<VM> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VM> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromVms",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VM> getAllForUserAndActionGroup(Guid userID, ActionGroup actionGroup) {
        return getCallsHandler().executeReadList("GetAllFromVmsForUserAndActionGroup",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("action_group_id", actionGroup.getId()));
    }

    @Override
    public void saveIsInitialized(Guid vmid, boolean isInitialized) {
        getCallsHandler().executeModification("UpdateIsInitialized",
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmid)
                        .addValue("is_initialized", isInitialized));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteVm", getCustomMapSqlParameterSource()
                .addValue("vm_guid", id));
    }

    @Override
    public List<VM> getAllForNetwork(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByNetworkId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("network_id", id));
    }

    @Override
    public List<VM> getAllForVnicProfile(Guid vNicProfileId) {
        return getCallsHandler().executeReadList("GetVmsByVnicProfileId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vnic_profile_id", vNicProfileId));
    }

    @Override
    public List<VM> getAllForCluster(Guid clusterId) {
        return getCallsHandler().executeReadList("GetVmsByClusterId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public List<VM> getAllForVmPool(Guid vmPoolId) {
        return getCallsHandler().executeReadList("GetVmsByVmPoolId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", vmPoolId));
    }

    @Override
    public List<VM> getAllFailedAutoStartVms() {
        return getCallsHandler().executeReadList("GetFailedAutoStartVms",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<VM> getAllMigratingToHost(Guid vdsId) {
        return getCallsHandler().executeReadList("GetVmsMigratingToVds",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vdsId));
    }

    @Override
    public void updateOriginalTemplateName(Guid originalTemplateId, String originalTemplateName) {
        getCallsHandler().executeModification("UpdateOriginalTemplateName",
                getCustomMapSqlParameterSource()
                        .addValue("original_template_id", originalTemplateId)
                        .addValue("original_template_name", originalTemplateName)
        );
    }

    @Override
    public List<VM> getAllRunningByCluster(Guid clusterId) {
        return getCallsHandler().executeReadList("GetRunningVmsByClusterId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public List<Guid> getVmIdsForVersionUpdate(Guid baseTemplateId) {
        return getCallsHandler().executeReadList("getVmIdsForVersionUpdate",
                createGuidMapper(), getCustomMapSqlParameterSource()
                    .addValue("base_template_id", baseTemplateId));
    }

    @Override
    public List<VM> getAllForStoragePool(Guid storagePoolId) {
        return getCallsHandler().executeReadList("GetAllForStoragePool",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public List<VM> getAllForCpuProfiles(Collection<Guid> cpuProfileIds) {
        return getCallsHandler().executeReadList("GetVmsByCpuProfileIds",
                VMRowMapper.instance, getCustomMapSqlParameterSource()
                        .addValue("cpu_profile_ids", createArrayOfUUIDs(cpuProfileIds)));
    }

    @Override
    public List<VM> getAllForDiskProfiles(Collection<Guid> diskProfileIds) {
        return getCallsHandler().executeReadList("GetAllVmsRelatedToDiskProfiles",
                VMRowMapper.instance, getCustomMapSqlParameterSource()
                        .addValue("disk_profile_ids", createArrayOfUUIDs(diskProfileIds)));
    }

    static final class VMRowMapper implements RowMapper<VM> {
        public static final VMRowMapper instance = new VMRowMapper();

        @Override
        public VM mapRow(ResultSet rs, int rowNum) throws SQLException {

            VM entity = new VM();
            entity.setStaticData(VmStaticDaoImpl.getRowMapper().mapRow(rs, rowNum));
            entity.setDynamicData(VmDynamicDaoImpl.getRowMapper().mapRow(rs, rowNum));
            entity.setStatisticsData(VmStatisticsDaoImpl.getRowMapper().mapRow(rs, rowNum));

            entity.setQuotaName(rs.getString("quota_name"));
            entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
            entity.setClusterName(rs.getString("cluster_name"));
            entity.setClusterDescription(rs.getString("cluster_description"));
            entity.setVmtName(rs.getString("vmt_name"));
            entity.setVmtMemSizeMb(rs.getInt("vmt_mem_size_mb"));
            entity.setVmtOsId(rs.getInt("vmt_os"));
            entity.setVmtCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("vmt_creation_date")));
            entity.setVmtChildCount(rs.getInt("vmt_child_count"));
            entity.setVmtNumOfCpus(rs.getInt("vmt_num_of_cpus"));
            entity.setVmtNumOfSockets(rs.getInt("vmt_num_of_sockets"));
            entity.setVmtCpuPerSocket(rs.getInt("vmt_cpu_per_socket"));
            entity.setVmtDescription(rs.getString("vmt_description"));
            entity.setVmPoolName(rs.getString("vm_pool_name"));
            entity.setVmPoolId(getGuid(rs, "vm_pool_id"));
            entity.setRunOnVdsName(rs.getString("run_on_vds_name"));
            entity.setClusterCpuName(rs.getString("cluster_cpu_name"));
            entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
            entity.setStoragePoolName(rs.getString("storage_pool_name"));
            entity.setTransparentHugePages(rs.getBoolean("transparent_hugepages"));
            entity.setClusterCompatibilityVersion(new Version(rs.getString("cluster_compatibility_version")));
            entity.setTrustedService(rs.getBoolean("trusted_service"));
            entity.setClusterArch(ArchitectureType.forValue(rs.getInt("architecture")));
            entity.setVmPoolSpiceProxy(rs.getString("vm_pool_spice_proxy"));
            entity.setClusterSpiceProxy(rs.getString("cluster_spice_proxy"));
            entity.setNextRunConfigurationExists(rs.getBoolean("next_run_config_exists"));
            entity.setPreviewSnapshot(rs.getBoolean("is_previewing_snapshot"));
            return entity;
        }
    }

    private static final class VMWithPlugInfoRowMapper implements RowMapper<Pair<VM, VmDevice>> {
        public static final VMWithPlugInfoRowMapper instance = new VMWithPlugInfoRowMapper();

        @Override
        public Pair<VM, VmDevice> mapRow(ResultSet rs, int rowNum) throws SQLException {
            @SuppressWarnings("synthetic-access")
            Pair<VM, VmDevice> entity = new Pair<>();
            entity.setFirst(VMRowMapper.instance.mapRow(rs, rowNum));
            entity.setSecond(VmDeviceDaoImpl.VmDeviceRowMapper.instance.mapRow(rs, rowNum));
            return entity;
        }
    }

    @Override
    public List<VM> getVmsByOrigins(List<OriginType> origins) {
        Object[] originValues = origins.stream().map(OriginType::getValue).toArray();
        return getCallsHandler().executeReadList("GetVmsByOrigin",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("origins", createArrayOf("int", originValues)));
    }
}
