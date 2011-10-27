package org.ovirt.engine.core.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.VMRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VmDAODbFacadeImpl</code> provides a concrete implementation of {@link VmDAO}. The functionality is code
 * refactored out of {@link DbFacade}.
 *
 *
 */
public class VmDAODbFacadeImpl extends BaseDAODbFacade implements VmDAO {

    @Override
    public VM get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);
        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeRead("GetVmByVmGuid", mapper, parameterSource);
    }

    @Override
    public VM getById(Guid id) {
        VM vm = get(id);
        if (vm != null) {
            vm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getvm_guid()));
        }
        return vm;
    }

    @Override
    public VM getForHibernationImage(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_id", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeRead("GetVmByHibernationImageId", mapper, parameterSource);
    }

    @Override
    public VM getForImage(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeRead("GetVmByImageId", mapper, parameterSource);
    }

    @Override
    public VM getForImageGroup(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeRead("GetVmByImageGroupId", mapper, parameterSource);
    }

    @Override
    public List<VM> getAllForUser(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);

        return getCallsHandler().executeReadList("GetVmsByUserId", new VMRowMapper(), parameterSource);
    }

    @Override
    public List<VM> getAllForUserWithGroupsAndUserRoles(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);

        return getCallsHandler().executeReadList("GetVmsByUserIdWithGroupsAndUserRoles", new VMRowMapper(),
                parameterSource);
    }

    @Override
    public List<VM> getAllForAdGroupByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("ad_group_names", name);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeReadList("GetVmsByAdGroupNames", mapper, parameterSource);
    }

    @Override
    public List<VM> getAllWithTemplate(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vmt_guid", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeReadList("GetVmsByVmtGuid", mapper, parameterSource);
    }

    @Override
    public List<VM> getAllRunningForVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();

        return getCallsHandler().executeReadList("GetVmsRunningOnVds", mapper, parameterSource);
    }

    @Override
    public List<VM> getAllForDedicatedPowerClientByVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("dedicated_vm_for_vds", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeReadList("GetVmsDedicatedToPowerClientByVdsId", mapper, parameterSource);
    }

    @Override
    public Map<Guid, VM> getAllRunningByVds(Guid id) {
        HashMap<Guid, VM> map = new HashMap<Guid, VM>();

        List<VM> vms = getAllRunningForVds(id);
        for (VM vm : vms) {
            map.put(vm.getvm_guid(), vm);
        }

        return map;
    }

    @Override
    public List<VM> getAllUsingQuery(String query) {
        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, mapper);
    }

    @Override
    public List<VM> getAllForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeReadList("GetVmsByStorageDomainId", mapper, parameterSource);
    }

    @Override
    public List<VM> getAllRunningForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeReadList("GetRunningVmsByStorageDomainId", mapper, parameterSource);
    }

    @Override
    public List<VM> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<VM> mapper = new VMRowMapper();
        return getCallsHandler().executeReadList("GetAllFromVms", mapper, parameterSource);
    }

    @Override
    public void save(VM vm) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", vm.getdescription())
                .addValue("mem_size_mb", vm.getmem_size_mb())
                .addValue("os", vm.getos())
                .addValue("vds_group_id", vm.getvds_group_id())
                .addValue("vm_guid", vm.getvm_guid())
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
                .addValue("min_allocated_mem", vm.getMinAllocatedMem());

        getCallsHandler().executeModification("InsertVm", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);
        getCallsHandler().executeModification("DeleteVm", parameterSource);
    }

}
