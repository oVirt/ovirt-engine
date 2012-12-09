package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * The common basic rowmapper for properties in VmBase.
 *  @param <T> a subclass of VmBase.
 */
public abstract class AbstractVmRowMapper<T extends VmBase> implements ParameterizedRowMapper<T> {

    protected final void map(final ResultSet rs, final T entity) throws SQLException {
        entity.setos(VmOsType.forValue(rs.getInt("os")));
        entity.setdescription(rs.getString("description"));
        entity.setcreation_date(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
        entity.setnum_of_sockets(rs.getInt("num_of_sockets"));
        entity.setcpu_per_socket(rs.getInt("cpu_per_socket"));
        entity.settime_zone(rs.getString("time_zone"));
        entity.setvm_type(VmType.forValue(rs.getInt("vm_type")));
        entity.setusb_policy(UsbPolicy.forValue(rs.getInt("usb_policy")));
        entity.setfail_back(rs.getBoolean("fail_back"));
        entity.setdefault_boot_sequence(BootSequence.forValue(rs.getInt("default_boot_sequence")));
        entity.setnice_level(rs.getInt("nice_level"));
        entity.setis_auto_suspend(rs.getBoolean("is_auto_suspend"));
        entity.setpriority(rs.getInt("priority"));
        entity.setauto_startup(rs.getBoolean("auto_startup"));
        entity.setis_stateless(rs.getBoolean("is_stateless"));
        entity.setDbGeneration(rs.getLong("db_generation"));
        entity.setiso_path(rs.getString("iso_path"));
        entity.setorigin(OriginType.forValue(rs.getInt("origin")));
        entity.setkernel_url(rs.getString("kernel_url"));
        entity.setkernel_params(rs.getString("kernel_params"));
        entity.setinitrd_url(rs.getString("initrd_url"));
        entity.setSmartcardEnabled(rs.getBoolean("is_smartcard_enabled"));
        entity.setDeleteProtected(rs.getBoolean("is_delete_protected"));
    }

}
