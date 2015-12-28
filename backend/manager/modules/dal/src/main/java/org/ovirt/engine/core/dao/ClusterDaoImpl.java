package org.ovirt.engine.core.dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ClusterHostsAndVMs;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>ClusterDaoImpl</code> provides an implementation of {@link ClusterDao} that uses code previously
 * found in {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 */
@Named
@Singleton
public class ClusterDaoImpl extends BaseDao implements ClusterDao {
    private static final Logger log = LoggerFactory.getLogger(ClusterDaoImpl.class);

    @Override
    public Cluster get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public Cluster get(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetClusterByClusterId", ClusterRowMapper.instance, parameterSource);
    }

    @Override
    public Cluster getWithRunningVms(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);
        return getCallsHandler().executeRead("GetClusterWithRunningVms", ClusterRowMapper.instance, parameterSource);
    }

    @Override
    public Boolean getIsEmpty(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);
        return getCallsHandler().executeRead("GetIsClusterEmpty", BooleanRowMapper.instance, parameterSource);
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
                        ClusterRowMapper.instance,
                        parameterSource);
    }

    @Override
    public Cluster getByName(String name, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return (Cluster) DbFacadeUtils.asSingleResult(
                getCallsHandler().executeReadList("GetClusterForUserByClusterName",
                        ClusterRowMapper.instance,
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
                ClusterRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<Cluster> getAllWithQuery(String query) {
        List<Cluster> groups = getJdbcTemplate().query(query, ClusterRowMapper.instance);

        try {
            // The UI requires the host and vm count
            return getHostsAndVmsForClusters(groups);
        } catch (Exception e) {
            log.error("Can't load host and vm count for cluster. Query is '{}'. Error: {}", query, e.getMessage());
            log.debug("Excpetion", e);
        }
        return groups;
    }

    @Override
    public List<Cluster> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<Cluster> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromCluster", ClusterRowMapper.instance, parameterSource);
    }

    @Override
    public void save(Cluster group) {
        Guid id = group.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            group.setId(id);
        }
        getCallsHandler().executeModification("InsertCluster", getClusterParamSource(group));
    }

    @Override
    public void update(Cluster group) {
        getCallsHandler().executeModification("UpdateCluster", getClusterParamSource(group));
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
                ClusterRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<Cluster> getClustersHavingHosts() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetClustersHavingHosts",
                ClusterRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<Cluster> getWithoutMigratingVms() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetClustersWithoutMigratingVms",
                ClusterRowMapper.instance,
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
    public int getVmsCountByClusterId(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("cluster_id", clusterId);
        return getCallsHandler().executeRead("GetNumberOfVmsInCluster",
                getIntegerMapper(),
                parameterSource);
    }

    @Override
    public List<Cluster> getTrustedClusters() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("trusted_service", true);
        return getCallsHandler().executeReadList("GetTrustedClusters", ClusterRowMapper.instance, parameterSource);
    }

    private MapSqlParameterSource getClusterParamSource(Cluster group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", group.getDescription())
                .addValue("name", group.getName())
                .addValue("free_text_comment", group.getComment())
                .addValue("cluster_id", group.getId())
                .addValue("cpu_name", group.getCpuName())
                .addValue("storage_pool_id", group.getStoragePoolId())
                .addValue("max_vds_memory_over_commit",
                        group.getMaxVdsMemoryOverCommit())
                .addValue("count_threads_as_cores",
                        group.getCountThreadsAsCores())
                .addValue("transparent_hugepages",
                        group.getTransparentHugepages())
                .addValue("compatibility_version",
                        group.getCompatibilityVersion())
                .addValue("migrate_on_error", group.getMigrateOnError())
                .addValue("virt_service", group.supportsVirtService())
                .addValue("gluster_service", group.supportsGlusterService())
                .addValue("gluster_cli_based_snapshot_scheduled", group.isGlusterCliBasedSchedulingOn())
                .addValue("tunnel_migration", group.isTunnelMigration())
                .addValue("required_rng_sources", VmRngDevice.sourcesToCsv(group.getRequiredRngSources()))
                .addValue("emulated_machine", group.getEmulatedMachine())
                .addValue("detect_emulated_machine", group.isDetectEmulatedMachine())
                .addValue("trusted_service", group.supportsTrustedService())
                .addValue("ha_reservation", group.supportsHaReservation())
                .addValue("optional_reason", group.isOptionalReasonRequired())
                .addValue("maintenance_reason_required", group.isMaintenanceReasonRequired())
                .addValue("cluster_policy_id", group.getClusterPolicyId())
                .addValue("cluster_policy_custom_properties",
                                SerializationFactory.getSerializer().serialize(group.getClusterPolicyProperties()))
                .addValue("architecture", group.getArchitecture())
                .addValue("enable_balloon", group.isEnableBallooning())
                .addValue("optimization_type", group.getOptimizationType())
                .addValue("enable_ksm", group.isEnableKsm())
                .addValue("spice_proxy", group.getSpiceProxy())
                .addValue("serial_number_policy", group.getSerialNumberPolicy() == null ? null : group.getSerialNumberPolicy().getValue())
                .addValue("custom_serial_number", group.getCustomSerialNumber())
                .addValue("skip_fencing_if_sd_active", group.getFencingPolicy().isSkipFencingIfSDActive())
                .addValue("skip_fencing_if_connectivity_broken", group.getFencingPolicy().isSkipFencingIfConnectivityBroken())
                .addValue("hosts_with_broken_connectivity_threshold", group.getFencingPolicy().getHostsWithBrokenConnectivityThreshold())
                .addValue("fencing_enabled", group.getFencingPolicy().isFencingEnabled())
                .addValue("is_auto_converge", group.getAutoConverge())
                .addValue("is_migrate_compressed", group.getMigrateCompressed())
                .addValue("gluster_tuned_profile", group.getGlusterTunedProfile())
                .addValue("ksm_merge_across_nodes", group.isKsmMergeAcrossNumaNodes());

        return parameterSource;
    }

    private final static class ClusterHostsAndVMsRowMapper implements RowMapper<ClusterHostsAndVMs> {
        public static final RowMapper<ClusterHostsAndVMs> instance = new ClusterHostsAndVMsRowMapper();

        @Override
        public ClusterHostsAndVMs mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClusterHostsAndVMs entity = new ClusterHostsAndVMs();
            entity.setHosts(rs.getInt("hosts"));
            entity.setVms(rs.getInt("vms"));
            entity.setClusterId(getGuid(rs, "cluster_id"));
            return entity;
        }

    }
    private final static class ClusterRowMapper implements RowMapper<Cluster> {
        public static final RowMapper<Cluster> instance = new ClusterRowMapper();
        @Override
        public Cluster mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            Cluster entity = new Cluster();
            entity.setDescription(rs.getString("description"));
            entity.setName(rs.getString("name"));
            entity.setId(getGuidDefaultEmpty(rs, "cluster_id"));
            entity.setComment(rs.getString("free_text_comment"));
            entity.setCpuName(rs.getString("cpu_name"));
            entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
            entity.setStoragePoolName(rs
                    .getString("storage_pool_name"));
            entity.setMaxVdsMemoryOverCommit(rs
                    .getInt("max_vds_memory_over_commit"));
            entity.setCountThreadsAsCores(rs
                    .getBoolean("count_threads_as_cores"));
            entity.setTransparentHugepages(rs
                    .getBoolean("transparent_hugepages"));
            entity.setCompatibilityVersion(new Version(rs
                    .getString("compatibility_version")));
            entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
            entity.setVirtService(rs.getBoolean("virt_service"));
            entity.setGlusterService(rs.getBoolean("gluster_service"));
            entity.setGlusterCliBasedSchedulingOn(rs.getBoolean("gluster_cli_based_snapshot_scheduled"));
            entity.setTunnelMigration(rs.getBoolean("tunnel_migration"));
            entity.getRequiredRngSources().addAll(VmRngDevice.csvToSourcesSet(rs.getString("required_rng_sources")));
            entity.setEmulatedMachine(rs.getString("emulated_machine"));
            entity.setDetectEmulatedMachine(rs.getBoolean("detect_emulated_machine"));
            entity.setTrustedService(rs.getBoolean("trusted_service"));
            entity.setHaReservation(rs.getBoolean("ha_reservation"));
            entity.setOptionalReasonRequired(rs.getBoolean("optional_reason"));
            entity.setMaintenanceReasonRequired(rs.getBoolean("maintenance_reason_required"));
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
            entity.getFencingPolicy().setHostsWithBrokenConnectivityThreshold(rs.getInt("hosts_with_broken_connectivity_threshold"));
            entity.getFencingPolicy().setFencingEnabled(rs.getBoolean("fencing_enabled"));
            entity.setAutoConverge((Boolean) rs.getObject("is_auto_converge"));
            entity.setMigrateCompressed((Boolean) rs.getObject("is_migrate_compressed"));
            entity.setGlusterTunedProfile(rs.getString("gluster_tuned_profile"));
            entity.setKsmMergeAcrossNumaNodes(rs.getBoolean("ksm_merge_across_nodes"));

            return entity;
        }
    }

    private final static class BooleanRowMapper implements RowMapper<Boolean> {
        public static final RowMapper<Boolean> instance = new BooleanRowMapper();

        @Override
        public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Boolean.valueOf(rs.getBoolean(1));
        }
    }

    @Override
    public List<Cluster> getClustersByClusterPolicyId(Guid clusterPolicyId) {
        return getCallsHandler().executeReadList("GetClustersByClusterPolicyId",
                ClusterRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_policy_id", clusterPolicyId));
    }

    protected List<Cluster> getHostsAndVmsForClusters(List<Cluster> clusters) throws Exception {
        Map<Guid, Cluster> groupsById = new HashMap<>();
        for (Cluster cluster : clusters) {
            groupsById.put(cluster.getId(), cluster);
        }
        Connection c = getJdbcTemplate().getDataSource().getConnection();
        Array groups = c.createArrayOf("uuid", groupsById.keySet().toArray());
        List<ClusterHostsAndVMs> dataList = getCallsHandler().executeReadList("GetHostsAndVmsForClusters",
                ClusterHostsAndVMsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_ids", groups));

        c.close();
        for (ClusterHostsAndVMs groupDetail : dataList) {
            groupsById.get(groupDetail.getClusterId()).setGroupHostsAndVms(groupDetail);
        }
        //The VDS groups have been updated, but we want to keep the order, so return the original list which is
        //in the right order.
        return clusters;

    }

    @Override
    public List<Cluster> getClustersByServiceAndCompatibilityVersion(boolean glusterService, boolean virtService, String compatibilityVersion) {
        return getCallsHandler().executeReadList("GetClustersByServiceAndCompatibilityVersion",
                ClusterRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("gluster_service", glusterService)
                        .addValue("virt_service", virtService)
                        .addValue("compatibility_version", compatibilityVersion));
    }
}
