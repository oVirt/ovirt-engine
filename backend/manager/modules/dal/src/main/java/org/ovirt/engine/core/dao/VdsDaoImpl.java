package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.di.interceptor.InvocationLogger;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code VdsDaoImpl} provides an implementation of {@code VdsDao}.
 */
@Named
@Singleton
@InvocationLogger
public class VdsDaoImpl extends BaseDao implements VdsDao {

    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    @Override
    public VDS get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VDS get(Guid id, Guid userID, boolean isFiltered) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByVdsId",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
        return vdsList.size() == 0 ? null : vdsList.get(0);
    }

    @Override
    public VDS getByName(String name, Guid clusterId) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByNameAndClusterId",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_name", name)
                        .addValue("cluster_id", clusterId));
        return vdsList.size() == 0 ? null : vdsList.get(0);
    }

    @Override
    public Optional<VDS> getFirstByName(String name) {
        List<VDS> hosts = getByName(name);
        if (hosts.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(hosts.get(0));
    }

    @Override
    public List<VDS> getByName(String name) {
        return getCallsHandler().executeReadList("GetVdsByName",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_name", name));
    }

    @Override
    public List<VDS> getAllForHostname(String hostname, Guid clusterId) {
        return getCallsHandler().executeReadList("GetVdsByHostNameAndClusterId",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", hostname)
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public List<VDS> getAllForHostname(String hostname) {
        return getCallsHandler().executeReadList("GetVdsByHostName",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", hostname));
    }

    @Override
    public List<VDS> getAllWithUniqueId(String id) {
        return getCallsHandler().executeReadList("GetVdsByUniqueID",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_unique_id", id));
    }

    @Override
    public List<VDS> getAllOfTypes(VDSType[] types) {
        List<VDS> list = new ArrayList<>();
        for (VDSType type : types) {
            list.addAll(getAllOfType(type));
        }
        return list;
    }

    @Override
    public List<VDS> getAllOfType(VDSType type) {
        return getCallsHandler().executeReadList("GetVdsByType",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_type", type));
    }

    @Override
    public List<VDS> getAllForClusterWithoutMigrating(Guid clusterId) {
        return getCallsHandler().executeReadList("GetVdsWithoutMigratingVmsByClusterId",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public List<VDS> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, vdsRowMapper);
    }

    @Override
    public List<VDS> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VDS> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromVds",
                vdsRowMapper,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VDS> getAllForCluster(Guid cluster) {
        return getAllForCluster(cluster, null, false);
    }

    @Override
    public List<VDS> getAllForCluster(Guid cluster, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetVdsByClusterId",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", cluster)
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
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                    .addValue("storage_pool_id", storagePoolId)
                    .addValue("local_fs_only", localFsOnly));
    }

    @Override
    public VDS getFirstUpRhelForCluster(Guid clsuterId) {
        List<VDS> vds = getCallsHandler().executeReadList("getFirstUpRhelForClusterId",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clsuterId));

        return vds.size() != 0 ? vds.iterator().next() : null;
    }

    @Override
    public List<VDS> getAllForStoragePool(Guid storagePool) {
        return getAllForStoragePool(storagePool, null, false);
    }

    @Override
    public List<VDS> getAllForStoragePool(Guid storagePool, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetVdsByStoragePoolId",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePool)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VDS> getAllForClusterWithStatus(Guid clusterId, VDSStatus status) {
        return getCallsHandler().executeReadList("getVdsForClusterWithStatus",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("status", status.getValue()));
    }

    @Override
    public List<VDS> getAllForClusterWithStatusAndPeerStatus(Guid clusterId, VDSStatus status, PeerStatus peerStatus) {
        return getCallsHandler().executeReadList("getVdsForClusterWithPeerStatus",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("status", status.getValue())
                        .addValue("peer_status", peerStatus.name()));
    }

    @Override
    public List<VDS> getAllForStoragePoolAndStatus(Guid storagePool, VDSStatus status) {
        return getAllForStoragePoolAndStatuses(storagePool, status != null ? EnumSet.of(status) : null);
    }

    @Override
    public List<VDS> getAllForStoragePoolAndStatuses(Guid storagePool, Set<VDSStatus> statuses) {
        return getCallsHandler().executeReadList("getVdsByStoragePoolIdWithStatuses",
                vdsRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePool)
                        .addValue("statuses",
                                statuses == null?
                                        null :
                                        statuses.stream()
                                                .map(VDSStatus::getValue)
                                                .map(Object::toString)
                                                .collect(Collectors.joining(","))));

    }

    @Override
    public List<VDS> getListForSpmSelection(Guid storagePoolId) {
        return getCallsHandler().executeReadList("GetUpAndPrioritizedVds",
                vdsRowMapper,
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public List<VDS> listFailedAutorecoverables() {
        return getCallsHandler().executeReadList("GetFailingVdss", vdsRowMapper, getCustomMapSqlParameterSource());
    }

    @Override
    public List<VDS> getAllForNetwork(Guid networkId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", networkId);

        return getCallsHandler().executeReadList("GetVdsByNetworkId",
                vdsRowMapper,
                parameterSource);
    }

    @Override
    public List<VDS> getAllWithoutNetwork(Guid networkId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", networkId);

        return getCallsHandler().executeReadList("GetVdsWithoutNetwork",
                vdsRowMapper,
                parameterSource);
    }

    private final RowMapper<VDS> vdsRowMapper = (rs, rowNum) -> {
        final VDS entity = new VDS();
        entity.setStatisticsData(VdsStatisticsDaoImpl.getRowMapper().mapRow(rs, rowNum));

        Guid hostId = getGuidDefaultEmpty(rs, "vds_id");
        entity.setId(hostId);
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setClusterName(rs.getString("cluster_name"));
        entity.setClusterDescription(rs.getString("cluster_description"));
        entity.setVdsName(rs.getString("vds_name"));
        entity.setComment(rs.getString("free_text_comment"));
        entity.setUniqueId(rs.getString("vds_unique_id"));
        entity.setServerSslEnabled(rs.getBoolean("server_SSL_enabled"));
        entity.setHostName(rs.getString("host_name"));
        entity.setPort(rs.getInt("port"));
        entity.setSshPort(rs.getInt("ssh_port"));
        entity.setSshUsername(rs.getString("ssh_username"));
        entity.setStatus(VDSStatus.forValue(rs.getInt("status")));
        entity.setExternalStatus(ExternalStatus.forValue(rs.getInt("external_status")));
        entity.setCpuCores((Integer) rs.getObject("cpu_cores"));
        entity.setCpuThreads((Integer) rs.getObject("cpu_threads"));
        entity.setCpuModel(rs.getString("cpu_model"));
        entity.setOnlineCpus(rs.getString("online_cpus"));
        entity.setCpuSpeedMh(rs.getDouble("cpu_speed_mh"));
        entity.setIfTotalSpeed(rs.getString("if_total_speed"));
        entity.setKvmEnabled((Boolean) rs.getObject("kvm_enabled"));
        entity.setPhysicalMemMb((Integer) rs.getObject("physical_mem_mb"));
        entity.setMemCommited((Integer) rs.getObject("mem_commited"));
        entity.setVmActive((Integer) rs.getObject("vm_active"));
        entity.setVmCount((Integer) rs.getObject("vm_count"));
        entity.setVmsCoresCount((Integer) rs.getObject("vms_cores_count"));
        entity.setVmMigrating((Integer) rs.getObject("vm_migrating"));
        entity.setIncomingMigrations(rs.getInt("incoming_migrations"));
        entity.setOutgoingMigrations(rs.getInt("outgoing_migrations"));
        entity.setReservedMem((Integer) rs.getObject("reserved_mem"));
        entity.setGuestOverhead((Integer) rs.getObject("guest_overhead"));
        entity.setVersion(new RpmVersion(rs.getString("rpm_version")));
        entity.setSoftwareVersion(rs.getString("software_version"));
        entity.setVersionName(rs.getString("version_name"));
        entity.setPreviousStatus(VDSStatus.forValue(rs.getInt("previous_status")));
        entity.setVdsType(VDSType.forValue(rs.getInt("vds_type")));
        entity.setCpuFlags(rs.getString("cpu_flags"));
        entity.setClusterCpuName(rs.getString("cluster_cpu_name"));
        entity.setClusterFlags(rs.getString("cluster_cpu_flags"));
        entity.setClusterVerb(rs.getString("cluster_cpu_verb"));
        entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
        entity.setStoragePoolName(rs.getString("storage_pool_name"));
        entity.setPendingVcpusCount((Integer) rs.getObject("pending_vcpus_count"));
        entity.setCpuOverCommitTimestamp(DbFacadeUtils.fromDate(rs.getTimestamp("cpu_over_commit_time_stamp")));
        entity.setPendingVmemSize(rs.getInt("pending_vmem_size"));
        entity.setMaxVdsMemoryOverCommit(rs.getInt("max_vds_memory_over_commit"));
        entity.setCpuSockets((Integer) rs.getObject("cpu_sockets"));
        entity.setVdsSpmId((Integer) rs.getObject("vds_spm_id"));
        entity.setNetConfigDirty((Boolean) rs.getObject("net_config_dirty"));
        entity.setPmEnabled(rs.getBoolean("pm_enabled"));
        entity.setFenceProxySources(FenceProxySourceTypeHelper.parseFromString(rs.getString("pm_proxy_preferences")));
        entity.setPmKdumpDetection(rs.getBoolean("pm_detect_kdump"));
        entity.setSpmStatus(VdsSpmStatus.forValue(rs.getInt("spm_status")));
        entity.setSupportedClusterLevels(rs.getString("supported_cluster_levels"));


        entity.setSupportedEngines(rs.getString("supported_engines"));
        entity.setClusterCompatibilityVersion(new VersionRowMapper("cluster_compatibility_version").mapRow(rs, rowNum));
        entity.setClusterSupportsVirtService(rs.getBoolean("cluster_virt_service"));
        entity.setClusterSupportsGlusterService(rs.getBoolean("cluster_gluster_service"));
        entity.setHostOs(rs.getString("host_os"));
        entity.setGlusterVersion(new RpmVersion(rs.getString("gluster_version")));
        entity.setLibrbdVersion(new RpmVersion(rs.getString("librbd1_version")));
        entity.setGlusterfsCliVersion(new RpmVersion(rs.getString("glusterfs_cli_version")));
        entity.setOvsVersion(new RpmVersion(rs.getString("openvswitch_version")));
        entity.setNmstateVersion(new RpmVersion(rs.getString("nmstate_version")));
        entity.setKvmVersion(rs.getString("kvm_version"));
        entity.setLibvirtVersion(new RpmVersion(rs.getString("libvirt_version")));
        entity.setSpiceVersion(rs.getString("spice_version"));
        entity.setKernelVersion(rs.getString("kernel_version"));
        entity.setIScsiInitiatorName(rs.getString("iscsi_initiator_name"));
        entity.setTransparentHugePagesState(VdsTransparentHugePagesState
                .forValue(rs.getInt("transparent_hugepages_state")));
        entity.setHooksStr(rs.getString("hooks"));
        entity.setNonOperationalReason(NonOperationalReason.forValue(rs.getInt("non_operational_reason")));
        entity.setOtpValidity(rs.getLong("otp_validity"));
        entity.setVdsSpmPriority(rs.getInt("vds_spm_priority"));
        entity.setAutoRecoverable(rs.getBoolean("recoverable"));
        entity.setSshKeyFingerprint(rs.getString("sshKeyFingerprint"));
        entity.setSshPublicKey(rs.getString("ssh_public_key"));
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
        entity.setDisablePowerManagementPolicy(rs.getBoolean("disable_auto_pm"));
        entity.setPowerManagementControlledByPolicy(rs.getBoolean("controlled_by_pm_policy"));
        entity.setKdumpStatus(KdumpStatus.valueOfNumber(rs.getInt("kdump_status")));
        entity.getSupportedRngSources().addAll(VmRngDevice.csvToSourcesSet(rs.getString("supported_rng_sources")));
        entity.setSELinuxEnforceMode((Integer) rs.getObject("selinux_enforce_mode"));
        entity.setAutoNumaBalancing(AutoNumaBalanceStatus.forValue(rs.getInt("auto_numa_balancing")));
        entity.setNumaSupport(rs.getBoolean("is_numa_supported"));
        entity.setBalloonEnabled(rs.getBoolean("enable_balloon"));
        entity.setCountThreadsAsCores(rs.getBoolean("count_threads_as_cores"));
        entity.setMaintenanceReason(rs.getString("maintenance_reason"));
        entity.setUpdateAvailable(rs.getBoolean("is_update_available"));
        entity.setHostDevicePassthroughEnabled(rs.getBoolean("is_hostdev_enabled"));
        entity.setHostedEngineHost(rs.getBoolean("is_hosted_engine_host"));
        VdsStaticDaoImpl.KernelCmdlineColumn.fromJson(rs.getString("kernel_cmdline")).toVds(entity);
        entity.setLastStoredKernelCmdline(rs.getString("last_stored_kernel_cmdline"));
        entity.setKernelArgs(rs.getString("kernel_args"));
        entity.setFencingEnabled(rs.getBoolean("fencing_enabled"));
        entity.setGlusterPeerStatus(PeerStatus.fromValue(rs.getString("gluster_peer_status")));
        entity.setPrettyName(rs.getString("pretty_name"));
        entity.setHostedEngineConfigured(rs.getBoolean("hosted_engine_configured"));
        entity.setInFenceFlow(rs.getBoolean("in_fence_flow"));
        entity.setReinstallRequired(rs.getBoolean("reinstall_required"));
        entity.setKernelFeatures(ObjectUtils.mapNullable(
                rs.getString("kernel_features"), JsonHelper::jsonToMapUnchecked));
        entity.setVncEncryptionEnabled(rs.getBoolean("vnc_encryption_enabled"));
        entity.setVgpuPlacement(rs.getInt("vgpu_placement"));
        entity.setConnectorInfo(ObjectUtils.mapNullable(
                rs.getString("connector_info"), JsonHelper::jsonToMapUnchecked));
        entity.setBackupEnabled(rs.getBoolean("backup_enabled"));
        entity.setColdBackupEnabled(rs.getBoolean("cold_backup_enabled"));
        entity.setClearBitmapsEnabled(rs.getBoolean("clear_bitmaps_enabled"));
        entity.setSupportedDomainVersionsAsString(rs.getString("supported_domain_versions"));
        entity.setClusterSmtDisabled(rs.getBoolean("cluster_smt_disabled"));
        entity.setSupportedBlockSize(ObjectUtils.mapNullable(
                rs.getString("supported_block_size"), JsonHelper::jsonToMapUnchecked));
        entity.setTscFrequency(rs.getString("tsc_frequency"));
        entity.setTscScalingEnabled(rs.getBoolean("tsc_scaling"));
        entity.setFipsEnabled(rs.getBoolean("fips_enabled"));
        entity.setBootUuid(rs.getString("boot_uuid"));
        entity.setCdChangePdiv(rs.getBoolean("cd_change_pdiv"));
        entity.setOvnConfigured(rs.getBoolean("ovn_configured"));
        return entity;
    };
}
