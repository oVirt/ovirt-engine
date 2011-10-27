package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.HypervisorType;
import org.ovirt.engine.core.common.businessentities.OperationMode;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VMTemplateRowMapper implements ParameterizedRowMapper<VmTemplate> {

    @Override
    public VmTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
        VmTemplate entity = new VmTemplate();
        entity.setchild_count(rs.getInt("child_count"));
        entity.setcreation_date(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
        entity.setdescription(rs.getString("description"));
        entity.setmem_size_mb(rs.getInt("mem_size_mb"));
        entity.setname(rs.getString("name"));
        entity.setnum_of_sockets(rs.getInt("num_of_sockets"));
        entity.setcpu_per_socket(rs.getInt("cpu_per_socket"));
        // entity.setnum_of_cpus(rs.getInt("num_of_cpus"));
        entity.setos(VmOsType.forValue(rs.getInt("os")));
        entity.setId(Guid.createGuidFromString(rs.getString("vmt_guid")));
        entity.setvds_group_id(Guid.createGuidFromString(rs.getString("vds_group_id")));
        entity.setdomain(rs.getString("domain"));
        entity.setnum_of_monitors(rs.getInt("num_of_monitors"));
        entity.setstatus(VmTemplateStatus.forValue(rs.getInt("status")));
        entity.setusb_policy(UsbPolicy.forValue(rs.getInt("usb_policy")));
        entity.settime_zone(rs.getString("time_zone"));
        entity.setfail_back(rs.getBoolean("fail_back"));
        entity.setis_auto_suspend(rs.getBoolean("is_auto_suspend"));
        entity.setvds_group_name(rs.getString("vds_group_name"));
        entity.setvm_type(VmType.forValue(rs.getInt("vm_type")));
        entity.sethypervisor_type(HypervisorType.forValue(rs.getInt("hypervisor_type")));
        entity.setoperation_mode(OperationMode.forValue(rs.getInt("operation_mode")));
        entity.setnice_level(rs.getInt("nice_level"));
        entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
        entity.setstorage_pool_name(rs.getString("storage_pool_name"));
        entity.setdefault_boot_sequence(BootSequence.forValue(rs.getInt("default_boot_sequence")));
        entity.setdefault_display_type(DisplayType.forValue(rs.getInt("default_display_type")));
        entity.setpriority(rs.getInt("priority"));
        entity.setauto_startup(rs.getBoolean("auto_startup"));
        entity.setis_stateless(rs.getBoolean("is_stateless"));
        entity.setiso_path(rs.getString("iso_path"));
        entity.setorigin(OriginType.forValue(rs.getInt("origin")));
        entity.setinitrd_url(rs.getString("initrd_url"));
        entity.setkernel_url(rs.getString("kernel_url"));
        entity.setkernel_params(rs.getString("kernel_params"));
        return entity;

    }

}
