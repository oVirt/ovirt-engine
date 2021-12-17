package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public abstract class VmBaseDao<T extends VmBase> extends DefaultGenericDao<T, Guid> {

    public static final String SMALL_ICON_ID_COLUMN = "small_icon_id";
    public static final String LARGE_ICON_ID_COLUMN = "large_icon_id";

    public VmBaseDao(String entityStoredProcedureName) {
        super(entityStoredProcedureName);
    }

    protected MapSqlParameterSource createBaseParametersMapper(T entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("description", entity.getDescription())
                .addValue("free_text_comment", entity.getComment())
                .addValue("creation_date", entity.getCreationDate())
                .addValue("mem_size_mb", entity.getMemSizeMb())
                .addValue("max_memory_size_mb", entity.getMaxMemorySizeMb())
                .addValue("num_of_io_threads", entity.getNumOfIoThreads())
                .addValue("vnc_keyboard_layout", entity.getVncKeyboardLayout())
                .addValue("tunnel_migration", entity.getTunnelMigration())
                .addValue("cluster_id", entity.getClusterId())
                .addValue("num_of_sockets", entity.getNumOfSockets())
                .addValue("cpu_per_socket", entity.getCpuPerSocket())
                .addValue("threads_per_cpu", entity.getThreadsPerCpu())
                .addValue("os", entity.getOsId())
                .addValue("num_of_monitors", entity.getNumOfMonitors())
                .addValue("allow_console_reconnect", entity.isAllowConsoleReconnect())
                .addValue("vm_type", entity.getVmType())
                .addValue("priority", entity.getPriority())
                .addValue("auto_startup", entity.isAutoStartup())
                .addValue("is_stateless", entity.isStateless())
                .addValue("is_smartcard_enabled", entity.isSmartcardEnabled())
                .addValue("is_delete_protected", entity.isDeleteProtected())
                .addValue("sso_method", entity.getSsoMethod().toString())
                .addValue("iso_path", entity.getIsoPath())
                .addValue("usb_policy", entity.getUsbPolicy())
                .addValue("time_zone", entity.getTimeZone())
                .addValue("nice_level", entity.getNiceLevel())
                .addValue("cpu_shares", entity.getCpuShares())
                .addValue("default_boot_sequence", entity.getDefaultBootSequence())
                .addValue("default_display_type", entity.getDefaultDisplayType())
                .addValue("origin", entity.getOrigin())
                .addValue("initrd_url", entity.getInitrdUrl())
                .addValue("kernel_url", entity.getKernelUrl())
                .addValue("kernel_params", entity.getKernelParams())
                .addValue("quota_id", entity.getQuotaId())
                .addValue("migration_support", entity.getMigrationSupport().getValue())
                .addValue("dedicated_vm_for_vds", entity.getDedicatedVmForVdsList().isEmpty() ?
                        null : StringUtils.join(entity.getDedicatedVmForVdsList(), BaseDao.SEPARATOR))
                .addValue("min_allocated_mem", entity.getMinAllocatedMem())
                .addValue("is_run_and_pause", entity.isRunAndPause())
                .addValue("created_by_user_id", entity.getCreatedByUserId())
                .addValue("migration_downtime", entity.getMigrationDowntime())
                .addValue("serial_number_policy", entity.getSerialNumberPolicy() == null ? null : entity.getSerialNumberPolicy().getValue())
                .addValue("custom_serial_number", entity.getCustomSerialNumber())
                .addValue("is_boot_menu_enabled", entity.isBootMenuEnabled())
                .addValue("is_spice_file_transfer_enabled", entity.isSpiceFileTransferEnabled())
                .addValue("is_spice_copy_paste_enabled", entity.isSpiceCopyPasteEnabled())
                .addValue("cpu_profile_id", entity.getCpuProfileId())
                .addValue("is_auto_converge", entity.getAutoConverge())
                .addValue("is_migrate_compressed", entity.getMigrateCompressed())
                .addValue("is_migrate_encrypted", entity.getMigrateEncrypted())
                .addValue("predefined_properties", entity.getPredefinedProperties())
                .addValue("userdefined_properties", entity.getUserDefinedProperties())
                .addValue("custom_emulated_machine", entity.getCustomEmulatedMachine())
                .addValue("bios_type", entity.getBiosType())
                .addValue("custom_cpu_name", entity.getCustomCpuName())
                .addValue("host_cpu_flags", entity.isUseHostCpuFlags())
                .addValue(SMALL_ICON_ID_COLUMN, entity.getSmallIconId())
                .addValue(LARGE_ICON_ID_COLUMN, entity.getLargeIconId())
                .addValue("console_disconnect_action", entity.getConsoleDisconnectAction().toString())
                .addValue("console_disconnect_action_delay", entity.getConsoleDisconnectActionDelay())
                .addValue("resume_behavior", entity.getResumeBehavior() == null ? null : entity.getResumeBehavior().toString())
                .addValue("custom_compatibility_version", entity.getCustomCompatibilityVersion())
                .addValue("migration_policy_id", entity.getMigrationPolicyId())
                .addValue("lease_sd_id", entity.getLeaseStorageDomainId())
                .addValue("multi_queues_enabled", entity.isMultiQueuesEnabled())
                .addValue("use_tsc_frequency", entity.getUseTscFrequency())
                .addValue("cpu_pinning", StringUtils.isEmpty(entity.getCpuPinning()) ? null : entity.getCpuPinning())
                .addValue("virtio_scsi_multi_queues", entity.getVirtioScsiMultiQueues())
                .addValue("balloon_enabled", entity.isBalloonEnabled())
                .addValue("cpu_pinning_policy", entity.getCpuPinningPolicy());
    }

    /**
     * The common basic rowmapper for properties in VmBase.
     *  @param <T> a subclass of VmBase.
     */
    protected abstract static class AbstractVmRowMapper<T extends VmBase> implements RowMapper<T> {

        protected final void map(final ResultSet rs, final T entity) throws SQLException {
            entity.setMemSizeMb(rs.getInt("mem_size_mb"));
            entity.setMaxMemorySizeMb(rs.getInt("max_memory_size_mb"));
            entity.setNumOfIoThreads(rs.getInt("num_of_io_threads"));
            entity.setOsId(rs.getInt("os"));
            entity.setNumOfMonitors(rs.getInt("num_of_monitors"));
            entity.setAllowConsoleReconnect(rs.getBoolean("allow_console_reconnect"));
            entity.setDefaultDisplayType(DisplayType.forValue(rs.getInt("default_display_type")));
            entity.setDescription(rs.getString("description"));
            entity.setComment(rs.getString("free_text_comment"));
            entity.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
            entity.setNumOfSockets(rs.getInt("num_of_sockets"));
            entity.setCpuPerSocket(rs.getInt("cpu_per_socket"));
            entity.setThreadsPerCpu(rs.getInt("threads_per_cpu"));
            entity.setTimeZone(rs.getString("time_zone"));
            entity.setVmType(VmType.forValue(rs.getInt("vm_type")));
            entity.setUsbPolicy(UsbPolicy.forValue(rs.getInt("usb_policy")));
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
            entity.setDedicatedVmForVdsList(Guid.createGuidListFromString(rs.getString("dedicated_vm_for_vds")));
            entity.setMinAllocatedMem(rs.getInt("min_allocated_mem"));
            entity.setQuotaId(getGuid(rs, "quota_id"));
            entity.setCpuProfileId(getGuid(rs, "cpu_profile_id"));
            entity.setAutoConverge((Boolean) rs.getObject("is_auto_converge"));
            entity.setMigrateCompressed((Boolean) rs.getObject("is_migrate_compressed"));
            entity.setMigrateEncrypted((Boolean) rs.getObject("is_migrate_encrypted"));
            String predefinedProperties = rs.getString("predefined_properties");
            String userDefinedProperties = rs.getString("userdefined_properties");
            entity.setPredefinedProperties(predefinedProperties);
            entity.setUserDefinedProperties(userDefinedProperties);
            entity.setCustomProperties(VmPropertiesUtils.getInstance().customProperties(predefinedProperties,
                    userDefinedProperties));
            entity.setCustomEmulatedMachine(rs.getString("custom_emulated_machine"));
            entity.setBiosType(BiosType.forValue(rs.getInt("bios_type")));
            entity.setCustomCpuName(rs.getString("custom_cpu_name"));
            entity.setUseHostCpuFlags((Boolean) rs.getObject("host_cpu_flags"));
            entity.setSmallIconId(getGuid(rs, SMALL_ICON_ID_COLUMN));
            entity.setLargeIconId(getGuid(rs, LARGE_ICON_ID_COLUMN));
            entity.setConsoleDisconnectAction(ConsoleDisconnectAction.fromString(rs.getString("console_disconnect_action")));
            entity.setConsoleDisconnectActionDelay(rs.getInt("console_disconnect_action_delay"));
            String resumeBehavior = rs.getString("resume_behavior");
            entity.setResumeBehavior(resumeBehavior == null ? null : VmResumeBehavior.valueOf(resumeBehavior));
            entity.setCustomCompatibilityVersion(new VersionRowMapper("custom_compatibility_version").mapRow(rs, 0));
            entity.setLeaseStorageDomainId(getGuid(rs, "lease_sd_id"));
            entity.setMigrationPolicyId(getGuid(rs, "migration_policy_id"));
            entity.setMultiQueuesEnabled(rs.getBoolean("multi_queues_enabled"));
            entity.setUseTscFrequency(rs.getBoolean("use_tsc_frequency"));
            entity.setCpuPinning(rs.getString("cpu_pinning"));
            entity.setVirtioScsiMultiQueues(rs.getInt("virtio_scsi_multi_queues"));
            entity.setBalloonEnabled(rs.getBoolean("balloon_enabled"));
            entity.setCpuPinningPolicy(CpuPinningPolicy.forValue(rs.getInt("cpu_pinning_policy")));
        }
    }

}
