package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.di.interceptor.InvocationLogger;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * {@code VmDaoImpl} provides a concrete implementation of {@link VmDao}.
 */
@Named
@Singleton
@InvocationLogger
public class VmDaoImpl extends BaseDao implements VmDao {

    @Override
    public VM get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VM get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmByVmGuid", vmRowMapper, getCustomMapSqlParameterSource()
                .addValue("vm_guid", id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public VM getHostedEngineVm() {
        return getCallsHandler().executeRead("GetHostedEngineVm",
                vmRowMapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    public VM getByNameForDataCenter(Guid dataCenterId, String name, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmByVmNameForDataCenter", vmRowMapper, getCustomMapSqlParameterSource()
                .addValue("data_center_id", dataCenterId).addValue("vm_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public VM getByNameAndNamespaceForCluster(Guid clusterId,
            String name,
            String namespace) {
        return getCallsHandler().executeRead("getByNameAndNamespaceForCluster",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("vm_name", name)
                        .addValue("namespace", namespace));
    }

    @Override
    public Map<Boolean, List<VM>> getForDisk(Guid id, boolean includeVmsSnapshotAttachedTo) {
        List<Pair<VM, VmDevice>> vms = getVmsWithPlugInfo(id);

        return vms.stream()
                .filter(p -> includeVmsSnapshotAttachedTo || p.getSecond().getSnapshotId() == null)
                .collect(Collectors.groupingBy(p -> p.getSecond().isPlugged(),
                        Collectors.mapping(Pair::getFirst, Collectors.toList())));

    }

    @Override
    public List<VM> getAllVMsWithDisksOnOtherStorageDomain(Guid storageDomainGuid) {
        return getCallsHandler().executeReadList("GetAllVMsWithDisksOnOtherStorageDomain",
                vmRowMapper,
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainGuid));
    }

    @Override
    public List<VM> getVmsListForDisk(Guid id, boolean includeVmsSnapshotAttachedTo) {
        List<Pair<VM, VmDevice>> vms = getVmsWithPlugInfo(id);
        return vms.stream()
                .filter(pair -> includeVmsSnapshotAttachedTo || pair.getSecond().getSnapshotId() == null)
                .map(Pair::getFirst)
                .collect(Collectors.toList());
    }

    @Override
    public List<VM> getVmsListByInstanceType(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByInstanceTypeId",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("instance_type_id", id));
    }

    public List<Pair<VM, VmDevice>> getVmsWithPlugInfo(Guid id) {
        return getCallsHandler().executeReadList
                ("GetVmsByDiskId",
                        vmWithPlugInfoRowMapper,
                        getCustomMapSqlParameterSource().addValue("disk_guid", id));
    }

    @Override
    public List<VM> getAllForUser(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByUserId",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", id));
    }

    @Override
    public List<VM> getAllSortedAndFiltered(Guid userID, int offset, int limit) {
        return getCallsHandler().executeReadList("GetAllFromVmsFilteredAndSorted",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userID)
                        .addValue("offset", offset)
                        .addValue("limit", limit));
    }

    @Override
    public List<VM> getAllForUserWithGroupsAndUserRoles(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByUserIdWithGroupsAndUserRoles", vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", id));
    }

    @Override
    public List<VM> getAllForAdGroupByName(String name) {
        return getCallsHandler().executeReadList("GetVmsByAdGroupNames",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("ad_group_names", name));
    }

    @Override
    public List<VM> getAllWithTemplate(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByVmtGuid",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vmt_guid", id));
    }

    @Override
    public List<VM> getAllRunningForVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsRunningOnVds",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public Map<Guid, List<VM>> getAllRunningForMultipleVds(Collection<Guid> hostIds) {
        List<VM> vms = getCallsHandler().executeReadList("GetVmsRunningOnMultipleVds",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_ids", createArrayOfUUIDs(hostIds)));

        return vms.stream().collect(Collectors.groupingBy(VM::getRunOnVds));
    }

    @Override
    public List<VM> getAllRunningOnOrMigratingToVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsRunningOnOrMigratingToVds",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public List<VM> getMonitoredVmsRunningByVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsRunningByVds",
                 vmMonitoringRowMapper,
                 getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public List<VM> getAllUsingQuery(String query) {
        return getJdbcTemplate().query(query, vmRowMapper);
    }

    @Override
    public List<VM> getAllForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByStorageDomainId",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", id));
    }

    @Override
    public List<VM> getAllVmsRelatedToQuotaId(Guid quotaId) {
        return getCallsHandler().executeReadList("getAllVmsRelatedToQuotaId",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("quota_id", quotaId));
    }

    @Override
    public List<VM> getVmsByIds(Collection<Guid> vmsIds) {
        return getCallsHandler().executeReadList("GetVmsByIds",
                vmRowMapper,
                getCustomMapSqlParameterSource().addValue("vms_ids", createArrayOfUUIDs(vmsIds)));
    }

    @Override
    public List<VM> getAllActiveForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("GetActiveVmsByStorageDomainId",
                vmRowMapper,
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
                vmRowMapper,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
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
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("network_id", id));
    }

    @Override
    public List<VM> getAllForVnicProfile(Guid vNicProfileId) {
        return getCallsHandler().executeReadList("GetVmsByVnicProfileId",
                vmRowMapper,
                getCustomMapSqlParameterSource().addValue("vnic_profile_id", vNicProfileId));
    }

    @Override
    public List<VM> getAllForCluster(Guid clusterId) {
        return getCallsHandler().executeReadList("GetVmsByClusterId",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public List<VM> getAllForVmPool(Guid vmPoolId) {
        return getCallsHandler().executeReadList("GetVmsByVmPoolId",
                vmRowMapper,
                getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", vmPoolId));
    }

    @Override
    public List<VM> getAllFailedAutoStartVms() {
        return getCallsHandler().executeReadList("GetFailedAutoStartVms",
                vmRowMapper,
                getCustomMapSqlParameterSource());
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
                vmRowMapper,
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
                vmRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public List<VM> getAllForCpuProfiles(Collection<Guid> cpuProfileIds) {
        return getCallsHandler().executeReadList("GetVmsByCpuProfileIds",
                vmRowMapper, getCustomMapSqlParameterSource()
                        .addValue("cpu_profile_ids", createArrayOfUUIDs(cpuProfileIds)));
    }

    @Override
    public List<VM> getAllForDiskProfiles(Collection<Guid> diskProfileIds) {
        return getCallsHandler().executeReadList("GetAllVmsRelatedToDiskProfiles",
                vmRowMapper, getCustomMapSqlParameterSource()
                        .addValue("disk_profile_ids", createArrayOfUUIDs(diskProfileIds)));
    }

    @Override
    public List<VM> getAllPinnedToHost(Guid hostId) {
        return getCallsHandler().executeReadList("GetVmsPinnedToHost",
                vmRowMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    static final RowMapper<VM> vmRowMapper = (rs, rowNum) -> {
        VM entity = new VM();
        entity.setStaticData(VmStaticDaoImpl.getRowMapper().mapRow(rs, rowNum));
        entity.setDynamicData(VmDynamicDaoImpl.getRowMapper().mapRow(rs, rowNum));

        entity.setQuotaName(rs.getString("quota_name"));
        entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
        entity.setClusterName(rs.getString("cluster_name"));
        entity.setVmtName(rs.getString("vmt_name"));
        entity.setVmPoolName(rs.getString("vm_pool_name"));
        entity.setVmPoolId(getGuid(rs, "vm_pool_id"));
        entity.setRunOnVdsName(rs.getString("run_on_vds_name"));
        entity.setClusterCpuName(rs.getString("cluster_cpu_name"));
        entity.setClusterCpuFlags(rs.getString("cluster_cpu_flags"));
        entity.setClusterCpuVerb(rs.getString("cluster_cpu_verb"));
        entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
        entity.setStoragePoolName(rs.getString("storage_pool_name"));
        entity.setTransparentHugePages(rs.getBoolean("transparent_hugepages"));
        entity.setClusterCompatibilityVersion(new VersionRowMapper("cluster_compatibility_version").mapRow(rs, rowNum));
        entity.setTrustedService(rs.getBoolean("trusted_service"));
        entity.setClusterArch(ArchitectureType.forValue(rs.getInt("architecture")));
        entity.setVmPoolSpiceProxy(rs.getString("vm_pool_spice_proxy"));
        entity.setClusterSpiceProxy(rs.getString("cluster_spice_proxy"));
        entity.setNextRunConfigurationExists(rs.getBoolean("next_run_config_exists"));
        entity.setNextRunChangedFields(SerializationFactory.getDeserializer().deserializeOrCreateNew(rs.getString("changed_fields"), HashSet.class));
        entity.setPreviewSnapshot(rs.getBoolean("is_previewing_snapshot"));
        entity.setHasIllegalImages(rs.getBoolean("has_illegal_images"));
        entity.setClusterBiosType(BiosType.forValue(rs.getInt("cluster_bios_type")));
        return entity;
    };

    private static final RowMapper<VM> vmMonitoringRowMapper = (rs, rowNum) -> {
        VM entity = new VM();
        entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
        entity.setName(rs.getString("vm_name"));
        entity.setOrigin(OriginType.forValue(rs.getInt("origin")));
        entity.setAutoStartup(rs.getBoolean("auto_startup"));
        entity.setVmMemSizeMb(rs.getInt("mem_size_mb"));
        entity.setMinAllocatedMem(rs.getInt("min_allocated_mem"));
        entity.setNumOfSockets(rs.getInt("num_of_sockets"));
        entity.setCpuPerSocket(rs.getInt("cpu_per_socket"));
        entity.setThreadsPerCpu(rs.getInt("threads_per_cpu"));
        entity.setPriority(rs.getInt("priority"));
        entity.setLeaseStorageDomainId(getGuid(rs, "lease_sd_id"));
        entity.setDynamicData(VmDynamicDaoImpl.getRowMapper().mapRow(rs, rowNum));

        return entity;
    };

    private static final RowMapper<Pair<VM, VmDevice>> vmWithPlugInfoRowMapper = (rs, rowNum) -> {
        Pair<VM, VmDevice> entity = new Pair<>();
        entity.setFirst(vmRowMapper.mapRow(rs, rowNum));
        entity.setSecond(VmDeviceDaoImpl.vmDeviceRowMapper.mapRow(rs, rowNum));
        return entity;
    };

    @Override
    public List<VM> getVmsByOrigins(List<OriginType> origins) {
        Object[] originValues = origins.stream().map(OriginType::getValue).toArray();
        return getCallsHandler().executeReadList("GetVmsByOrigin",
                vmRowMapper,
                getCustomMapSqlParameterSource().addValue("origins", createArrayOf("int", originValues)));
    }

    @Override
    public List<String> getAllRunningNamesWithSpecificIsoAttached(Guid isoDiskId) {
        return getCallsHandler().executeReadList("GetActiveVmNamesWithIsoAttached",
                SingleColumnRowMapper.newInstance(String.class),
                getCustomMapSqlParameterSource().addValue("iso_disk_id", isoDiskId));
    }

    private Pair<String, String> getExternalData(Guid vmId, String functionName) {
        List<Pair<String, String>> resultRows = getCallsHandler().executeReadList(functionName,
                (rs, i) -> new Pair<>(new String(rs.getString("data")), rs.getString("hash")),
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
        if (resultRows.isEmpty()) {
            return new Pair<String, String>();
        } else {
            return resultRows.get(0);
        }
    }

    @Override
    public Pair<String, String> getTpmData(Guid vmId) {
        return getExternalData(vmId, "GetTpmData");
    }

    @Override
    public void updateTpmData(Guid vmId, String tpmData, String tpmDataHash) {
        getCallsHandler().executeModification("UpdateTpmData",
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId)
                        .addValue("tpm_data", tpmData)
                        .addValue("tpm_hash", tpmDataHash));
    }

    @Override
    public void deleteTpmData(Guid vmId) {
        getCallsHandler().executeModification("DeleteTpmData",
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId));
    }

    @Override
    public void copyTpmData(Guid sourceVmId, Guid targetVmId) {
        getCallsHandler().executeModification("CopyTpmData",
                getCustomMapSqlParameterSource()
                        .addValue("source_vm_id", sourceVmId)
                        .addValue("target_vm_id", targetVmId));
    }

    @Override
    public Pair<String, String> getNvramData(Guid vmId) {
        return getExternalData(vmId, "GetNvramData");
    }

    @Override
    public void updateNvramData(Guid vmId, String nvramData, String nvramDataHash) {
        getCallsHandler().executeModification("UpdateNvramData",
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId)
                        .addValue("nvram_data", nvramData)
                        .addValue("nvram_hash", nvramDataHash));
    }

    @Override
    public void deleteNvramData(Guid vmId) {
        getCallsHandler().executeModification("DeleteNvramData",
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId));
    }

    @Override
    public void copyNvramData(Guid sourceVmId, Guid targetVmId) {
        getCallsHandler().executeModification("CopyNvramData",
                getCustomMapSqlParameterSource()
                        .addValue("source_vm_id", sourceVmId)
                        .addValue("target_vm_id", targetVmId));
    }
}
