package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.HypervisorType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@code VdsDAO} that uses previously written code from
 * {@code DbFacade}.
 *
 *
 */
public class VdsDAODbFacadeImpl extends BaseDAODbFacade implements VdsDAO {

    @Override
    public VDS get(NGuid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
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
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled(rs.getBoolean("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetVdsByVdsId", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllWithName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_name", name);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
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
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled(rs.getBoolean("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsByName", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllForHostname(String hostname) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("host_name", hostname);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
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
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled(rs.getBoolean("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsByHostName", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllWithIpAddress(String address) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ip", address);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
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
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled(rs.getBoolean("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsByIp", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllWithUniqueId(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_unique_id", id);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
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
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled(rs.getBoolean("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsByUniqueID", mapper, parameterSource);
    }

    @Override
    public List<VDS> getAllOfTypes(VDSType[] types) {
        List<VDS> list = new ArrayList<VDS>();
        for (VDSType type : types) {
            list.addAll(getAllOfType(type));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllOfType(VDSType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_type", type);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvds_group_id(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setvds_group_name(rs.getString("vds_group_name"));
                entity.setvds_group_description(rs
                        .getString("vds_group_description"));
                entity.setvds_name(rs.getString("vds_name"));
                entity.setManagmentIp(rs.getString("ip"));
                entity.setUniqueId(rs.getString("vds_unique_id"));
                entity.setserver_SSL_enabled((Boolean) rs
                        .getObject("server_SSL_enabled"));
                entity.sethost_name(rs.getString("host_name"));
                entity.setport(rs.getInt("port"));
                entity.setstatus(VDSStatus.forValue(rs.getInt("status")));
                entity.setcpu_cores((Integer) rs.getObject("cpu_cores"));
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled((Boolean) rs.getObject("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsByType", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllForVdsGroupWithoutMigrating(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
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
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled(rs.getBoolean("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsWithoutMigratingVmsByVdsGroupId", mapper,
                parameterSource);
    }

    @Override
    public List<VDS> getAllWithQuery(String query) {
        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvds_group_id(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setvds_group_name(rs.getString("vds_group_name"));
                entity.setvds_group_description(rs
                        .getString("vds_group_description"));
                entity.setvds_name(rs.getString("vds_name"));
                entity.setManagmentIp(rs.getString("ip"));
                entity.setUniqueId(rs.getString("vds_unique_id"));
                entity.setserver_SSL_enabled((Boolean) rs
                        .getObject("server_SSL_enabled"));
                entity.sethost_name(rs.getString("host_name"));
                entity.setport(rs.getInt("port"));
                entity.setstatus(VDSStatus.forValue(rs.getInt("status")));
                entity.setcpu_cores((Integer) rs.getObject("cpu_cores"));
                entity.setcpu_model(rs.getString("cpu_model"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled((Boolean) rs.getObject("pm_enabled"));
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
                return entity;
            }
        };

        return new SimpleJdbcTemplate(jdbcTemplate).query(query, mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
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
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled(rs.getBoolean("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromVds", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllForVdsGroup(Guid vdsGroupID) {

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", vdsGroupID);

        ParameterizedRowMapper<VDS> mapper = new ParameterizedRowMapper<VDS>() {
            @Override
            public VDS mapRow(ResultSet rs, int rowNum) throws SQLException {
                VDS entity = new VDS();
                entity.setvds_id(Guid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvds_group_id(Guid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setvds_group_name(rs.getString("vds_group_name"));
                entity.setvds_group_description(rs
                        .getString("vds_group_description"));
                entity.setvds_name(rs.getString("vds_name"));
                entity.setManagmentIp(rs.getString("ip"));
                entity.setUniqueId(rs.getString("vds_unique_id"));
                entity.setserver_SSL_enabled((Boolean) rs
                        .getObject("server_SSL_enabled"));
                entity.sethost_name(rs.getString("host_name"));
                entity.setport(rs.getInt("port"));
                entity.setstatus(VDSStatus.forValue(rs.getInt("status")));
                entity.setcpu_cores((Integer) rs.getObject("cpu_cores"));
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setcpu_idle(rs.getDouble("cpu_idle"));
                entity.setcpu_load(rs.getDouble("cpu_load"));
                entity.setcpu_sys(rs.getDouble("cpu_sys"));
                entity.setcpu_user(rs.getDouble("cpu_user"));
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
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setmem_available(rs.getLong("mem_available"));
                entity.setmem_shared(rs.getLong("mem_shared"));
                entity.setvds_type(VDSType.forValue(rs.getInt("vds_type")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.sethigh_utilization(rs.getInt("high_utilization"));
                entity.setlow_utilization(rs.getInt("low_utilization"));
                entity.setcpu_over_commit_duration_minutes(rs
                        .getInt("cpu_over_commit_duration_minutes"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs
                        .getInt("selection_algorithm")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
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
                entity.setpm_password(VdsStaticDAODbFacadeImpl.decryptPassword(rs.getString("pm_password")));
                entity.setpm_port((Integer) rs.getObject("pm_port"));
                entity.setpm_options(rs.getString("pm_options"));
                entity.setpm_enabled((Boolean) rs.getObject("pm_enabled"));
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetVdsByVdsGroupId", mapper, parameterSource);
    }


}
