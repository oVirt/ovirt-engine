package org.ovirt.engine.core.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ClusterHostsAndVMs;
import org.ovirt.engine.core.common.businessentities.FipsMode;
import org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.network.FirewallType;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code ClusterDaoImpl} provides an implementation of {@link ClusterDao}.
 *
 */
@Named
@Singleton
public class ClusterDaoImpl extends BaseDao implements ClusterDao {

    @Override
    public Cluster get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public Cluster get(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetClusterByClusterId", clusterRowMapper, parameterSource);
    }

    @Override
    public Cluster getWithRunningVms(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);
        return getCallsHandler().executeRead("GetClusterWithRunningVms", clusterRowMapper, parameterSource);
    }

    @Override
    public Boolean getIsEmpty(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);
        return getCallsHandler().executeRead
                ("GetIsClusterEmpty", SingleColumnRowMapper.newInstance(Boolean.class), parameterSource);
    }

    @Override
    public Cluster getByName(String name) {
        return (Cluster) DbFacadeUtils.asSingleResult(getByName(name, true));
    }

    @Override
    public List<Cluster> getByName(String name, boolean isCaseSensitive) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_name", name)
                .addValue("is_case_sensitive", isCaseSensitive);

        return getCallsHandler().executeReadList("GetClusterByClusterName",
                        clusterRowMapper,
                        parameterSource);
    }

    @Override
    public Cluster getByName(String name, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return (Cluster) DbFacadeUtils.asSingleResult(
                getCallsHandler().executeReadList("GetClusterForUserByClusterName",
                        clusterRowMapper,
                        parameterSource));
    }

    @Override
    public List<Cluster> getAllForStoragePool(Guid id) {
        return getAllForStoragePool(id, null, false);
    }

    @Override
    public List<Cluster> getAllForStoragePool(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetClustersByStoragePoolId",
                clusterRowMapper,
                parameterSource);
    }

    @Override
    public List<Cluster> getAllWithQuery(String query) {
        List<Cluster> clusters = getJdbcTemplate().query(query, clusterRowMapper);
        return getHostsAndVmsForClusters(clusters);
    }

    @Override
    public List<Cluster> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<Cluster> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromCluster", clusterRowMapper, parameterSource);
    }

    @Override
    public void save(Cluster cluster) {
        Guid id = cluster.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            cluster.setId(id);
        }
        getCallsHandler().executeModification("InsertCluster", getClusterParamSource(cluster));
    }

    @Override
    public void update(Cluster cluster) {
        getCallsHandler().executeModification("UpdateCluster", getClusterParamSource(cluster));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);

        getCallsHandler().executeModification("DeleteCluster", parameterSource);
    }

    @Override
    public List<Cluster> getClustersWithPermittedAction(Guid userId, ActionGroup actionGroup) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId());

        return getCallsHandler().executeReadList("fn_perms_get_clusters_with_permitted_action",
                clusterRowMapper,
                parameterSource);
    }

    @Override
    public List<Cluster> getClustersHavingHosts() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetClustersHavingHosts",
                clusterRowMapper,
                parameterSource);
    }

    @Override
    public List<Cluster> getWithoutMigratingVms() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetClustersWithoutMigratingVms",
                clusterRowMapper,
                parameterSource);
    }

    @Override
    public void setEmulatedMachine(Guid clusterId, String emulatedMachine, boolean detectEmulatedMachine) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId)
                .addValue("emulated_machine", emulatedMachine)
                .addValue("detect_emulated_machine", detectEmulatedMachine);

        getCallsHandler().executeModification("UpdateClusterEmulatedMachine", parameterSource);
    }

    @Override
    public boolean setUpgradeRunning(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId);

        Map<String, Object> results =
                getCallsHandler().executeModification("SetClusterUpgradeRunning", parameterSource);

        return (Boolean) results.get("updated");
    }

    @Override
    public boolean clearUpgradeRunning(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId);

        Map<String, Object> results =
                getCallsHandler().executeModification("ClearClusterUpgradeRunning", parameterSource);
        return (Boolean) results.get("updated");
    }

    @Override
    public void clearAllUpgradeRunning() {
        getCallsHandler().executeModification("ClearAllClusterUpgradeRunning", getCustomMapSqlParameterSource());
    }

    @Override
    public int getVmsCountByClusterId(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("cluster_id", clusterId);
        return getCallsHandler().executeRead("GetNumberOfVmsInCluster",
                SingleColumnRowMapper.newInstance(Integer.class),
                parameterSource);
    }

    @Override
    public List<Cluster> getTrustedClusters() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("trusted_service", true);
        return getCallsHandler().executeReadList("GetTrustedClusters", clusterRowMapper, parameterSource);
    }

    private MapSqlParameterSource getClusterParamSource(Cluster cluster) {
        return getCustomMapSqlParameterSource()
                .addValue("description", cluster.getDescription())
                .addValue("name", cluster.getName())
                .addValue("free_text_comment", cluster.getComment())
                .addValue("cluster_id", cluster.getId())
                .addValue("cpu_name", cluster.getCpuName())
                .addValue("cpu_flags", cluster.getCpuFlags())
                .addValue("cpu_verb", cluster.getCpuVerb())
                .addValue("storage_pool_id", cluster.getStoragePoolId())
                .addValue("max_vds_memory_over_commit",
                        cluster.getMaxVdsMemoryOverCommit())
                .addValue("smt_disabled", cluster.getSmtDisabled())
                .addValue("count_threads_as_cores",
                        cluster.getCountThreadsAsCores())
                .addValue("upgrade_running",
                        cluster.isUpgradeRunning())
                .addValue("transparent_hugepages",
                        cluster.getTransparentHugepages())
                .addValue("compatibility_version",
                        cluster.getCompatibilityVersion())
                .addValue("migrate_on_error", cluster.getMigrateOnError())
                .addValue("virt_service", cluster.supportsVirtService())
                .addValue("gluster_service", cluster.supportsGlusterService())
                .addValue("gluster_cli_based_snapshot_scheduled", cluster.isGlusterCliBasedSchedulingOn())
                .addValue("tunnel_migration", cluster.isTunnelMigration())
                .addValue("additional_rng_sources", VmRngDevice.sourcesToCsv(cluster.getAdditionalRngSources()))
                .addValue("emulated_machine", cluster.getEmulatedMachine())
                .addValue("detect_emulated_machine", cluster.isDetectEmulatedMachine())
                .addValue("bios_type", cluster.getBiosType())
                .addValue("trusted_service", cluster.supportsTrustedService())
                .addValue("ha_reservation", cluster.supportsHaReservation())
                .addValue("cluster_policy_id", cluster.getClusterPolicyId())
                .addValue("cluster_policy_custom_properties",
                                SerializationFactory.getSerializer().serialize(cluster.getClusterPolicyProperties()))
                .addValue("architecture", cluster.getArchitecture())
                .addValue("enable_balloon", cluster.isEnableBallooning())
                .addValue("optimization_type", cluster.getOptimizationType())
                .addValue("enable_ksm", cluster.isEnableKsm())
                .addValue("spice_proxy", cluster.getSpiceProxy())
                .addValue("serial_number_policy", cluster.getSerialNumberPolicy() == null ? null : cluster.getSerialNumberPolicy().getValue())
                .addValue("custom_serial_number", cluster.getCustomSerialNumber())
                .addValue("skip_fencing_if_sd_active", cluster.getFencingPolicy().isSkipFencingIfSDActive())
                .addValue("skip_fencing_if_connectivity_broken", cluster.getFencingPolicy().isSkipFencingIfConnectivityBroken())
                .addValue("hosts_with_broken_connectivity_threshold", cluster.getFencingPolicy().getHostsWithBrokenConnectivityThreshold())
                .addValue("fencing_enabled", cluster.getFencingPolicy().isFencingEnabled())
                .addValue("is_auto_converge", cluster.getAutoConverge())
                .addValue("is_migrate_compressed", cluster.getMigrateCompressed())
                .addValue("is_migrate_encrypted", cluster.getMigrateEncrypted())
                .addValue("gluster_tuned_profile", cluster.getGlusterTunedProfile())
                .addValue("ksm_merge_across_nodes", cluster.isKsmMergeAcrossNumaNodes())
                .addValue("migration_bandwidth_limit_type", cluster.getMigrationBandwidthLimitType().name())
                .addValue("custom_migration_bandwidth_limit", cluster.getCustomMigrationNetworkBandwidth())
                .addValue("migration_policy_id", cluster.getMigrationPolicyId())
                .addValue("mac_pool_id", cluster.getMacPoolId())
                .addValue("switch_type", cluster.getRequiredSwitchTypeForCluster().getOptionValue())
                .addValue("skip_fencing_if_gluster_bricks_up", cluster.getFencingPolicy().isSkipFencingIfGlusterBricksUp())
                .addValue("skip_fencing_if_gluster_quorum_not_met", cluster.getFencingPolicy().isSkipFencingIfGlusterQuorumNotMet())
                .addValue("firewall_type", cluster.getFirewallType())
                .addValue("default_network_provider_id", cluster.getDefaultNetworkProviderId())
                .addValue("log_max_memory_used_threshold", cluster.getLogMaxMemoryUsedThreshold())
                .addValue("log_max_memory_used_threshold_type", cluster.getLogMaxMemoryUsedThresholdType().getValue())
                .addValue("vnc_encryption_enabled", cluster.isVncEncryptionEnabled())
                .addValue("managed", cluster.isManaged())
                .addValue("fips_mode", cluster.getFipsMode());
    }

    private static final RowMapper<ClusterHostsAndVMs> clusterHostsAndVMsRowMapper = (rs, rowNum) -> {
        ClusterHostsAndVMs entity = new ClusterHostsAndVMs();
        entity.setHosts(rs.getInt("hosts"));
        entity.setVms(rs.getInt("vms"));
        entity.setHostsWithUpdateAvailable(rs.getInt("hosts_with_update_available"));
        entity.setClusterId(getGuid(rs, "cluster_id"));
        return entity;
    };

    private static final RowMapper<Cluster> clusterRowMapper = (rs, rowNum) -> {
        Cluster entity = new Cluster();
        entity.setDescription(rs.getString("description"));
        entity.setName(rs.getString("name"));
        entity.setId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setComment(rs.getString("free_text_comment"));
        entity.setCpuName(rs.getString("cpu_name"));
        entity.setCpuFlags(rs.getString("cpu_flags"));
        entity.setCpuVerb(rs.getString("cpu_verb"));
        entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
        entity.setStoragePoolName(rs.getString("storage_pool_name"));
        entity.setMaxVdsMemoryOverCommit(rs.getInt("max_vds_memory_over_commit"));
        entity.setSmtDisabled(rs.getBoolean("smt_disabled"));
        entity.setCountThreadsAsCores(rs.getBoolean("count_threads_as_cores"));
        entity.setUpgradeRunning(rs.getBoolean("upgrade_running"));
        entity.setTransparentHugepages(rs.getBoolean("transparent_hugepages"));
        entity.setCompatibilityVersion(new VersionRowMapper("compatibility_version").mapRow(rs, rowNum));
        entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
        entity.setVirtService(rs.getBoolean("virt_service"));
        entity.setGlusterService(rs.getBoolean("gluster_service"));
        entity.setGlusterCliBasedSchedulingOn(rs.getBoolean("gluster_cli_based_snapshot_scheduled"));
        entity.setTunnelMigration(rs.getBoolean("tunnel_migration"));
        entity.getAdditionalRngSources().addAll(VmRngDevice.csvToSourcesSet(rs.getString("additional_rng_sources")));
        entity.setEmulatedMachine(rs.getString("emulated_machine"));
        entity.setDetectEmulatedMachine(rs.getBoolean("detect_emulated_machine"));
        entity.setBiosType(BiosType.forValue(rs.getInt("bios_type")));
        entity.setTrustedService(rs.getBoolean("trusted_service"));
        entity.setHaReservation(rs.getBoolean("ha_reservation"));
        entity.setClusterPolicyId(Guid.createGuidFromString(rs.getString("cluster_policy_id")));
        entity.setClusterPolicyName(rs.getString("cluster_policy_name"));
        entity.setClusterPolicyProperties(SerializationFactory.getDeserializer()
                .deserializeOrCreateNew(rs.getString("cluster_policy_custom_properties"), LinkedHashMap.class));
        entity.setEnableBallooning(rs.getBoolean("enable_balloon"));
        entity.setEnableKsm(rs.getBoolean("enable_ksm"));
        entity.setArchitecture(ArchitectureType.forValue(rs.getInt("architecture")));
        entity.setOptimizationType(OptimizationType.from(rs.getInt("optimization_type")));
        entity.setSpiceProxy(rs.getString("spice_proxy"));
        entity.setSerialNumberPolicy(SerialNumberPolicy.forValue((Integer) rs.getObject("serial_number_policy")));
        entity.setCustomSerialNumber(rs.getString("custom_serial_number"));
        entity.getFencingPolicy().setSkipFencingIfSDActive(rs.getBoolean("skip_fencing_if_sd_active"));
        entity.getFencingPolicy().setSkipFencingIfConnectivityBroken(rs.getBoolean("skip_fencing_if_connectivity_broken"));
        entity.getFencingPolicy().setSkipFencingIfGlusterBricksUp(rs.getBoolean("skip_fencing_if_gluster_bricks_up"));
        entity.getFencingPolicy().setSkipFencingIfGlusterQuorumNotMet(rs.getBoolean("skip_fencing_if_gluster_quorum_not_met"));
        entity.getFencingPolicy().setHostsWithBrokenConnectivityThreshold(rs.getInt("hosts_with_broken_connectivity_threshold"));
        entity.getFencingPolicy().setFencingEnabled(rs.getBoolean("fencing_enabled"));
        entity.setAutoConverge((Boolean) rs.getObject("is_auto_converge"));
        entity.setMigrateCompressed((Boolean) rs.getObject("is_migrate_compressed"));
        entity.setMigrateEncrypted((Boolean) rs.getObject("is_migrate_encrypted"));
        entity.setGlusterTunedProfile(rs.getString("gluster_tuned_profile"));
        entity.setKsmMergeAcrossNumaNodes(rs.getBoolean("ksm_merge_across_nodes"));
        entity.setMigrationBandwidthLimitType(MigrationBandwidthLimitType.valueOf(rs.getString("migration_bandwidth_limit_type")));
        entity.setCustomMigrationNetworkBandwidth(getInteger(rs, "custom_migration_bandwidth_limit"));
        entity.setMigrationPolicyId(getGuid(rs, "migration_policy_id"));
        entity.setMacPoolId(getGuid(rs, "mac_pool_id"));
        entity.setRequiredSwitchTypeForCluster(SwitchType.parse(rs.getString("switch_type")));
        entity.setFirewallType(FirewallType.valueOf(rs.getInt("firewall_type")));
        entity.setDefaultNetworkProviderId(getGuid(rs, "default_network_provider_id"));
        entity.setLogMaxMemoryUsedThreshold(rs.getInt("log_max_memory_used_threshold"));
        entity.setLogMaxMemoryUsedThresholdType(LogMaxMemoryUsedThresholdType.forValue(rs.getInt("log_max_memory_used_threshold_type")));
        entity.setVncEncryptionEnabled(rs.getBoolean("vnc_encryption_enabled"));
        entity.setManaged(rs.getBoolean("managed"));
        entity.setFipsMode(FipsMode.forValue(rs.getInt("fips_mode")));
        return entity;
    };

    @Override
    public List<Cluster> getClustersByClusterPolicyId(Guid clusterPolicyId) {
        return getCallsHandler().executeReadList("GetClustersByClusterPolicyId",
                clusterRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_policy_id", clusterPolicyId));
    }

    protected List<Cluster> getHostsAndVmsForClusters(List<Cluster> clusters) {
        Map<Guid, Cluster> clustersById = new HashMap<>();
        for (Cluster cluster : clusters) {
            clustersById.put(cluster.getId(), cluster);
        }
        List<ClusterHostsAndVMs> dataList = getCallsHandler().executeReadList(
                "GetHostsAndVmsForClusters",
                clusterHostsAndVMsRowMapper,
                getCustomMapSqlParameterSource().addValue("cluster_ids", createArrayOf("uuid", clustersById.keySet().toArray())));

        for (ClusterHostsAndVMs clusterDetail : dataList) {
            clustersById.get(clusterDetail.getClusterId()).setClusterHostsAndVms(clusterDetail);
        }
        //The VDS clusters have been updated, but we want to keep the order, so return the original list which is
        //in the right order.
        return clusters;
    }

    @Override
    public List<Cluster> getClustersByServiceAndCompatibilityVersion(boolean glusterService, boolean virtService, String compatibilityVersion) {
        return getCallsHandler().executeReadList("GetClustersByServiceAndCompatibilityVersion",
                clusterRowMapper,
                getCustomMapSqlParameterSource().addValue("gluster_service", glusterService)
                        .addValue("virt_service", virtService)
                        .addValue("compatibility_version", compatibilityVersion));
    }

    @Override
    public List<Cluster> getAllClustersByMacPoolId(Guid macPoolId) {
        return getCallsHandler().executeReadList("GetAllClustersByMacPoolId",
                clusterRowMapper,
                getCustomMapSqlParameterSource().addValue("id", macPoolId));
    }

    @Override
    public List<Cluster> getAllClustersByDefaultNetworkProviderId(Guid defaultNetworkProviderId) {
        return getCallsHandler().executeReadList("GetAllClustersByDefaultNetworkProviderId",
                clusterRowMapper,
                getCustomMapSqlParameterSource().addValue("id", defaultNetworkProviderId));
    }

    @Override
    public Guid getClusterIdForHostByNameOrAddress(String hostName, String hostAddress) {
        return getCallsHandler().executeRead("GetClusterIdForHostByNameOrAddress",
                SingleColumnRowMapper.newInstance(Guid.class),
                getCustomMapSqlParameterSource()
                   .addValue("vds_name", hostName)
                   .addValue("host_address", hostAddress));
    }
}
