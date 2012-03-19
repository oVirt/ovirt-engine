package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.HypervisorType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OperationMode;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.MultiValueMapUtils;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VmDAODbFacadeImpl</code> provides a concrete implementation of {@link VmDAO}. The functionality is code
 * refactored out of {@link DbFacade}.
 */
public class VmDAODbFacadeImpl extends BaseDAODbFacade implements VmDAO {

    @Override
    public VM get(Guid id) {
        return getCallsHandler().executeRead("GetVmByVmGuid", VMRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("vm_guid", id));
    }

    @Override
    public VM getById(Guid id) {
        VM vm = get(id);
        if (vm != null) {
            vm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getId()));
        }
        return vm;
    }

    @Override
    public VM getForHibernationImage(Guid id) {
        return getCallsHandler().executeRead("GetVmByHibernationImageId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("image_id", id));
    }

    @Override
    public Map<Boolean, List<VM>> getForImage(Guid id) {
        Map<Boolean, List<VM>> result = new HashMap<Boolean, List<VM>>();
        List<VMWithPlugInfo> vms =
                getCallsHandler().executeReadList
                        ("GetVmsByImageId",
                                VMWithPlugInfoRowMapper.instance,
                                getCustomMapSqlParameterSource().addValue("image_guid", id));
        for (VMWithPlugInfo vm : vms) {
            MultiValueMapUtils.addToMap(vm.isPlugged(), vm.getVM(), result);
        }
        return result;
    }

    @Override
    public VM getForImageGroup(Guid id) {
        return getCallsHandler().executeRead("GetVmByImageGroupId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("image_group_id", id));
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
    public List<VM> getAllForDedicatedPowerClientByVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsDedicatedToPowerClientByVdsId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("dedicated_vm_for_vds", id));
    }

    @Override
    public Map<Guid, VM> getAllRunningByVds(Guid id) {
        HashMap<Guid, VM> map = new HashMap<Guid, VM>();

        for (VM vm : getAllRunningForVds(id)) {
            map.put(vm.getId(), vm);
        }

        return map;
    }

    @Override
    public List<VM> getAllUsingQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, VMRowMapper.instance);
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
    public List<VM> getAllRunningForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("GetRunningVmsByStorageDomainId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", id));
    }

    @Override
    public List<VM> getAll() {
        return getCallsHandler().executeReadList("GetAllFromVms",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource());
    }

    @Override
    public void save(VM vm) {
        getCallsHandler().executeModification("InsertVm", getCustomMapSqlParameterSource()
                .addValue("description", vm.getdescription())
                .addValue("mem_size_mb", vm.getmem_size_mb())
                .addValue("os", vm.getos())
                .addValue("vds_group_id", vm.getvds_group_id())
                .addValue("vm_guid", vm.getId())
                .addValue("vm_name", vm.getvm_name())
                .addValue("vmt_guid", vm.getvmt_guid())
                .addValue("num_of_monitors", vm.getnum_of_monitors())
                .addValue("is_initialized", vm.getis_initialized())
                .addValue("is_auto_suspend", vm.getis_auto_suspend())
                .addValue("num_of_sockets", vm.getnum_of_sockets())
                .addValue("cpu_per_socket", vm.getcpu_per_socket())
                .addValue("usb_policy", vm.getusb_policy())
                .addValue("time_zone", vm.gettime_zone())
                .addValue("auto_startup", vm.getauto_startup())
                .addValue("is_stateless", vm.getis_stateless())
                .addValue("dedicated_vm_for_vds", vm.getdedicated_vm_for_vds())
                .addValue("fail_back", vm.getfail_back())
                .addValue("vm_type", vm.getvm_type())
                .addValue("hypervisor_type", vm.gethypervisor_type())
                .addValue("operation_mode", vm.getoperation_mode())
                .addValue("nice_level", vm.getnice_level())
                .addValue("default_boot_sequence",
                        vm.getdefault_boot_sequence())
                .addValue("default_display_type", vm.getdefault_display_type())
                .addValue("priority", vm.getpriority())
                .addValue("iso_path", vm.getiso_path())
                .addValue("origin", vm.getorigin())
                .addValue("initrd_url", vm.getinitrd_url())
                .addValue("kernel_url", vm.getkernel_url())
                .addValue("kernel_params", vm.getkernel_params())
                .addValue("migration_support",
                        vm.getMigrationSupport().getValue())
                .addValue("predefined_properties", vm.getPredefinedProperties())
                .addValue("userdefined_properties",
                        vm.getUserDefinedProperties())
                .addValue("min_allocated_mem", vm.getMinAllocatedMem()));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteVm", getCustomMapSqlParameterSource()
                .addValue("vm_guid", id));
    }

    private static final class VMRowMapper implements ParameterizedRowMapper<VM> {
        public static final VMRowMapper instance = new VMRowMapper();

        @Override
        public VM mapRow(ResultSet rs, int rowNum) throws SQLException {

            VM entity = new VM();
            entity.setId(Guid.createGuidFromString(rs.getString("vm_guid")));
            entity.setvm_name(rs.getString("vm_name"));
            entity.setQuotaId(Guid.createGuidFromString(rs.getString("quota_id")));
            entity.setQuotaName(rs.getString("quota_name"));
            entity.setvm_mem_size_mb(rs.getInt("vm_mem_size_mb"));
            entity.setvmt_guid(Guid.createGuidFromString(rs.getString("vmt_guid")));
            entity.setvm_os(VmOsType.forValue(rs.getInt("vm_os")));
            entity.setvm_description(rs.getString("vm_description"));
            entity.setvds_group_id(Guid.createGuidFromString(rs.getString("vds_group_id")));
            entity.setvm_domain(rs.getString("vm_domain"));
            entity.setvm_creation_date(DbFacadeUtils.fromDate(rs.getTimestamp("vm_creation_date")));
            entity.setvds_group_name(rs.getString("vds_group_name"));
            entity.setvds_group_description(rs.getString("vds_group_description"));
            entity.setvmt_name(rs.getString("vmt_name"));
            entity.setvmt_mem_size_mb(rs.getInt("vmt_mem_size_mb"));
            entity.setvmt_os(VmOsType.forValue(rs.getInt("vmt_os")));
            entity.setvmt_creation_date(DbFacadeUtils.fromDate(rs.getTimestamp("vmt_creation_date")));
            entity.setvmt_child_count(rs.getInt("vmt_child_count"));
            entity.setvmt_num_of_cpus(rs.getInt("vmt_num_of_cpus"));
            entity.setvmt_num_of_sockets(rs.getInt("vmt_num_of_sockets"));
            entity.setvmt_cpu_per_socket(rs.getInt("vmt_cpu_per_socket"));
            entity.setvmt_description(rs.getString("vmt_description"));
            entity.setstatus(VMStatus.forValue(rs.getInt("status")));
            entity.setvm_ip(rs.getString("vm_ip"));
            entity.setvm_host(rs.getString("vm_host"));
            entity.setvm_pid((Integer) rs.getObject("vm_pid"));
            entity.setvm_last_up_time(DbFacadeUtils.fromDate(rs.getTimestamp("vm_last_up_time")));
            entity.setvm_last_boot_time(DbFacadeUtils.fromDate(rs.getTimestamp("vm_last_boot_time")));
            entity.setguest_cur_user_name(rs.getString("guest_cur_user_name"));
            entity.setguest_last_login_time(DbFacadeUtils.fromDate(rs.getTimestamp("guest_last_login_time")));
            entity.setguest_cur_user_id(NGuid.createGuidFromString(rs.getString("guest_cur_user_id")));
            entity.setguest_last_logout_time(DbFacadeUtils.fromDate(rs.getTimestamp("guest_last_logout_time")));
            entity.setguest_os(rs.getString("guest_os"));
            entity.setcpu_user(rs.getDouble("cpu_user"));
            entity.setcpu_sys(rs.getDouble("cpu_sys"));
            entity.setelapsed_time(rs.getDouble("elapsed_time"));
            entity.setusage_network_percent((Integer) rs.getObject("usage_network_percent"));
            entity.setusage_mem_percent((Integer) rs.getObject("usage_mem_percent"));
            entity.setusage_cpu_percent((Integer) rs.getObject("usage_cpu_percent"));
            entity.setrun_on_vds(NGuid.createGuidFromString(rs.getString("run_on_vds")));
            entity.setmigrating_to_vds(NGuid.createGuidFromString(rs.getString("migrating_to_vds")));
            entity.setapp_list(rs.getString("app_list"));
            entity.setdisplay((Integer) rs.getObject("display"));
            entity.setVmPoolName(rs.getString("vm_pool_name"));
            entity.setVmPoolId(NGuid.createGuidFromString(rs.getString("vm_pool_id")));
            entity.setnum_of_monitors(rs.getInt("num_of_monitors"));
            entity.setis_initialized(rs.getBoolean("is_initialized"));
            entity.setis_auto_suspend(rs.getBoolean("is_auto_suspend"));
            entity.setnum_of_sockets(rs.getInt("num_of_sockets"));
            entity.setcpu_per_socket(rs.getInt("cpu_per_socket"));
            entity.setusb_policy(UsbPolicy.forValue(rs.getInt("usb_policy")));
            entity.setacpi_enable((Boolean) rs.getObject("acpi_enable"));
            entity.setsession(SessionState.forValue(rs.getInt("session")));
            entity.setdisplay_ip(rs.getString("display_ip"));
            entity.setdisplay_type(DisplayType.forValue(rs.getInt("display_type")));
            entity.setkvm_enable((Boolean) rs.getObject("kvm_enable"));
            entity.setboot_sequence(BootSequence.forValue(rs.getInt("boot_sequence")));
            entity.setrun_on_vds_name(rs.getString("run_on_vds_name"));
            entity.settime_zone(rs.getString("time_zone"));
            entity.setdisplay_secure_port((Integer) rs.getObject("display_secure_port"));
            entity.setutc_diff((Integer) rs.getObject("utc_diff"));
            entity.setauto_startup(rs.getBoolean("auto_startup"));
            entity.setis_stateless(rs.getBoolean("is_stateless"));
            entity.setdedicated_vm_for_vds(NGuid.createGuidFromString(rs.getString("dedicated_vm_for_vds")));
            entity.setfail_back(rs.getBoolean("fail_back"));
            entity.setlast_vds_run_on(NGuid.createGuidFromString(rs.getString("last_vds_run_on")));
            entity.setclient_ip(rs.getString("client_ip"));
            entity.setguest_requested_memory((Integer) rs.getObject("guest_requested_memory"));
            entity.setvds_group_cpu_name(rs.getString("vds_group_cpu_name"));
            entity.setvm_type(VmType.forValue(rs.getInt("vm_type")));
            entity.setstorage_pool_id(Guid.createGuidFromString(rs.getString("storage_pool_id")));
            entity.setstorage_pool_name(rs.getString("storage_pool_name"));
            entity.sethypervisor_type(HypervisorType.forValue(rs.getInt("hypervisor_type")));
            entity.setoperation_mode(OperationMode.forValue(rs.getInt("operation_mode")));
            entity.setselection_algorithm(VdsSelectionAlgorithm.forValue(rs.getInt("selection_algorithm")));
            entity.setTransparentHugePages(rs.getBoolean("transparent_hugepages"));
            entity.setnice_level(rs.getInt("nice_level"));
            entity.sethibernation_vol_handle(rs.getString("hibernation_vol_handle"));
            entity.setdefault_boot_sequence(BootSequence.forValue(rs.getInt("default_boot_sequence")));
            entity.setdefault_display_type(DisplayType.forValue(rs.getInt("default_display_type")));
            entity.setpriority(rs.getInt("priority"));
            entity.setiso_path(rs.getString("iso_path"));
            entity.setorigin(OriginType.forValue(rs.getInt("origin")));
            entity.setinitrd_url(rs.getString("initrd_url"));
            entity.setkernel_url(rs.getString("kernel_url"));
            entity.setkernel_params(rs.getString("kernel_params"));
            entity.setvds_group_compatibility_version(new Version(rs.getString("vds_group_compatibility_version")));
            entity.setExitMessage(rs.getString("exit_message"));
            entity.setExitStatus(VmExitStatus.forValue(rs.getInt("exit_status")));
            entity.setVmPauseStatus(VmPauseStatus.forValue(rs.getInt("pause_status")));
            entity.setMigrationSupport(MigrationSupport.forValue(rs.getInt("migration_support")));
            String predefinedProperties = rs.getString("predefined_properties");
            String userDefinedProperties = rs.getString("userdefined_properties");
            entity.setPredefinedProperties(predefinedProperties);
            entity.setUserDefinedProperties(userDefinedProperties);
            entity.setCustomProperties(VmPropertiesUtils.customProperties(predefinedProperties, userDefinedProperties));
            entity.setMinAllocatedMem(rs.getInt("min_allocated_mem"));
            entity.setHash(rs.getString("hash"));
            return entity;
        }
    }

    private static class VMWithPlugInfo {

        public VM getVM() {
            return vm;
        }

        public void setVM(VM vm) {
            this.vm = vm;
        }

        public boolean isPlugged() {
            return isPlugged;
        }

        public void setPlugged(boolean isPlugged) {
            this.isPlugged = isPlugged;
        }

        private VM vm;
        private boolean isPlugged;
    }

    private static final class VMWithPlugInfoRowMapper implements ParameterizedRowMapper<VMWithPlugInfo> {
        public static final VMWithPlugInfoRowMapper instance = new VMWithPlugInfoRowMapper();

        @Override
        public VMWithPlugInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            @SuppressWarnings("synthetic-access")
            VMWithPlugInfo entity = new VMWithPlugInfo();

            entity.setPlugged(rs.getBoolean("is_plugged"));
            entity.setVM(VMRowMapper.instance.mapRow(rs, rowNum));
            return entity;
        }
    }

}
