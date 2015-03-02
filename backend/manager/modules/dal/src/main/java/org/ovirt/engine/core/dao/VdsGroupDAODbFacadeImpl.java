package org.ovirt.engine.core.dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSGroupHostsAndVMs;
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
 * <code>VdsGroupDAODbFacadeImpl</code> provides an implementation of {@link VdsGroupDAO} that uses code previously
 * found in {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 */
public class VdsGroupDAODbFacadeImpl extends BaseDAODbFacade implements VdsGroupDAO {
    private static final Logger log = LoggerFactory.getLogger(VdsGroupDAODbFacadeImpl.class);

    @Override
    public VDSGroup get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VDSGroup get(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetVdsGroupByVdsGroupId", VdsGroupRowMapper.instance, parameterSource);
    }

    @Override
    public VDSGroup getWithRunningVms(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id);
        return getCallsHandler().executeRead("GetVdsGroupWithRunningVms", VdsGroupRowMapper.instance, parameterSource);
    }

    @Override
    public Boolean getIsEmpty(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id);
        return getCallsHandler().executeRead("GetIsVdsGroupEmpty", BooleanRowMapper.instance, parameterSource);
    }

    @Override
    public VDSGroup getByName(String name) {
        return (VDSGroup) DbFacadeUtils.asSingleResult(getByName(name, true));
    }

    @Override
    public List<VDSGroup> getByName(String name, boolean isCaseSensitive) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_name", name)
                .addValue("is_case_sensitive", isCaseSensitive);

        return getCallsHandler().executeReadList("GetVdsGroupByVdsGroupName",
                        VdsGroupRowMapper.instance,
                        parameterSource);
    }

    @Override
    public VDSGroup getByName(String name, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return (VDSGroup) DbFacadeUtils.asSingleResult(
                getCallsHandler().executeReadList("GetVdsGroupForUserByVdsGroupName",
                        VdsGroupRowMapper.instance,
                        parameterSource));
    }

    @Override
    public List<VDSGroup> getAllForStoragePool(Guid id) {
        return getAllForStoragePool(id, null, false);
    }

    @Override
    public List<VDSGroup> getAllForStoragePool(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetVdsGroupsByStoragePoolId",
                VdsGroupRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<VDSGroup> getAllWithQuery(String query) {
        List<VDSGroup> groups = jdbcTemplate.query(query, VdsGroupRowMapper.instance);

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
    public List<VDSGroup> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VDSGroup> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromVdsGroups", VdsGroupRowMapper.instance, parameterSource);
    }

    @Override
    public void save(VDSGroup group) {
        Guid id = group.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            group.setId(id);
        }
        getCallsHandler().executeModification("InsertVdsGroups", getVdsGroupParamSource(group));
    }

    @Override
    public void update(VDSGroup group) {
        getCallsHandler().executeModification("UpdateVdsGroup", getVdsGroupParamSource(group));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id);

        getCallsHandler().executeModification("DeleteVdsGroup", parameterSource);
    }

    @Override
    public List<VDSGroup> getClustersWithPermittedAction(Guid userId, ActionGroup actionGroup) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId());

        return getCallsHandler().executeReadList("fn_perms_get_vds_groups_with_permitted_action",
                VdsGroupRowMapper.instance,
                parameterSource);
    }

    @Override
    public void setEmulatedMachine(Guid vdsGroupId, String emulatedMachine, boolean detectEmulatedMachine) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", vdsGroupId)
                .addValue("emulated_machine", emulatedMachine)
                .addValue("detect_emulated_machine", detectEmulatedMachine);

        getCallsHandler().executeModification("UpdateVdsGroupEmulatedMachine", parameterSource);
    }

    @Override
    public int getVmsCountByClusterId(Guid vdsGroupId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vds_group_id", vdsGroupId);
        return getCallsHandler().executeRead("GetNumberOfVmsInCluster",
                getIntegerMapper(),
                parameterSource);
    }

    @Override
    public List<VDSGroup> getTrustedClusters() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("trusted_service", true);
        return getCallsHandler().executeReadList("GetTrustedVdsGroups", VdsGroupRowMapper.instance, parameterSource);
    }

    private MapSqlParameterSource getVdsGroupParamSource(VDSGroup group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", group.getDescription())
                .addValue("name", group.getName())
                .addValue("free_text_comment", group.getComment())
                .addValue("vds_group_id", group.getId())
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
                .addValue("tunnel_migration", group.isTunnelMigration())
                .addValue("required_rng_sources", VmRngDevice.sourcesToCsv(group.getRequiredRngSources()))
                .addValue("emulated_machine", group.getEmulatedMachine())
                .addValue("detect_emulated_machine", group.isDetectEmulatedMachine())
                .addValue("trusted_service", group.supportsTrustedService())
                .addValue("ha_reservation", group.supportsHaReservation())
                .addValue("optional_reason", group.isOptionalReasonRequired())
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
                .addValue("is_migrate_compressed", group.getMigrateCompressed());

        return parameterSource;
    }

    private final static class VDSGroupHostsAndVMsRowMapper implements RowMapper<VDSGroupHostsAndVMs> {
        public static final RowMapper<VDSGroupHostsAndVMs> instance = new VDSGroupHostsAndVMsRowMapper();

        @Override
        public VDSGroupHostsAndVMs mapRow(ResultSet rs, int rowNum) throws SQLException {
            VDSGroupHostsAndVMs entity = new VDSGroupHostsAndVMs();
            entity.setHosts(rs.getInt("hosts"));
            entity.setVms(rs.getInt("vms"));
            entity.setVdsGroupId(getGuid(rs, "vds_group_id"));
            return entity;
        }

    }
    private final static class VdsGroupRowMapper implements RowMapper<VDSGroup> {
        public static final RowMapper<VDSGroup> instance = new VdsGroupRowMapper();
        @Override
        public VDSGroup mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            VDSGroup entity = new VDSGroup();
            entity.setDescription(rs.getString("description"));
            entity.setName(rs.getString("name"));
            entity.setId(getGuidDefaultEmpty(rs, "vds_group_id"));
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
            entity.setTunnelMigration(rs.getBoolean("tunnel_migration"));
            entity.getRequiredRngSources().addAll(VmRngDevice.csvToSourcesSet(rs.getString("required_rng_sources")));
            entity.setEmulatedMachine(rs.getString("emulated_machine"));
            entity.setDetectEmulatedMachine(rs.getBoolean("detect_emulated_machine"));
            entity.setTrustedService(rs.getBoolean("trusted_service"));
            entity.setHaReservation(rs.getBoolean("ha_reservation"));
            entity.setOptionalReasonRequired(rs.getBoolean("optional_reason"));
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
    public List<VDSGroup> getClustersByClusterPolicyId(Guid clusterPolicyId) {
        return getCallsHandler().executeReadList("GetVdsGroupsByClusterPolicyId",
                VdsGroupRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_policy_id", clusterPolicyId));
    }

    protected List<VDSGroup> getHostsAndVmsForClusters(List<VDSGroup> vdsGroups) throws Exception {
        Map<Guid, VDSGroup> groupsById = new HashMap<>();
        for (VDSGroup vdsGroup : vdsGroups) {
            groupsById.put(vdsGroup.getId(), vdsGroup);
        }
        Connection c = jdbcTemplate.getDataSource().getConnection();
        Array groups = c.createArrayOf("uuid", groupsById.keySet().toArray());
        List<VDSGroupHostsAndVMs> dataList = getCallsHandler().executeReadList("GetHostsAndVmsForClusters",
                VDSGroupHostsAndVMsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_ids", groups));

        c.close();
        for (VDSGroupHostsAndVMs groupDetail : dataList) {
            groupsById.get(groupDetail.getVdsGroupId()).setGroupHostsAndVms(groupDetail);
        }
        //The VDS groups have been updated, but we want to keep the order, so return the original list which is
        //in the right order.
        return vdsGroups;

    }

    @Override
    public List<VDSGroup> getClustersByServiceAndCompatibilityVersion(boolean glusterService, boolean virtService, String compatibilityVersion) {
        return getCallsHandler().executeReadList("GetVdsGroupsByServiceAndCompatibilityVersion",
                VdsGroupRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("gluster_service", glusterService)
                        .addValue("virt_service", virtService)
                        .addValue("compatibility_version", compatibilityVersion));
    }
}
