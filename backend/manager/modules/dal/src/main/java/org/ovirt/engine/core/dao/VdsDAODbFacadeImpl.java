package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@code VdsDAO} that uses previously written code from
 * {@code DbFacade}.
 */
public class VdsDAODbFacadeImpl extends BaseDAODbFacade implements VdsDAO {

    @Override
    public VDS get(NGuid id) {
        return get(id, null, false);
    }

    @Override
    public VDS get(NGuid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVdsByVdsId",
                VdsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VDS> getAllWithName(String name) {
        return getCallsHandler().executeReadList("GetVdsByName",
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
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, VdsRowMapper.instance);
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
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vdsGroupId)
                        .addValue("status", status.getValue()));
    }

    @Override
    public List<VDS> getAllForStoragePoolAndStatus(Guid storagePool, VDSStatus status) {
        return getCallsHandler().executeReadList("getVdsByStoragePoolIdWithStatus",
                new VdsRowMapper(),
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

    static final class VdsRowMapper implements ParameterizedRowMapper<VDS> {
        // single instance
        public final static VdsRowMapper instance = new VdsRowMapper();

        @Override
        public VDS mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final VDS entity = new VDS();
            entity.setId(Guid.createGuidFromString(rs
                    .getString("vds_id")));
            entity.setvds_group_id(Guid.createGuidFromString(rs
                    .getString("vds_group_id")));
            entity.setvds_group_name(rs.getString("vds_group_name"));
            entity.setvds_group_description(rs
                    .getString("vds_group_description"));
            entity.setvds_name(rs.getString("vds_name"));
            entity.setManagmentIp(rs.getString("ip"));
            entity.setUniqueId(rs.getString("vds_unique_id"));
            entity.setserver_SSL_enabled(rs
                    .getBoolean("server_SSL_enabled"));
            entity.sethost_name(rs.getString("host_name"));
            entity.setport(rs.getInt("port"));
            entity.setstatus(VDSStatus.forValue(rs.getInt("status")));
            entity.setcpu_cores((Integer) rs.getObject("cpu_cores"));
            entity.setCpuThreads((Integer) rs.getObject("cpu_threads"));
            entity.setcpu_model(rs.getString("cpu_model"));
            entity.setcpu_user(rs.getDouble("cpu_user"));
            entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
            entity.setif_total_speed(rs.getString("if_total_speed"));
            entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
            entity.setphysical_mem_mb((Integer) rs
                    .getObject("physical_mem_mb"));
            entity.setcpu_idle(rs.getDouble("cpu_idle"));
            entity.setcpu_load(rs.getDouble("cpu_load"));
            entity.setcpu_sys(rs.getDouble("cpu_sys"));
            entity.setmem_commited((Integer) rs.getObject("mem_commited"));
            entity.setvm_active((Integer) rs.getObject("vm_active"));
            entity.setvm_count((Integer) rs.getObject("vm_count"));
            entity.setvms_cores_count((Integer) rs
                    .getObject("vms_cores_count"));
            entity.setvm_migrating((Integer) rs.getObject("vm_migrating"));
            entity.setusage_cpu_percent((Integer) rs
                    .getObject("usage_cpu_percent"));
            entity.setusage_mem_percent((Integer) rs
                    .getObject("usage_mem_percent"));
            entity.setusage_network_percent((Integer) rs
                    .getObject("usage_network_percent"));
            entity.setreserved_mem((Integer) rs.getObject("reserved_mem"));
            entity.setguest_overhead((Integer) rs
                    .getObject("guest_overhead"));
            entity.setVersion(new RpmVersion(rs.getString("rpm_version")));
            entity.setsoftware_version(rs.getString("software_version"));
            entity.setversion_name(rs.getString("version_name"));
            entity.setprevious_status(VDSStatus.forValue(rs
                    .getInt("previous_status")));
            entity.setmem_available(rs.getLong("mem_available"));
            entity.setmem_shared(rs.getLong("mem_shared"));
            entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
            entity.setcpu_flags(rs.getString("cpu_flags"));
            entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
            entity.setStoragePoolId(Guid.createGuidFromString(rs
                    .getString("storage_pool_id")));
            entity.setstorage_pool_name(rs.getString("storage_pool_name"));
            entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                    .getInt("selection_algorithm")));
            entity.setpending_vcpus_count((Integer) rs
                    .getObject("pending_vcpus_count"));
            entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                    .getTimestamp("cpu_over_commit_time_stamp")));
            entity.sethigh_utilization(rs.getInt("high_utilization"));
            entity.setlow_utilization(rs.getInt("low_utilization"));
            entity.setcpu_over_commit_duration_minutes(rs
                    .getInt("cpu_over_commit_duration_minutes"));

            entity.setpending_vmem_size(rs.getInt("pending_vmem_size"));
            entity.setvds_strength(rs.getInt("vds_strength"));
            entity.setmax_vds_memory_over_commit(rs
                    .getInt("max_vds_memory_over_commit"));
            entity.setcpu_sockets((Integer) rs.getObject("cpu_sockets"));
            entity.setvds_spm_id((Integer) rs.getObject("vds_spm_id"));
            entity.setnet_config_dirty((Boolean) rs
                    .getObject("net_config_dirty"));
            entity.setpm_type(rs.getString("pm_type"));
            entity.setpm_user(rs.getString("pm_user"));
            entity.setpm_password(DbFacadeUtils.decryptPassword(rs.getString("pm_password")));
            entity.setpm_port((Integer) rs.getObject("pm_port"));
            entity.setpm_options(rs.getString("pm_options"));
            entity.setpm_enabled(rs.getBoolean("pm_enabled"));
            entity.setPmProxyPreferences(rs.getString("pm_proxy_preferences"));
            entity.setPmSecondaryIp((rs.getString("pm_secondary_ip")));
            entity.setPmSecondaryType(rs.getString("pm_secondary_type"));
            entity.setPmSecondaryUser(rs.getString("pm_secondary_user"));
            entity.setPmSecondaryPassword(DbFacadeUtils.decryptPassword(rs.getString("pm_secondary_password")));
            entity.setPmSecondaryPort((Integer) rs.getObject("pm_secondary_port"));
            entity.setPmSecondaryOptions(rs.getString("pm_secondary_options"));
            entity.setPmSecondaryConcurrent(rs.getBoolean("pm_secondary_concurrent"));
            entity.setspm_status(VdsSpmStatus.forValue(rs
                    .getInt("spm_status")));
            entity.setswap_free(rs.getLong("swap_free"));
            entity.setswap_total(rs.getLong("swap_total"));
            entity.setksm_cpu_percent((Integer) rs
                    .getObject("ksm_cpu_percent"));
            entity.setksm_pages(rs.getLong("ksm_pages"));
            entity.setksm_state((Boolean) rs.getObject("ksm_state"));
            entity.setsupported_cluster_levels(rs
                    .getString("supported_cluster_levels"));
            entity.setsupported_engines(rs.getString("supported_engines"));
            entity.setvds_group_compatibility_version(new Version(rs
                    .getString("vds_group_compatibility_version")));
            entity.sethost_os(rs.getString("host_os"));
            entity.setkvm_version(rs.getString("kvm_version"));
            entity.setlibvirt_version(new RpmVersion(rs.getString("libvirt_version")));
            entity.setspice_version(rs.getString("spice_version"));
            entity.setkernel_version(rs.getString("kernel_version"));
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
            entity.setSSHKeyFingerprint(rs.getString("sshKeyFingerprint"));
            entity.setHardwareManufacturer(rs.getString("hw_manufacturer"));
            entity.setHardwareProductName(rs.getString("hw_product_name"));
            entity.setHardwareVersion(rs.getString("hw_version"));
            entity.setHardwareSerialNumber(rs.getString("hw_serial_number"));
            entity.setHardwareUUID(rs.getString("hw_uuid"));
            entity.setHardwareFamily(rs.getString("hw_family"));
            entity.calculateFreeVirtualMemory();
            return entity;
        }
    }
}
