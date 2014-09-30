package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@code VdsDAO} that uses previously written code from
 * {@code DbFacade}.
 */
public class VdsDAODbFacadeImpl extends BaseDAODbFacade implements VdsDAO {

    @Override
    public VDS get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VDS get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVdsByVdsId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public VDS getByName(String name) {
        return getCallsHandler().executeRead("GetVdsByName",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_name", name));
    }

    @Override
    public List<VDS> getAllForHostname(String hostname) {
        return getCallsHandler().executeReadList("GetVdsByHostName",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", hostname));
    }

    @Override
    public List<VDS> getAllWithIpAddress(String address) {
        return getCallsHandler().executeReadList("GetVdsByIp",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("ip", address));
    }

    @Override
    public List<VDS> getAllWithUniqueId(String id) {
        return getCallsHandler().executeReadList("GetVdsByUniqueID",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_unique_id", id));
    }

    @Override
    public List<VDS> getAllOfTypes(VDSType[] types) {
        List<VDS> list = new ArrayList<VDS>();
        for (VDSType type : types) {
            list.addAll(getAllOfType(type));
        }
        return list;
    }

    @Override
    public List<VDS> getAllOfType(VDSType type) {
        return getCallsHandler().executeReadList("GetVdsByType",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_type", type));
    }

    @Override
    public List<VDS> getAllForVdsGroupWithoutMigrating(Guid id) {
        return getCallsHandler().executeReadList("GetVdsWithoutMigratingVmsByVdsGroupId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", id));
    }

    @Override
    public List<VDS> getAllWithQuery(String query) {
        return jdbcTemplate.query(query, VdsRowMapper.instance);
    }

    @Override
    public List<VDS> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VDS> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromVds",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VDS> getAllForVdsGroup(Guid vdsGroupID) {
        return getAllForVdsGroup(vdsGroupID, null, false);
    }

    @Override
    public List<VDS> getAllForVdsGroup(Guid vdsGroupID, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetVdsByVdsGroupId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vdsGroupID)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VDS> getHostsForStorageOperation(Guid storagePoolId, boolean localFsOnly) {
        // normalize to uniquely represent in DB that we want candidates from all DC's
        if (storagePoolId == null || storagePoolId.equals(Guid.Empty)) {
            storagePoolId = null;
        }
        return getCallsHandler().executeReadList("getHostsForStorageOperation",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                    .addValue("storage_pool_id", storagePoolId)
                    .addValue("local_fs_only", localFsOnly));
    }

    @Override
    public VDS getFirstUpRhelForVdsGroup(Guid vdsGroupId) {
        List<VDS> vds = getCallsHandler().executeReadList("getFirstUpRhelForVdsGroupId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vdsGroupId));

        return vds.size() != 0 ? vds.iterator().next() : null;
    }

    @Override
    public List<VDS> getAllForStoragePool(Guid storagePool) {
        return getAllForStoragePool(storagePool, null, false);
    }

    @Override
    public List<VDS> getAllForStoragePool(Guid storagePool, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetVdsByStoragePoolId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePool)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VDS> getAllForVdsGroupWithStatus(Guid vdsGroupId, VDSStatus status) {
        return getCallsHandler().executeReadList("getVdsForVdsGroupWithStatus",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vdsGroupId)
                        .addValue("status", status.getValue()));
    }

    @Override
    public List<VDS> getAllForStoragePoolAndStatus(Guid storagePool, VDSStatus status) {
        return getCallsHandler().executeReadList("getVdsByStoragePoolIdWithStatus",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePool)
                        .addValue("status", status.getValue()));
    }

    @Override
    public List<VDS> getListForSpmSelection(Guid storagePoolId) {
        return getCallsHandler().executeReadList("GetUpAndPrioritizedVds",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public List<VDS> listFailedAutorecoverables() {
        return getCallsHandler().executeReadList("GetFailingVdss", VdsRowMapper.instance, null);
    }

    @Override
    public List<VDS> getAllForNetwork(Guid networkId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", networkId);

        return getCallsHandler().executeReadList("GetVdsByNetworkId",
                VdsRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<VDS> getAllWithoutNetwork(Guid networkId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", networkId);

        return getCallsHandler().executeReadList("GetVdsWithoutNetwork",
                VdsRowMapper.instance,
                parameterSource);
    }

    static final class VdsRowMapper implements RowMapper<VDS> {
        // single instance
        public final static VdsRowMapper instance = new VdsRowMapper();

        @Override
        public VDS mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final VDS entity = new VDS();
            entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
            entity.setVdsGroupId(getGuidDefaultEmpty(rs, "vds_group_id"));
            entity.setVdsGroupName(rs.getString("vds_group_name"));
            entity.setVdsGroupDescription(rs
                    .getString("vds_group_description"));
            entity.setVdsName(rs.getString("vds_name"));
            entity.setComment(rs.getString("free_text_comment"));
            entity.setManagementIp(rs.getString("ip"));
            entity.setUniqueId(rs.getString("vds_unique_id"));
            entity.setServerSslEnabled(rs
                    .getBoolean("server_SSL_enabled"));
            entity.setHostName(rs.getString("host_name"));
            entity.setPort(rs.getInt("port"));
            entity.setProtocol(VdsProtocol.fromValue(rs.getInt("protocol")));
            entity.setSshPort(rs.getInt("ssh_port"));
            entity.setSshUsername(rs.getString("ssh_username"));
            entity.setStatus(VDSStatus.forValue(rs.getInt("status")));
            entity.setCpuCores((Integer) rs.getObject("cpu_cores"));
            entity.setCpuThreads((Integer) rs.getObject("cpu_threads"));
            entity.setCpuModel(rs.getString("cpu_model"));
            entity.setOnlineCpus(rs.getString("online_cpus"));
            entity.setCpuUser(rs.getDouble("cpu_user"));
            entity.setCpuSpeedMh(rs.getDouble("cpu_speed_mh"));
            entity.setIfTotalSpeed(rs.getString("if_total_speed"));
            entity.setKvmEnabled((Boolean) rs.getObject("kvm_enabled"));
            entity.setPhysicalMemMb((Integer) rs
                    .getObject("physical_mem_mb"));
            entity.setCpuIdle(rs.getDouble("cpu_idle"));
            entity.setCpuLoad(rs.getDouble("cpu_load"));
            entity.setCpuSys(rs.getDouble("cpu_sys"));
            entity.setMemCommited((Integer) rs.getObject("mem_commited"));
            entity.setVmActive((Integer) rs.getObject("vm_active"));
            entity.setVmCount((Integer) rs.getObject("vm_count"));
            entity.setVmsCoresCount((Integer) rs
                    .getObject("vms_cores_count"));
            entity.setVmMigrating((Integer) rs.getObject("vm_migrating"));
            entity.setUsageCpuPercent((Integer) rs
                    .getObject("usage_cpu_percent"));
            entity.setUsageMemPercent((Integer) rs
                    .getObject("usage_mem_percent"));
            entity.setUsageNetworkPercent((Integer) rs
                    .getObject("usage_network_percent"));
            entity.setReservedMem((Integer) rs.getObject("reserved_mem"));
            entity.setGuestOverhead((Integer) rs
                    .getObject("guest_overhead"));
            entity.setVersion(new RpmVersion(rs.getString("rpm_version")));
            entity.setSoftwareVersion(rs.getString("software_version"));
            entity.setVersionName(rs.getString("version_name"));
            entity.setPreviousStatus(VDSStatus.forValue(rs
                    .getInt("previous_status")));
            entity.setMemAvailable(rs.getLong("mem_available"));
            entity.setMemShared(rs.getLong("mem_shared"));
            entity.setVdsType(VDSType.forValue(rs.getInt("vds_type")));
            entity.setCpuFlags(rs.getString("cpu_flags"));
            entity.setVdsGroupCpuName(rs.getString("vds_group_cpu_name"));
            entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
            entity.setStoragePoolName(rs.getString("storage_pool_name"));
            entity.setPendingVcpusCount((Integer) rs
                    .getObject("pending_vcpus_count"));
            entity.setCpuOverCommitTimestamp(DbFacadeUtils.fromDate(rs
                    .getTimestamp("cpu_over_commit_time_stamp")));
            entity.setPendingVmemSize(rs.getInt("pending_vmem_size"));
            entity.setVdsStrength(rs.getInt("vds_strength"));
            entity.setMaxVdsMemoryOverCommit(rs
                    .getInt("max_vds_memory_over_commit"));
            entity.setCpuSockets((Integer) rs.getObject("cpu_sockets"));
            entity.setVdsSpmId((Integer) rs.getObject("vds_spm_id"));
            entity.setNetConfigDirty((Boolean) rs
                    .getObject("net_config_dirty"));
            entity.setPmType(rs.getString("pm_type"));
            entity.setPmUser(rs.getString("pm_user"));
            entity.setPmPassword(DbFacadeUtils.decryptPassword(rs.getString("pm_password")));
            entity.setPmPort((Integer) rs.getObject("pm_port"));
            entity.setPmOptions(rs.getString("pm_options"));
            entity.setpm_enabled(rs.getBoolean("pm_enabled"));
            entity.setPmProxyPreferences(rs.getString("pm_proxy_preferences"));
            entity.setPmSecondaryIp((rs.getString("pm_secondary_ip")));
            entity.setPmSecondaryType(rs.getString("pm_secondary_type"));
            entity.setPmSecondaryUser(rs.getString("pm_secondary_user"));
            entity.setPmSecondaryPassword(DbFacadeUtils.decryptPassword(rs.getString("pm_secondary_password")));
            entity.setPmSecondaryPort((Integer) rs.getObject("pm_secondary_port"));
            entity.setPmSecondaryOptions(rs.getString("pm_secondary_options"));
            entity.setPmSecondaryConcurrent(rs.getBoolean("pm_secondary_concurrent"));
            entity.setPmKdumpDetection(rs.getBoolean("pm_detect_kdump"));
            entity.setSpmStatus(VdsSpmStatus.forValue(rs
                    .getInt("spm_status")));
            entity.setSwapFree(rs.getLong("swap_free"));
            entity.setSwapTotal(rs.getLong("swap_total"));
            entity.setKsmCpuPercent((Integer) rs
                    .getObject("ksm_cpu_percent"));
            entity.setKsmPages(rs.getLong("ksm_pages"));
            entity.setKsmState((Boolean) rs.getObject("ksm_state"));
            entity.setSupportedClusterLevels(rs
                    .getString("supported_cluster_levels"));
            entity.setSupportedEngines(rs.getString("supported_engines"));
            entity.setVdsGroupCompatibilityVersion(new Version(rs
                    .getString("vds_group_compatibility_version")));
            entity.setVdsGroupSupportsVirtService(rs.getBoolean("vds_group_virt_service"));
            entity.setVdsGroupSupportsGlusterService(rs.getBoolean("vds_group_gluster_service"));
            entity.setHostOs(rs.getString("host_os"));
            entity.setGlusterVersion(new RpmVersion(rs.getString("gluster_version")));
            entity.setKvmVersion(rs.getString("kvm_version"));
            entity.setLibvirtVersion(new RpmVersion(rs.getString("libvirt_version")));
            entity.setSpiceVersion(rs.getString("spice_version"));
            entity.setKernelVersion(rs.getString("kernel_version"));
            entity.setIScsiInitiatorName(rs
                    .getString("iscsi_initiator_name"));
            entity.setTransparentHugePagesState(VdsTransparentHugePagesState
                    .forValue(rs.getInt("transparent_hugepages_state")));
            entity.setAnonymousHugePages(rs.getInt("anonymous_hugepages"));
            entity.setHooksStr(rs.getString("hooks"));
            entity.setNonOperationalReason(NonOperationalReason.forValue(rs
                    .getInt("non_operational_reason")));
            entity.setOtpValidity(rs.getLong("otp_validity"));
            entity.setVdsSpmPriority(rs.getInt("vds_spm_priority"));
            entity.setAutoRecoverable(rs.getBoolean("recoverable"));
            entity.setSshKeyFingerprint(rs.getString("sshKeyFingerprint"));
            entity.setHostProviderId(getGuid(rs, "host_provider_id"));
            entity.setHardwareManufacturer(rs.getString("hw_manufacturer"));
            entity.setHardwareProductName(rs.getString("hw_product_name"));
            entity.setHardwareVersion(rs.getString("hw_version"));
            entity.setHardwareSerialNumber(rs.getString("hw_serial_number"));
            entity.setHardwareUUID(rs.getString("hw_uuid"));
            entity.setHardwareFamily(rs.getString("hw_family"));
            entity.setHBAs(new JsonObjectDeserializer().deserialize(rs.getString("hbas"), HashMap.class));
            entity.setConsoleAddress(rs.getString("console_address"));
            entity.setSupportedEmulatedMachines(rs.getString("supported_emulated_machines"));
            entity.setHighlyAvailableScore(rs.getInt("ha_score"));
            entity.setDisablePowerManagementPolicy(rs.getBoolean("disable_auto_pm"));
            entity.setPowerManagementControlledByPolicy(rs.getBoolean("controlled_by_pm_policy"));
            entity.setHighlyAvailableIsConfigured(rs.getBoolean("ha_configured"));
            entity.setHighlyAvailableIsActive(rs.getBoolean("ha_active"));
            entity.setHighlyAvailableGlobalMaintenance(rs.getBoolean("ha_global_maintenance"));
            entity.setHighlyAvailableLocalMaintenance(rs.getBoolean("ha_local_maintenance"));
            entity.setKdumpStatus(KdumpStatus.valueOfNumber(rs.getInt("kdump_status")));
            entity.getSupportedRngSources().addAll(VmRngDevice.csvToSourcesSet(rs.getString("supported_rng_sources")));
            entity.calculateFreeVirtualMemory();
            entity.setBootTime((Long) rs.getObject("boot_time"));
            entity.setSELinuxEnforceMode((Integer) rs.getObject("selinux_enforce_mode"));
            entity.setAutoNumaBalancing(AutoNumaBalanceStatus.forValue(rs.getInt("auto_numa_balancing")));
            entity.setNumaSupport(rs.getBoolean("is_numa_supported"));
            entity.setLiveSnapshotSupport(rs.getBoolean("is_live_snapshot_supported"));
            entity.setLiveMergeSupport(rs.getBoolean("is_live_merge_supported"));
            entity.setCountThreadsAsCores(rs.getBoolean("count_threads_as_cores"));
            return entity;
        }
    }
}
