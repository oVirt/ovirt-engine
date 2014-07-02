package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.springframework.jdbc.core.RowMapper;

/**
 * The common basic rowmapper for properties in VmBase.
 *  @param <T> a subclass of VmBase.
 */
public abstract class AbstractVmRowMapper<T extends VmBase> implements RowMapper<T> {

    protected final void map(final ResultSet rs, final T entity) throws SQLException {
        entity.setMemSizeMb(rs.getInt("mem_size_mb"));
        entity.setOsId(rs.getInt("os"));
        entity.setNumOfMonitors(rs.getInt("num_of_monitors"));
        entity.setSingleQxlPci(rs.getBoolean("single_qxl_pci"));
        entity.setDefaultDisplayType(DisplayType.forValue(rs.getInt("default_display_type")));
        entity.setDescription(rs.getString("description"));
        entity.setComment(rs.getString("free_text_comment"));
        entity.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
        entity.setNumOfSockets(rs.getInt("num_of_sockets"));
        entity.setCpuPerSocket(rs.getInt("cpu_per_socket"));
        entity.setTimeZone(rs.getString("time_zone"));
        entity.setVmType(VmType.forValue(rs.getInt("vm_type")));
        entity.setUsbPolicy(UsbPolicy.forValue(rs.getInt("usb_policy")));
        entity.setFailBack(rs.getBoolean("fail_back"));
        entity.setDefaultBootSequence(BootSequence.forValue(rs.getInt("default_boot_sequence")));
        entity.setNiceLevel(rs.getInt("nice_level"));
        entity.setCpuShares(rs.getInt("cpu_shares"));
        entity.setPriority(rs.getInt("priority"));
        entity.setAutoStartup(rs.getBoolean("auto_startup"));
        entity.setStateless(rs.getBoolean("is_stateless"));
        entity.setDbGeneration(rs.getLong("db_generation"));
        entity.setIsoPath(rs.getString("iso_path"));
        entity.setOrigin(OriginType.forValue(rs.getInt("origin")));
        entity.setKernelUrl(rs.getString("kernel_url"));
        entity.setKernelParams(rs.getString("kernel_params"));
        entity.setInitrdUrl(rs.getString("initrd_url"));
        entity.setSmartcardEnabled(rs.getBoolean("is_smartcard_enabled"));
        entity.setDeleteProtected(rs.getBoolean("is_delete_protected"));
        entity.setSsoMethod(SsoMethod.fromString(rs.getString("sso_method")));
        entity.setTunnelMigration((Boolean) rs.getObject("tunnel_migration"));
        entity.setVncKeyboardLayout(rs.getString("vnc_keyboard_layout"));
        entity.setRunAndPause(rs.getBoolean("is_run_and_pause"));
        entity.setCreatedByUserId(Guid.createGuidFromString(rs.getString("created_by_user_id")));
        entity.setMigrationDowntime((Integer) rs.getObject("migration_downtime"));
        entity.setSerialNumberPolicy(SerialNumberPolicy.forValue((Integer) rs.getObject("serial_number_policy")));
        entity.setCustomSerialNumber(rs.getString("custom_serial_number"));
        entity.setBootMenuEnabled(rs.getBoolean("is_boot_menu_enabled"));
        entity.setSpiceFileTransferEnabled(rs.getBoolean("is_spice_file_transfer_enabled"));
        entity.setSpiceCopyPasteEnabled(rs.getBoolean("is_spice_copy_paste_enabled"));
        entity.setMigrationSupport(MigrationSupport.forValue(rs.getInt("migration_support")));
        entity.setDedicatedVmForVds(BaseDAODbFacade.getGuid(rs, "dedicated_vm_for_vds"));
        entity.setMinAllocatedMem(rs.getInt("min_allocated_mem"));
        entity.setQuotaId(BaseDAODbFacade.getGuid(rs, "quota_id"));
    }

}
