package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.AbstractVmRowMapper;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class VmStaticDAODbFacadeImpl extends BaseDAODbFacade implements VmStaticDAO {

    @Override
    public VmStatic get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        ParameterizedRowMapper<VmStatic> mapper = new VMStaticRowMapper();
        return getCallsHandler().executeRead("GetVmStaticByVmGuid", mapper, parameterSource);
    }

    @Override
    public List<VmStatic> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public void save(VmStatic vm) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", vm.getdescription())
                .addValue("mem_size_mb", vm.getmem_size_mb())
                .addValue("os", vm.getos())
                .addValue("vds_group_id", vm.getvds_group_id())
                .addValue("vm_guid", vm.getId())
                .addValue("vm_name", vm.getvm_name())
                .addValue("vmt_guid", vm.getvmt_guid())
                .addValue("domain", vm.getdomain())
                .addValue("creation_date", vm.getcreation_date())
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
                .addValue("min_allocated_mem", vm.getMinAllocatedMem())
                .addValue("quota_id", vm.getQuotaId());

        getCallsHandler().executeModification("InsertVmStatic", parameterSource);

    }

    @Override
    public void update(VmStatic vm) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", vm.getdescription())
                .addValue("mem_size_mb", vm.getmem_size_mb())
                .addValue("os", vm.getos())
                .addValue("vds_group_id", vm.getvds_group_id())
                .addValue("vm_guid", vm.getId())
                .addValue("vm_name", vm.getvm_name())
                .addValue("vmt_guid", vm.getvmt_guid())
                .addValue("domain", vm.getdomain())
                .addValue("creation_date", vm.getcreation_date())
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
                .addValue("min_allocated_mem", vm.getMinAllocatedMem())
                .addValue("quota_id", vm.getQuotaId());

        getCallsHandler().executeModification("UpdateVmStatic", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        getCallsHandler().executeModification("DeleteVmStatic", parameterSource);
    }

    @Override
    public List<VmStatic> getAllByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_name", name);

        ParameterizedRowMapper<VmStatic> mapper = new VMStaticRowMapper();
        return getCallsHandler().executeReadList("GetVmStaticByName", mapper, parameterSource);

    }

    @Override
    public List<VmStatic> getAllByStoragePoolId(Guid spId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("sp_id", spId);

        ParameterizedRowMapper<VmStatic> mapper = new VMStaticRowMapper();

        return getCallsHandler().executeReadList("GetAllFromVmStaticByStoragePoolId", mapper, parameterSource);
    }

    @Override
    public List<VmStatic> getAllByVdsGroup(Guid vdsGroup) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", vdsGroup);

        ParameterizedRowMapper<VmStatic> mapper = new VMStaticRowMapper();
        return getCallsHandler().executeReadList("GetVmStaticByVdsGroup", mapper, parameterSource);
    }

    @Override
    public List<VmStatic> getAllWithFailbackByVds(Guid vds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", vds);

        ParameterizedRowMapper<VmStatic> mapper = new VMStaticRowMapper();
        return getCallsHandler().executeReadList("GetVmStaticWithFailbackByVdsId", mapper,
                parameterSource);
    }

    @Override
    public List<VmStatic> getAllByGroupAndNetworkName(Guid group, String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("groupId", group).addValue("networkName", name);

        ParameterizedRowMapper<VmStatic> mapper = new VMStaticRowMapper();

        return getCallsHandler().executeReadList("GetvmStaticByGroupIdAndNetwork", mapper,
                parameterSource);
    }

    @Override
    public List<String> getAllNamesPinnedToHost(Guid host) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", host);

        ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("vm_name");
            }
        };

        return getCallsHandler().executeReadList("GetNamesOfVmStaticDedicatedToVds", mapper,
                parameterSource);
    }

    /**
     * JDBC row mapper for VM static
     */
    static class VMStaticRowMapper extends AbstractVmRowMapper<VmStatic> {

        @Override
        public VmStatic mapRow(ResultSet rs, int rowNum) throws SQLException {
            final VmStatic entity = new VmStatic();
            map(rs, entity);

            entity.setId(Guid.createGuidFromString(rs.getString("vm_guid")));
            entity.setmem_size_mb(rs.getInt("mem_size_mb"));
            entity.setvds_group_id(Guid.createGuidFromString(rs.getString("vds_group_id")));

            entity.setvm_name(rs.getString("vm_name"));
            entity.setvmt_guid(Guid.createGuidFromString(rs.getString("vmt_guid")));
            entity.setdomain(rs.getString("domain"));
            entity.setnum_of_monitors(rs.getInt("num_of_monitors"));
            entity.setis_initialized(rs.getBoolean("is_initialized"));
            entity.setdedicated_vm_for_vds(NGuid.createGuidFromString(rs.getString("dedicated_vm_for_vds")));
            entity.setdefault_display_type(DisplayType.forValue(rs.getInt("default_display_type")));
            entity.setMigrationSupport(MigrationSupport.forValue(rs.getInt("migration_support")));
            String predefinedProperties = rs.getString("predefined_properties");
            String userDefinedProperties = rs.getString("userdefined_properties");
            entity.setPredefinedProperties(predefinedProperties);
            entity.setUserDefinedProperties(userDefinedProperties);
            entity.setCustomProperties(VmPropertiesUtils.customProperties(predefinedProperties, userDefinedProperties));
            entity.setMinAllocatedMem(rs.getInt("min_allocated_mem"));
            entity.setQuotaId(Guid.createGuidFromString(rs.getString("quota_id")));

            return entity;
        }
    }

}
