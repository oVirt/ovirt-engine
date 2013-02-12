package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.AbstractVmRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VmTemplateDAODbFacadeImpl</code> provides a concrete implementation of {@link VmTemplateDAO}.
 */
public class VmTemplateDAODbFacadeImpl extends BaseDAODbFacade implements VmTemplateDAO {

    @Override
    public VmTemplate get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VmTemplate get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmTemplateByVmtGuid",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vmt_guid", id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public VmTemplate getByName(String name, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmTemplateByVmtName",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vmt_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VmTemplate> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VmTemplate> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromVmTemplates",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VmTemplate> getVmTemplatesByIds(List<Guid> templatesIds) {
        return getCallsHandler().executeReadList("GetVmTemplatesByIds",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vm_templates_ids", StringUtils.join(templatesIds,',')));
    }

    @Override
    public List<VmTemplate> getAllForStorageDomain(Guid storageDomain) {
        return getAllForStorageDomain(storageDomain, null, false);
    }

    @Override
    public List<VmTemplate> getAllForStorageDomain(Guid storageDomain, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetVmTemplatesByStorageDomainId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", storageDomain)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VmTemplate> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, VMTemplateRowMapper.instance);
    }

    @Override
    public List<VmTemplate> getAllForVdsGroup(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplateByVdsGroupId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", id));
    }

    @Override
    public List<VmTemplate> getAllTemplatesRelatedToQuotaId(Guid quotaId) {
        return getCallsHandler().executeReadList("GetAllVmTemplatesRelatedToQuotaId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("quota_id", quotaId));
    }

    @Override
    public Map<Boolean, VmTemplate> getAllForImage(Guid imageId) {
        VMTemplateWithPlugInfo plugInfo = getVMTemplatesWithPlugInfo(imageId);
        Map<Boolean, VmTemplate> result = new HashMap<Boolean, VmTemplate>();
        if (plugInfo != null) {
            result.put(plugInfo.isPlugged(), plugInfo.getVmTemplate());
        }
        return result;
    }

    private VMTemplateWithPlugInfo getVMTemplatesWithPlugInfo(Guid imageId) {
        VMTemplateWithPlugInfo plugInfo =
                getCallsHandler().executeRead("GetVmTemplatesByImageId",
                        VMTemplateWithPlugInfoRowMapper.instance,
                        getCustomMapSqlParameterSource().addValue("image_guid", imageId));
        return plugInfo;
    }

    @Override
    public void save(VmTemplate template) {
        getCallsHandler().executeModification("InsertVmTemplate", getInsertOrUpdateParameters(template));
    }

    private MapSqlParameterSource getInsertOrUpdateParameters(VmTemplate template) {
        return getCustomMapSqlParameterSource()
                .addValue("child_count", template.getchild_count())
                .addValue("creation_date", template.getCreationDate())
                .addValue("description", template.getDescription())
                .addValue("mem_size_mb", template.getMemSizeMb())
                .addValue("name", template.getName())
                .addValue("num_of_sockets", template.getNumOfSockets())
                .addValue("cpu_per_socket", template.getCpuPerSocket())
                .addValue("os", template.getOs())
                .addValue("vmt_guid", template.getId())
                .addValue("vds_group_id", template.getVdsGroupId())
                .addValue("domain", template.getDomain())
                .addValue("num_of_monitors", template.getNumOfMonitors())
                .addValue("allow_console_reconnect", template.isAllowConsoleReconnect())
                .addValue("status", template.getstatus())
                .addValue("usb_policy", template.getUsbPolicy())
                .addValue("time_zone", template.getTimeZone())
                .addValue("fail_back", template.isFailBack())
                .addValue("is_auto_suspend", template.isAutoSuspend())
                .addValue("vm_type", template.getVmType())
                .addValue("nice_level", template.getNiceLevel())
                .addValue("default_boot_sequence",
                        template.getDefaultBootSequence())
                .addValue("default_display_type",
                        template.getDefaultDisplayType())
                .addValue("priority", template.getPriority())
                .addValue("auto_startup", template.isAutoStartup())
                .addValue("is_stateless", template.isStateless())
                .addValue("is_smartcard_enabled", template.isSmartcardEnabled())
                .addValue("is_delete_protected", template.isDeleteProtected())
                .addValue("iso_path", template.getIsoPath())
                .addValue("origin", template.getOrigin())
                .addValue("initrd_url", template.getInitrdUrl())
                .addValue("kernel_url", template.getKernelUrl())
                .addValue("kernel_params", template.getKernelParams())
                .addValue("is_disabled", template.isDisabled())
                .addValue("quota_id", template.getQuotaId())
                .addValue("migration_support", template.getMigrationSupport().getValue())
                .addValue("dedicated_vm_for_vds", template.getDedicatedVmForVds())
                .addValue("tunnel_migration", template.getTunnelMigration())
                .addValue("vnc_keyboard_layout", template.getVncKeyboardLayout());
    }

    @Override
    public void update(VmTemplate template) {
        getCallsHandler().executeModification("UpdateVmTemplate", getInsertOrUpdateParameters(template));
    }

    @Override
    public void updateStatus(Guid id, VmTemplateStatus status) {
        getCallsHandler().executeModification("UpdateVmTemplateStatus", getCustomMapSqlParameterSource()
                .addValue("vmt_guid", id)
                .addValue("status", status));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteVmTemplates", getCustomMapSqlParameterSource()
                .addValue("vmt_guid", id));
    }

    @Override
    public List<VmTemplate> getTemplatesWithPermittedAction(Guid userId, ActionGroup actionGroup) {
        return getCallsHandler().executeReadList("fn_perms_get_templates_with_permitted_action",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId()));
    }

    @Override
    public List<VmTemplate> getAllForNetwork(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplatesByNetworkId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("network_id", id));
    }

    private final static class VMTemplateRowMapper extends AbstractVmRowMapper<VmTemplate> {
        public static final VMTemplateRowMapper instance = new VMTemplateRowMapper();

        @Override
        public VmTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            final VmTemplate entity = new VmTemplate();
            map(rs, entity);

            entity.setId(Guid.createGuidFromString(rs.getString("vmt_guid")));
            entity.setchild_count(rs.getInt("child_count"));
            entity.setMemSizeMb(rs.getInt("mem_size_mb"));
            entity.setName(rs.getString("name"));
            entity.setOs(VmOsType.forValue(rs.getInt("os")));
            entity.setVdsGroupId(Guid.createGuidFromString(rs.getString("vds_group_id")));
            entity.setDomain(rs.getString("domain"));
            entity.setNumOfMonitors(rs.getInt("num_of_monitors"));
            entity.setAllowConsoleReconnect(rs.getBoolean("allow_console_reconnect"));
            entity.setstatus(VmTemplateStatus.forValue(rs.getInt("status")));
            entity.setvds_group_name(rs.getString("vds_group_name"));
            entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
            entity.setstorage_pool_name(rs.getString("storage_pool_name"));
            entity.setDefaultDisplayType(DisplayType.forValue(rs.getInt("default_display_type")));
            entity.setQuotaId(Guid.createGuidFromString(rs.getString("quota_id")));
            entity.setQuotaName(rs.getString("quota_name"));
            entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
            entity.setMigrationSupport(MigrationSupport.forValue(rs.getInt("migration_support")));
            entity.setDedicatedVmForVds(NGuid.createGuidFromString(rs.getString("dedicated_vm_for_vds")));
            entity.setDisabled(rs.getBoolean("is_disabled"));
            entity.setTunnelMigration((Boolean) rs.getObject("tunnel_migration"));
            entity.setVncKeyboardLayout(rs.getString("vnc_keyboard_layout"));
            return entity;
        }
    }

    private static class VMTemplateWithPlugInfo {

        public VmTemplate getVmTemplate() {
            return vmTemplate;
        }

        public void setVmTemplate(VmTemplate vmTemplate) {
            this.vmTemplate = vmTemplate;
        }

        public boolean isPlugged() {
            return isPlugged;
        }

        public void setPlugged(boolean isPlugged) {
            this.isPlugged = isPlugged;
        }

        private VmTemplate vmTemplate;
        private boolean isPlugged;
    }

    private static final class VMTemplateWithPlugInfoRowMapper implements ParameterizedRowMapper<VMTemplateWithPlugInfo> {
        public static final VMTemplateWithPlugInfoRowMapper instance = new VMTemplateWithPlugInfoRowMapper();

        @Override
        public VMTemplateWithPlugInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            @SuppressWarnings("synthetic-access")
            VMTemplateWithPlugInfo entity = new VMTemplateWithPlugInfo();

            entity.setPlugged(rs.getBoolean("is_plugged"));
            entity.setVmTemplate(VMTemplateRowMapper.instance.mapRow(rs, rowNum));
            return entity;
        }
    }
}
