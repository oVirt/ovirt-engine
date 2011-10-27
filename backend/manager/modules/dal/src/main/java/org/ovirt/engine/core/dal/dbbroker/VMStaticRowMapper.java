package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.HypervisorType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OperationMode;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * JDBC row mapper for VM static
 *
 *
 */
public class VMStaticRowMapper implements ParameterizedRowMapper<VmStatic> {

    @Override
    public VmStatic mapRow(ResultSet rs, int rowNum) throws SQLException {
        VmStatic entity = new VmStatic();
        entity.setdescription(rs.getString("description"));
        entity.setmem_size_mb(rs.getInt("mem_size_mb"));
        entity.setos(VmOsType.forValue(rs.getInt("os")));
        entity.setvds_group_id(Guid.createGuidFromString(rs.getString("vds_group_id")));
        entity.setId(Guid.createGuidFromString(rs.getString("vm_guid")));
        entity.setvm_name(rs.getString("vm_name"));
        entity.setvmt_guid(Guid.createGuidFromString(rs.getString("vmt_guid")));
        entity.setdomain(rs.getString("domain"));
        entity.setcreation_date(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
        entity.setnum_of_monitors(rs.getInt("num_of_monitors"));
        entity.setis_initialized(rs.getBoolean("is_initialized"));
        entity.setis_auto_suspend(rs.getBoolean("is_auto_suspend"));
        entity.setnum_of_sockets(rs.getInt("num_of_sockets"));
        entity.setcpu_per_socket(rs.getInt("cpu_per_socket"));
        entity.setusb_policy(UsbPolicy.forValue(rs.getInt("usb_policy")));
        entity.settime_zone(rs.getString("time_zone"));
        entity.setauto_startup(rs.getBoolean("auto_startup"));
        entity.setis_stateless(rs.getBoolean("is_stateless"));
        entity.setdedicated_vm_for_vds(NGuid.createGuidFromString(rs.getString("dedicated_vm_for_vds")));
        entity.setfail_back(rs.getBoolean("fail_back"));
        entity.setvm_type(VmType.forValue(rs.getInt("vm_type")));
        entity.sethypervisor_type(HypervisorType.forValue(rs.getInt("hypervisor_type")));
        entity.setoperation_mode(OperationMode.forValue(rs.getInt("operation_mode")));
        entity.setnice_level(rs.getInt("nice_level"));
        entity.setdefault_boot_sequence(BootSequence.forValue(rs.getInt("default_boot_sequence")));
        entity.setdefault_display_type(DisplayType.forValue(rs.getInt("default_display_type")));
        entity.setpriority(rs.getInt("priority"));
        entity.setiso_path(rs.getString("iso_path"));
        entity.setorigin(OriginType.forValue(rs.getInt("origin")));
        entity.setinitrd_url(rs.getString("initrd_url"));
        entity.setkernel_url(rs.getString("kernel_url"));
        entity.setkernel_params(rs.getString("kernel_params"));
        entity.setMigrationSupport(MigrationSupport.forValue(rs.getInt("migration_support")));
        String predefinedProperties = rs.getString("predefined_properties");
        String userDefinedProperties = rs.getString("userdefined_properties");
        entity.setPredefinedProperties(predefinedProperties);
        entity.setUserDefinedProperties(userDefinedProperties);
        entity.setCustomProperties(VmPropertiesUtils.customProperties(predefinedProperties, userDefinedProperties));
        entity.setMinAllocatedMem(rs.getInt("min_allocated_mem"));

        return entity;
    }
}
