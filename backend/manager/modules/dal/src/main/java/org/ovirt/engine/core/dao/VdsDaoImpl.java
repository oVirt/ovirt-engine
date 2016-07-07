package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>VdsDaoImpl</code> provides an implementation of {@code VdsDao} that uses previously written code from
 * {@code DbFacade}.
 */
@Named
@Singleton
public class VdsDaoImpl extends BaseDao implements VdsDao {

    @Override
    public VDS get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VDS get(Guid id, Guid userID, boolean isFiltered) {
        // several rows may be returned because of join with fence agents table.
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByVdsId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
        return vdsList.size() == 0 ? null : uniteAgentsSingleVds(vdsList);
    }

    @Override
    public VDS getByName(String name) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByName",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_name", name));
        return vdsList.size() == 0 ? null : uniteAgentsSingleVds(vdsList);
    }

    @Override
    public List<VDS> getAllForHostname(String hostname) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByHostName",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", hostname));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllWithUniqueId(String id) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByUniqueID",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_unique_id", id));
        return uniteAgents(vdsList);
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
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByType",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_type", type));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllForClusterWithoutMigrating(Guid clusterId) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsWithoutMigratingVmsByClusterId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllWithQuery(String query) {
        List<VDS> vdsList = getJdbcTemplate().query(query, VdsRowMapper.instance);
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VDS> getAll(Guid userID, boolean isFiltered) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetAllFromVds",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllForCluster(Guid cluster) {
        return getAllForCluster(cluster, null, false);
    }

    @Override
    public List<VDS> getAllForCluster(Guid cluster, Guid userID, boolean isFiltered) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByClusterId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", cluster)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getHostsForStorageOperation(Guid storagePoolId, boolean localFsOnly) {
        // normalize to uniquely represent in DB that we want candidates from all DC's
        if (storagePoolId == null || storagePoolId.equals(Guid.Empty)) {
            storagePoolId = null;
        }
        List<VDS> vdsList = getCallsHandler().executeReadList("getHostsForStorageOperation",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                    .addValue("storage_pool_id", storagePoolId)
                    .addValue("local_fs_only", localFsOnly));
        return uniteAgents(vdsList);
    }

    @Override
    public VDS getFirstUpRhelForCluster(Guid clsuterId) {
        List<VDS> vds = getCallsHandler().executeReadList("getFirstUpRhelForClusterId",
                VdsRowMapper.instance,
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
        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByStoragePoolId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePool)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllForClusterWithStatus(Guid clusterId, VDSStatus status) {
        List<VDS> vdsList = getCallsHandler().executeReadList("getVdsForClusterWithStatus",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("status", status.getValue()));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllForClusterWithStatusAndPeerStatus(Guid clusterId, VDSStatus status, PeerStatus peerStatus) {
        List<VDS> vdsList = getCallsHandler().executeReadList("getVdsForClusterWithPeerStatus",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("status", status.getValue())
                        .addValue("peer_status", peerStatus.name()));
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllForStoragePoolAndStatus(Guid storagePool, VDSStatus status) {
        return getAllForStoragePoolAndStatuses(storagePool, status != null ? EnumSet.of(status) : null);
    }

    @Override
    public List<VDS> getAllForStoragePoolAndStatuses(Guid storagePool, Set<VDSStatus> statuses) {
        List<VDS> vdsList = getCallsHandler().executeReadList("getVdsByStoragePoolIdWithStatuses",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePool)
                        .addValue("statuses",
                                statuses == null?
                                        null :
                                        statuses.stream()
                                                .map(VDSStatus::getValue)
                                                .map(Object::toString)
                                                .collect(Collectors.joining(","))));
        return uniteAgents(vdsList);

    }

    @Override
    public List<VDS> getListForSpmSelection(Guid storagePoolId) {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetUpAndPrioritizedVds",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId));
        return uniteAgentsPreserveSpmPrioritySorting(vdsList);
    }

    @Override
    public List<VDS> listFailedAutorecoverables() {
        List<VDS> vdsList = getCallsHandler().executeReadList("GetFailingVdss", VdsRowMapper.instance, null);
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllForNetwork(Guid networkId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", networkId);

        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsByNetworkId",
                VdsRowMapper.instance,
                parameterSource);
        return uniteAgents(vdsList);
    }

    @Override
    public List<VDS> getAllWithoutNetwork(Guid networkId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", networkId);

        List<VDS> vdsList = getCallsHandler().executeReadList("GetVdsWithoutNetwork",
                VdsRowMapper.instance,
                parameterSource);
        return uniteAgents(vdsList);
    }

    static final class VdsRowMapper implements RowMapper<VDS> {
        // single instance
        public static final VdsRowMapper instance = new VdsRowMapper();

        @Override
        public VDS mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final VDS entity = new VDS();
            entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
            entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
            entity.setClusterName(rs.getString("cluster_name"));
            entity.setClusterDescription(rs
                    .getString("cluster_description"));
            entity.setVdsName(rs.getString("vds_name"));
            entity.setComment(rs.getString("free_text_comment"));
            entity.setUniqueId(rs.getString("vds_unique_id"));
            entity.setServerSslEnabled(rs
                    .getBoolean("server_SSL_enabled"));
            entity.setHostName(rs.getString("host_name"));
            entity.setPort(rs.getInt("port"));
            entity.setProtocol(VdsProtocol.fromValue(rs.getInt("protocol")));
            entity.setSshPort(rs.getInt("ssh_port"));
            entity.setSshUsername(rs.getString("ssh_username"));
            entity.setStatus(VDSStatus.forValue(rs.getInt("status")));
            entity.setExternalStatus(ExternalStatus.forValue(rs.getInt("external_status")));
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
            entity.setIncomingMigrations(rs.getInt("incoming_migrations"));
            entity.setOutgoingMigrations(rs.getInt("outgoing_migrations"));
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
            entity.setMemFree(rs.getLong("mem_free"));
            entity.setVdsType(VDSType.forValue(rs.getInt("vds_type")));
            entity.setCpuFlags(rs.getString("cpu_flags"));
            entity.setClusterCpuName(rs.getString("cluster_cpu_name"));
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
            entity.setPmEnabled(rs.getBoolean("pm_enabled"));
            entity.setFenceProxySources(
                    FenceProxySourceTypeHelper.parseFromString(rs.getString("pm_proxy_preferences")));
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
            entity.setClusterCompatibilityVersion(new Version(rs
                    .getString("cluster_compatibility_version")));
            entity.setClusterSupportsVirtService(rs.getBoolean("cluster_virt_service"));
            entity.setClusterSupportsGlusterService(rs.getBoolean("cluster_gluster_service"));
            entity.setHostOs(rs.getString("host_os"));
            entity.setGlusterVersion(new RpmVersion(rs.getString("gluster_version")));
            entity.setLibrbdVersion(new RpmVersion(rs.getString("librbd1_version")));
            entity.setGlusterfsCliVersion(new RpmVersion(rs.getString("glusterfs_cli_version")));
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
            entity.setBootTime((Long) rs.getObject("boot_time"));
            entity.setSELinuxEnforceMode((Integer) rs.getObject("selinux_enforce_mode"));
            entity.setAutoNumaBalancing(AutoNumaBalanceStatus.forValue(rs.getInt("auto_numa_balancing")));
            entity.setNumaSupport(rs.getBoolean("is_numa_supported"));
            entity.setBalloonEnabled(rs.getBoolean("enable_balloon"));
            entity.setCountThreadsAsCores(rs.getBoolean("count_threads_as_cores"));
            entity.setMaintenanceReason(rs.getString("maintenance_reason"));
            entity.getStaticData().setOpenstackNetworkProviderId(
                    getGuid(rs, "openstack_network_provider_id"));
            Guid agentGuid = getGuid(rs, "agent_id");
            if (agentGuid != null) {
                FenceAgent agent = new FenceAgent();
                agent.setId(agentGuid);
                agent.setHostId(getGuid(rs, "vds_id"));
                agent.setOrder(rs.getInt("agent_order"));
                agent.setType(rs.getString("agent_type"));
                agent.setUser(rs.getString("agent_user"));
                agent.setPassword(DbFacadeUtils.decryptPassword(rs.getString("agent_password")));
                int port = rs.getInt("agent_port");
                agent.setPort(port == 0 ? null : port);
                agent.setEncryptOptions(rs.getBoolean("agent_encrypt_options"));
                if (agent.getEncryptOptions()) {
                    agent.setOptions(DbFacadeUtils.decryptPassword(rs.getString("agent_options")));
                } else {
                    agent.setOptions(rs.getString("agent_options"));
                }
                agent.setIp(rs.getString("agent_ip"));
                entity.getFenceAgents().add(agent);
            }
            entity.setUpdateAvailable(rs.getBoolean("is_update_available"));
            entity.setHostDevicePassthroughEnabled(rs.getBoolean("is_hostdev_enabled"));
            entity.setHostedEngineHost(rs.getBoolean("is_hosted_engine_host"));
            VdsStaticDaoImpl.KernelCmdlineColumn.fromJson(rs.getString("kernel_cmdline")).toVds(entity);
            entity.setLastStoredKernelCmdline(rs.getString("last_stored_kernel_cmdline"));
            entity.setKernelArgs(rs.getString("kernel_args"));
            entity.setGlusterPeerStatus(PeerStatus.fromValue(rs.getString("gluster_peer_status")));
            entity.setPrettyName(rs.getString("pretty_name"));
            return entity;
        }
    }

    /**
     * Unit the fence agents from potentially multiple VDS (they are the same VDS, they just have multiple rows
     * in the result set), while maintaining the order in which the VDSs were returned by the query.
     * @param vdsList The list of VDS (with potentially multiple VDS being the same with different fence agents).
     * @return A list of VDS where each VDS is unique (according to the GUID) and each VDS can contain potentially
     * multiple fence agents.
     */
    private List<VDS> uniteAgents(List<VDS> vdsList) {
        Map<Guid, VDS> vdsMap = new HashMap<>();
        List<VDS> results = new ArrayList<>();
        for (VDS vds: vdsList) {
            Guid vdsId = vds.getId();
            VDS usedVds = vdsMap.get(vdsId);
            if (usedVds == null) {
                results.add(vds);
                vdsMap.put(vdsId, vds);
            } else {
                usedVds.getFenceAgents().addAll(vds.getFenceAgents());
            }
        }
        return results;
    }

    private VDS uniteAgentsSingleVds(List<VDS> vdsList) {
        return uniteAgents(vdsList).get(0);
    }

    private List<VDS> uniteAgentsPreserveSpmPrioritySorting(List<VDS> vdsList) {

        List<VDS> results = uniteAgents(vdsList);
        // insure that list is ordered according to SPM priority DESC
        Collections.sort(results, new HostSpmPriorityComparator());
        return results;

    }

    private static class HostSpmPriorityComparator implements Comparator<VDS>, Serializable {
        @Override
        public int compare(VDS host1, VDS host2) {
            return Integer.compare(host2.getVdsSpmPriority(), host1.getVdsSpmPriority());
        }
    }
}
