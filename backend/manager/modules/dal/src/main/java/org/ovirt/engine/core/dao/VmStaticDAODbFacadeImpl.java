package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.AbstractVmRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class VmStaticDAODbFacadeImpl extends BaseDAODbFacade implements VmStaticDAO {
    public static final Integer USE_LATEST_VERSION_NUMBER_INDICATOR = null;
    public static final Integer DONT_USE_LATEST_VERSION_NUMBER_INDICATOR = 1;

    @Override
    public VmStatic get(Guid id) {
        return getCallsHandler().executeRead("GetVmStaticByVmGuid",
                VMStaticRowMapper.instance,
                getIdParamterSource(id));
    }

    @Override
    public List<VmStatic> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public void save(VmStatic vm) {
        getCallsHandler().executeModification("InsertVmStatic", getFullParameterSource(vm));

    }

    @Override
    public void update(VmStatic vm) {
        getCallsHandler().executeModification("UpdateVmStatic", getFullParameterSource(vm));
    }

    private MapSqlParameterSource getFullParameterSource(VmStatic vm) {
        return getIdParamterSource(vm.getId())
                .addValue("description", vm.getDescription())
                .addValue("free_text_comment", vm.getComment())
                .addValue("mem_size_mb", vm.getMemSizeMb())
                .addValue("os", vm.getOsId())
                .addValue("vds_group_id", vm.getVdsGroupId())
                .addValue("vm_name", vm.getName())
                .addValue("vmt_guid", vm.getVmtGuid())
                .addValue("creation_date", vm.getCreationDate())
                .addValue("num_of_monitors", vm.getNumOfMonitors())
                .addValue("single_qxl_pci", vm.getSingleQxlPci())
                .addValue("is_initialized", vm.isInitialized())
                .addValue("num_of_sockets", vm.getNumOfSockets())
                .addValue("cpu_per_socket", vm.getCpuPerSocket())
                .addValue("usb_policy", vm.getUsbPolicy())
                .addValue("time_zone", vm.getTimeZone())
                .addValue("auto_startup", vm.isAutoStartup())
                .addValue("is_stateless", vm.isStateless())
                .addValue("is_smartcard_enabled", vm.isSmartcardEnabled())
                .addValue("is_delete_protected", vm.isDeleteProtected())
                .addValue("sso_method", vm.getSsoMethod().toString())
                .addValue("dedicated_vm_for_vds", vm.getDedicatedVmForVds())
                .addValue("fail_back", vm.isFailBack())
                .addValue("vm_type", vm.getVmType())
                .addValue("nice_level", vm.getNiceLevel())
                .addValue("cpu_shares", vm.getCpuShares())
                .addValue("default_boot_sequence",
                        vm.getDefaultBootSequence())
                .addValue("default_display_type", vm.getDefaultDisplayType())
                .addValue("priority", vm.getPriority())
                .addValue("iso_path", vm.getIsoPath())
                .addValue("origin", vm.getOrigin())
                .addValue("initrd_url", vm.getInitrdUrl())
                .addValue("kernel_url", vm.getKernelUrl())
                .addValue("kernel_params", vm.getKernelParams())
                .addValue("migration_support",
                        vm.getMigrationSupport().getValue())
                .addValue("predefined_properties", vm.getPredefinedProperties())
                .addValue("userdefined_properties",
                        vm.getUserDefinedProperties())
                .addValue("min_allocated_mem", vm.getMinAllocatedMem())
                .addValue("quota_id", vm.getQuotaId())
                .addValue("allow_console_reconnect", vm.isAllowConsoleReconnect())
                .addValue("cpu_pinning", vm.getCpuPinning())
                .addValue("host_cpu_flags", vm.isUseHostCpuFlags())
                .addValue("tunnel_migration", vm.getTunnelMigration())
                .addValue("vnc_keyboard_layout", vm.getVncKeyboardLayout())
                .addValue("is_run_and_pause", vm.isRunAndPause())
                .addValue("created_by_user_id", vm.getCreatedByUserId())
                .addValue("instance_type_id", vm.getInstanceTypeId())
                .addValue("image_type_id", vm.getImageTypeId())
                .addValue("original_template_name", vm.getOriginalTemplateName())
                .addValue("original_template_id", vm.getOriginalTemplateGuid())
                .addValue("migration_downtime", vm.getMigrationDowntime())
                .addValue("template_version_number", vm.isUseLatestVersion() ?
                        USE_LATEST_VERSION_NUMBER_INDICATOR : DONT_USE_LATEST_VERSION_NUMBER_INDICATOR)
                .addValue("serial_number_policy", vm.getSerialNumberPolicy() == null ? null : vm.getSerialNumberPolicy().getValue())
                .addValue("custom_serial_number", vm.getCustomSerialNumber())
                .addValue("is_boot_menu_enabled", vm.isBootMenuEnabled())
                .addValue("numatune_mode",
                        vm.getNumaTuneMode() == null ? NumaTuneMode.PREFERRED.getValue() : vm.getNumaTuneMode()
                                .getValue())
                .addValue("is_spice_file_transfer_enabled", vm.isSpiceFileTransferEnabled())
                .addValue("is_spice_copy_paste_enabled", vm.isSpiceCopyPasteEnabled());
    }

    @Override
    public void remove(Guid id) {
        remove(id, true);
    }

    public void remove(Guid id, boolean removePermissions) {
        getCallsHandler().executeModification("DeleteVmStatic",
                getIdParamterSource(id)
                        .addValue("remove_permissions", removePermissions));
    }

    private MapSqlParameterSource getIdParamterSource(Guid id) {
        return getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);
    }

    @Override
    public List<VmStatic> getAllByName(String name) {
        return getCallsHandler().executeReadList("GetVmStaticByName", VMStaticRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("vm_name", name));

    }

    @Override
    public List<VmStatic> getAllByStoragePoolId(Guid spId) {
        return getCallsHandler().executeReadList("GetAllFromVmStaticByStoragePoolId",
                VMStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("sp_id", spId));
    }

    @Override
    public List<VmStatic> getAllByVdsGroup(Guid vdsGroup) {
        return getCallsHandler().executeReadList("GetVmStaticByVdsGroup", VMStaticRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("vds_group_id", vdsGroup));
    }

    @Override
    public List<VmStatic> getAllWithFailbackByVds(Guid vds) {
        return getCallsHandler().executeReadList("GetVmStaticWithFailbackByVdsId", VMStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vds));
    }

    @Override
    public List<VmStatic> getAllByGroupAndNetworkName(Guid group, String name) {
        return getCallsHandler().executeReadList("GetvmStaticByGroupIdAndNetwork", VMStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("groupId", group).addValue("networkName", name));
    }

    @Override
    public List<String> getAllNamesPinnedToHost(Guid host) {
        RowMapper<String> mapper = new RowMapper<String>() {

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("vm_name");
            }
        };

        return getCallsHandler().executeReadList("GetNamesOfVmStaticDedicatedToVds", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", host));
    }

    @Override
    public void incrementDbGenerationForAllInStoragePool(Guid storagePoolId) {
        getCallsHandler().executeModification("IncrementDbGenerationForAllInStoragePool", getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId));

    }

    @Override
    public void incrementDbGeneration(Guid id) {
        getCallsHandler().executeModification("IncrementDbGeneration", getCustomMapSqlParameterSource()
                .addValue("vm_guid", id));
    }

    @Override
    public Long getDbGeneration(Guid id) {
        return getCallsHandler().executeRead("GetDbGeneration", getLongMapper()
                , getCustomMapSqlParameterSource()
                .addValue("vm_guid", id));
    }

    public List<Guid> getOrderedVmGuidsForRunMultipleActions(List<Guid> guids) {
        // Constructing an IN clause of SQL that contains a list of GUIDs
        // The in clause looks like ('guid1','guid2','guid3')
        StringBuilder guidsSb = new StringBuilder();
        guidsSb.append("'").append(StringUtils.join(guids, "','")).append("'");

        return getCallsHandler().executeReadList("GetOrderedVmGuidsForRunMultipleActions", createGuidMapper()
                , getCustomMapSqlParameterSource().addValue("vm_guids", guidsSb
                .toString()));
    }

    /**
     * JDBC row mapper for VM static
     */
    private static class VMStaticRowMapper extends AbstractVmRowMapper<VmStatic> {
        public static final VMStaticRowMapper instance = new VMStaticRowMapper();

        @Override
        public VmStatic mapRow(ResultSet rs, int rowNum) throws SQLException {
            final VmStatic entity = new VmStatic();
            map(rs, entity);

            entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
            entity.setVdsGroupId(getGuidDefaultEmpty(rs, "vds_group_id"));

            entity.setName(rs.getString("vm_name"));
            entity.setVmtGuid(getGuidDefaultEmpty(rs, "vmt_guid"));
            entity.setInitialized(rs.getBoolean("is_initialized"));
            String predefinedProperties = rs.getString("predefined_properties");
            String userDefinedProperties = rs.getString("userdefined_properties");
            entity.setPredefinedProperties(predefinedProperties);
            entity.setUserDefinedProperties(userDefinedProperties);
            entity.setCustomProperties(VmPropertiesUtils.getInstance().customProperties(predefinedProperties,
                    userDefinedProperties));
            entity.setCpuPinning(rs.getString("cpu_pinning"));
            entity.setUseHostCpuFlags(rs.getBoolean("host_cpu_flags"));
            entity.setInstanceTypeId(Guid.createGuidFromString(rs.getString("instance_type_id")));
            entity.setImageTypeId(Guid.createGuidFromString(rs.getString("image_type_id")));
            entity.setOriginalTemplateName(rs.getString("original_template_name"));
            entity.setOriginalTemplateGuid(getGuid(rs, "original_template_id"));
            // if template_version_number is null it means use latest version
            entity.setUseLatestVersion(rs.getObject("template_version_number") == USE_LATEST_VERSION_NUMBER_INDICATOR);
            entity.setNumaTuneMode(NumaTuneMode.forValue(rs.getString("numatune_mode")));

            return entity;
        }
    }

}
