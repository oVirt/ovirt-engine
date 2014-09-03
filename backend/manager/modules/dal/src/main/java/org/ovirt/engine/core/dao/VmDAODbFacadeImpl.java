package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * <code>VmDAODbFacadeImpl</code> provides a concrete implementation of {@link VmDAO}. The functionality is code
 * refactored out of {@link Dorg.ovirt.engine.core.dal.dbbroker.DbFacad}.
 */
public class VmDAODbFacadeImpl extends BaseDAODbFacade implements VmDAO {

    @Override
    public VM get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VM get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmByVmGuid", VMRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("vm_guid", id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public VM getByNameForDataCenter(Guid dataCenterId, String name, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmByVmNameForDataCenter", VMRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("data_center_id", dataCenterId).addValue("vm_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public VM getForHibernationImage(Guid id) {
        return getCallsHandler().executeRead("GetVmByHibernationImageId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("image_id", id));
    }

    @Override
    public Map<Boolean, List<VM>> getForDisk(Guid id, boolean includeVmsSnapshotAttachedTo) {
        Map<Boolean, List<VM>> result = new HashMap<Boolean, List<VM>>();
        List<Pair<VM, VmDevice>> vms = getVmsWithPlugInfo(id);
        for (Pair<VM, VmDevice> pair : vms) {
            VmDevice device = pair.getSecond();
            if (includeVmsSnapshotAttachedTo || device.getSnapshotId() == null) {
                MultiValueMapUtils.addToMap(device.getIsPlugged(), pair.getFirst(), result);
            }
        }
        return result;
    }

    @Override
    public List<VM> getAllVMsWithDisksOnOtherStorageDomain(Guid storageDomainGuid) {
        return getCallsHandler().executeReadList("GetAllVMsWithDisksOnOtherStorageDomain",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainGuid));
    }

    @Override
    public List<VM> getVmsListForDisk(Guid id, boolean includeVmsSnapshotAttachedTo) {
        List<VM> result = new ArrayList<>();
        List<Pair<VM, VmDevice>> vms = getVmsWithPlugInfo(id);
        for (Pair<VM, VmDevice> pair : vms) {
            if (includeVmsSnapshotAttachedTo || pair.getSecond().getSnapshotId() == null) {
                result.add(pair.getFirst());
            }
        }
        return result;
    }

    @Override
    public List<VM> getVmsListByInstanceType(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByInstanceTypeId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("instance_type_id", id));
    }

    public List<Pair<VM, VmDevice>> getVmsWithPlugInfo(Guid id) {
        return getCallsHandler().executeReadList
                ("GetVmsByDiskId",
                        VMWithPlugInfoRowMapper.instance,
                        getCustomMapSqlParameterSource().addValue("disk_guid", id));
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
    public List<VM> getAllRunningOnOrMigratingToVds(Guid id) {
        return getCallsHandler().executeReadList("GetVmsRunningOnOrMigratingToVds",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
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
        return jdbcTemplate.query(query, VMRowMapper.instance);
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
    public List<VM> getVmsByIds(List<Guid> vmsIds) {
        return getCallsHandler().executeReadList("GetVmsByIds",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vms_ids", StringUtils.join(vmsIds, ',')));
    }

    @Override
    public List<VM> getAllActiveForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("GetActiveVmsByStorageDomainId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", id));
    }

    @Override
    public List<VM> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VM> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromVms",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public void saveIsInitialized(Guid vmid, boolean isInitialized) {
        getCallsHandler().executeModification("UpdateIsInitialized",
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmid)
                        .addValue("is_initialized", isInitialized));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteVm", getCustomMapSqlParameterSource()
                .addValue("vm_guid", id));
    }

    @Override
    public List<VM> getAllForNetwork(Guid id) {
        return getCallsHandler().executeReadList("GetVmsByNetworkId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("network_id", id));
    }

    @Override
    public List<VM> getAllForVnicProfile(Guid vNicProfileId) {
        return getCallsHandler().executeReadList("GetVmsByVnicProfileId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vnic_profile_id", vNicProfileId));
    }

    @Override
    public List<VM> getAllForVdsGroup(Guid vds_group_id) {
        return getCallsHandler().executeReadList("GetVmsByVdsGroupId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vds_group_id));
    }

    @Override
    public List<VM> getAllForVmPool(Guid vmPoolId) {
        return getCallsHandler().executeReadList("GetVmsByVmPoolId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", vmPoolId));
    }

    @Override
    public List<VM> getAllFailedAutoStartVms() {
        return getCallsHandler().executeReadList("GetFailedAutoStartVms",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<VM> getAllMigratingToHost(Guid vdsId) {
        return getCallsHandler().executeReadList("GetVmsMigratingToVds",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vdsId));
    }

    @Override
    public void updateOriginalTemplateName(Guid originalTemplateId, String originalTemplateName) {
        getCallsHandler().executeModification("UpdateOriginalTemplateName",
                getCustomMapSqlParameterSource()
                        .addValue("original_template_id", originalTemplateId)
                        .addValue("original_template_name", originalTemplateName)
        );
    }

    @Override
    public List<VM> getAllRunningByCluster(Guid clusterId) {
        return getCallsHandler().executeReadList("GetRunningVmsByClusterId",
                VMRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public List<Guid> getVmIdsForVersionUpdate(Guid baseTemplateId) {
        return getCallsHandler().executeReadList("getVmIdsForVersionUpdate",
                createGuidMapper(), getCustomMapSqlParameterSource()
                    .addValue("base_template_id", baseTemplateId));
    }

    static final class VMRowMapper implements RowMapper<VM> {
        public static final VMRowMapper instance = new VMRowMapper();

        @Override
        public VM mapRow(ResultSet rs, int rowNum) throws SQLException {

            VM entity = new VM();
            entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
            entity.setName(rs.getString("vm_name"));
            entity.setQuotaId(getGuid(rs, "quota_id"));
            entity.setQuotaName(rs.getString("quota_name"));
            entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
            entity.setVmMemSizeMb(rs.getInt("vm_mem_size_mb"));
            entity.setVmtGuid(getGuidDefaultEmpty(rs, "vmt_guid"));
            entity.setVmOs(rs.getInt("vm_os"));
            entity.setVmDescription(rs.getString("vm_description"));
            entity.setVdsGroupId(getGuidDefaultEmpty(rs, "vds_group_id"));
            entity.setComment(rs.getString("vm_comment"));
            entity.setVmCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("vm_creation_date")));
            entity.setVdsGroupName(rs.getString("vds_group_name"));
            entity.setVdsGroupDescription(rs.getString("vds_group_description"));
            entity.setVmtName(rs.getString("vmt_name"));
            entity.setVmtMemSizeMb(rs.getInt("vmt_mem_size_mb"));
            entity.setVmtOsId(rs.getInt("vmt_os"));
            entity.setVmtCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("vmt_creation_date")));
            entity.setVmtChildCount(rs.getInt("vmt_child_count"));
            entity.setVmtNumOfCpus(rs.getInt("vmt_num_of_cpus"));
            entity.setVmtNumOfSockets(rs.getInt("vmt_num_of_sockets"));
            entity.setVmtCpuPerSocket(rs.getInt("vmt_cpu_per_socket"));
            entity.setVmtDescription(rs.getString("vmt_description"));
            entity.setStatus(VMStatus.forValue(rs.getInt("status")));
            entity.setVmIp(rs.getString("vm_ip"));
            entity.setVmFQDN(rs.getString("vm_fqdn"));
            entity.setVmHost(rs.getString("vm_host"));
            entity.setVmPid((Integer) rs.getObject("vm_pid"));
            entity.setDbGeneration(rs.getLong("db_generation"));
            entity.setLastStartTime(DbFacadeUtils.fromDate(rs.getTimestamp("last_start_time")));
            entity.setLastStopTime(DbFacadeUtils.fromDate(rs.getTimestamp("last_stop_time")));
            entity.setGuestCurrentUserName(rs.getString("guest_cur_user_name"));
            entity.setConsoleCurrentUserName(rs.getString("console_cur_user_name"));
            entity.setGuestLastLoginTime(DbFacadeUtils.fromDate(rs.getTimestamp("guest_last_login_time")));
            entity.setGuestLastLogoutTime(DbFacadeUtils.fromDate(rs.getTimestamp("guest_last_logout_time")));
            entity.setConsoleUserId(getGuid(rs, "console_user_id"));
            entity.setGuestOs(rs.getString("guest_os"));
            entity.setCpuUser(rs.getDouble("cpu_user"));
            entity.setCpuSys(rs.getDouble("cpu_sys"));
            entity.setElapsedTime(rs.getDouble("elapsed_time"));
            entity.setUsageNetworkPercent((Integer) rs.getObject("usage_network_percent"));
            entity.setUsageMemPercent((Integer) rs.getObject("usage_mem_percent"));
            entity.setUsageCpuPercent((Integer) rs.getObject("usage_cpu_percent"));
            entity.setMigrationProgressPercent((Integer) rs.getObject("migration_progress_percent"));
            entity.setRunOnVds(getGuid(rs, "run_on_vds"));
            entity.setMigratingToVds(getGuid(rs, "migrating_to_vds"));
            entity.setAppList(rs.getString("app_list"));
            entity.setDisplay((Integer) rs.getObject("display"));
            entity.setVmPoolName(rs.getString("vm_pool_name"));
            entity.setVmPoolId(getGuid(rs, "vm_pool_id"));
            entity.setNumOfMonitors(rs.getInt("num_of_monitors"));
            entity.setSingleQxlPci(rs.getBoolean("single_qxl_pci"));
            entity.setAllowConsoleReconnect(rs.getBoolean("allow_console_reconnect"));
            entity.setInitialized(rs.getBoolean("is_initialized"));
            entity.setNumOfSockets(rs.getInt("num_of_sockets"));
            entity.setCpuPerSocket(rs.getInt("cpu_per_socket"));
            entity.setUsbPolicy(UsbPolicy.forValue(rs.getInt("usb_policy")));
            entity.setAcpiEnable((Boolean) rs.getObject("acpi_enable"));
            entity.setSession(SessionState.forValue(rs.getInt("session")));
            entity.setDisplayIp(rs.getString("display_ip"));
            entity.setDisplayType(DisplayType.forValue(rs.getInt("display_type")));
            entity.setKvmEnable((Boolean) rs.getObject("kvm_enable"));
            entity.setBootSequence(BootSequence.forValue(rs.getInt("boot_sequence")));
            entity.setRunOnVdsName(rs.getString("run_on_vds_name"));
            entity.setTimeZone(rs.getString("time_zone"));
            entity.setDisplaySecurePort((Integer) rs.getObject("display_secure_port"));
            entity.setUtcDiff((Integer) rs.getObject("utc_diff"));
            entity.setAutoStartup(rs.getBoolean("auto_startup"));
            entity.setStateless(rs.getBoolean("is_stateless"));
            entity.setSmartcardEnabled(rs.getBoolean("is_smartcard_enabled"));
            entity.setDeleteProtected(rs.getBoolean("is_delete_protected"));
            entity.setSsoMethod(SsoMethod.fromString(rs.getString("sso_method")));
            entity.setDedicatedVmForVds(getGuid(rs, "dedicated_vm_for_vds"));
            entity.setFailBack(rs.getBoolean("fail_back"));
            entity.setLastVdsRunOn(getGuid(rs, "last_vds_run_on"));
            entity.setClientIp(rs.getString("client_ip"));
            entity.setGuestRequestedMemory((Integer) rs.getObject("guest_requested_memory"));
            entity.setVdsGroupCpuName(rs.getString("vds_group_cpu_name"));
            entity.setVmType(VmType.forValue(rs.getInt("vm_type")));
            entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
            entity.setStoragePoolName(rs.getString("storage_pool_name"));
            entity.setTransparentHugePages(rs.getBoolean("transparent_hugepages"));
            entity.setNiceLevel(rs.getInt("nice_level"));
            entity.setCpuShares(rs.getInt("cpu_shares"));
            entity.setHibernationVolHandle(rs.getString("hibernation_vol_handle"));
            entity.setDefaultBootSequence(BootSequence.forValue(rs.getInt("default_boot_sequence")));
            entity.setDefaultDisplayType(DisplayType.forValue(rs.getInt("default_display_type")));
            entity.setPriority(rs.getInt("priority"));
            entity.setIsoPath(rs.getString("iso_path"));
            entity.setOrigin(OriginType.forValue(rs.getInt("origin")));
            entity.setInitrdUrl(rs.getString("initrd_url"));
            entity.setKernelUrl(rs.getString("kernel_url"));
            entity.setKernelParams(rs.getString("kernel_params"));
            entity.setVdsGroupCompatibilityVersion(new Version(rs.getString("vds_group_compatibility_version")));
            entity.setExitMessage(rs.getString("exit_message"));
            entity.setExitStatus(VmExitStatus.forValue(rs.getInt("exit_status")));
            entity.setVmPauseStatus(VmPauseStatus.forValue(rs.getInt("pause_status")));
            entity.setMigrationSupport(MigrationSupport.forValue(rs.getInt("migration_support")));
            String predefinedProperties = rs.getString("predefined_properties");
            String userDefinedProperties = rs.getString("userdefined_properties");
            entity.setPredefinedProperties(predefinedProperties);
            entity.setUserDefinedProperties(userDefinedProperties);
            entity.setCustomProperties(VmPropertiesUtils.getInstance().customProperties(predefinedProperties,
                    userDefinedProperties));
            entity.setMinAllocatedMem(rs.getInt("min_allocated_mem"));
            entity.setHash(rs.getString("hash"));
            entity.setCpuPinning(rs.getString("cpu_pinning"));
            entity.setUseHostCpuFlags(rs.getBoolean("host_cpu_flags"));
            entity.setGuestAgentNicsHash(rs.getInt("guest_agent_nics_hash"));
            entity.setTunnelMigration((Boolean) rs.getObject("tunnel_migration"));
            entity.setDefaultVncKeyboardLayout(rs.getString("vnc_keyboard_layout"));
            entity.setRunAndPause(rs.getBoolean("is_run_and_pause"));
            entity.setLastWatchdogEvent(getLong(rs, "last_watchdog_event"));
            entity.setTrustedService(rs.getBoolean("trusted_service"));
            entity.setRunOnce(rs.getBoolean("is_run_once"));
            entity.setCreatedByUserId(getGuid(rs, "created_by_user_id"));
            entity.setCpuName(rs.getString("cpu_name"));
            entity.setInstanceTypeId(Guid.createGuidFromString(rs.getString("instance_type_id")));
            entity.setImageTypeId(Guid.createGuidFromString(rs.getString("image_type_id")));
            entity.setClusterArch(ArchitectureType.forValue(rs.getInt("architecture")));
            entity.setOriginalTemplateName(rs.getString("original_template_name"));
            entity.setOriginalTemplateGuid(getGuid(rs, "original_template_id"));
            entity.setVmPoolSpiceProxy(rs.getString("vm_pool_spice_proxy"));
            entity.setVdsGroupSpiceProxy(rs.getString("vds_group_spice_proxy"));
            entity.setMigrationDowntime((Integer) rs.getObject("migration_downtime"));
            // if template_version_number is null it means use latest version
            entity.setUseLatestVersion(rs.getObject("template_version_number") ==
                    VmStaticDAODbFacadeImpl.USE_LATEST_VERSION_NUMBER_INDICATOR);
            entity.setCurrentCd(rs.getString("current_cd"));
            entity.setSerialNumberPolicy(SerialNumberPolicy.forValue((Integer) rs.getObject("serial_number_policy")));
            entity.setCustomSerialNumber(rs.getString("custom_serial_number"));
            entity.setStopReason(rs.getString("reason"));
            entity.setExitReason(VmExitReason.forValue(rs.getInt("exit_reason")));
            entity.setBootMenuEnabled(rs.getBoolean("is_boot_menu_enabled"));
            entity.setGuestCpuCount(rs.getInt("guest_cpu_count"));
            entity.setNextRunConfigurationExists(rs.getBoolean("next_run_config_exists"));
            entity.setNumaTuneMode(NumaTuneMode.forValue(rs.getString("numatune_mode")));
            entity.setSpiceFileTransferEnabled(rs.getBoolean("is_spice_file_transfer_enabled"));
            entity.setSpiceCopyPasteEnabled(rs.getBoolean("is_spice_copy_paste_enabled"));
            entity.setCpuProfileId(getGuid(rs, "cpu_profile_id"));
            return entity;
        }
    }

    private static final class VMWithPlugInfoRowMapper implements RowMapper<Pair<VM, VmDevice>> {
        public static final VMWithPlugInfoRowMapper instance = new VMWithPlugInfoRowMapper();

        @Override
        public Pair<VM, VmDevice> mapRow(ResultSet rs, int rowNum) throws SQLException {
            @SuppressWarnings("synthetic-access")
            Pair<VM, VmDevice> entity = new Pair<>();
            entity.setFirst(VMRowMapper.instance.mapRow(rs, rowNum));
            entity.setSecond(VmDeviceDAODbFacadeImpl.VmDeviceRowMapper.instance.mapRow(rs, rowNum));
            return entity;
        }
    }

}
