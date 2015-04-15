package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>VmTemplateDAODbFacadeImpl</code> provides a concrete implementation of {@link VmTemplateDAO}.
 */

@Named
@Singleton
public class VmTemplateDAODbFacadeImpl extends VmBaseDaoDbFacade<VmTemplate> implements VmTemplateDAO {

    public VmTemplateDAODbFacadeImpl() {
        super("VmTemplate");
        setProcedureNameForRemove("DeleteVmTemplates");
    }

    @Override
    public VmTemplate get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public InstanceType getInstanceType(Guid id) {
        VmTemplate result = get(id);
        if (result != null && result.getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            result = null;
        }
        return result;
    }

    @Override
    public ImageType getImageType(Guid id) {
        VmTemplate result = get(id);
        if (result != null && result.getTemplateType() != VmEntityType.IMAGE_TYPE) {
            result = null;
        }
        return result;
    }

    @Override
    public VmTemplate get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmTemplateByVmtGuid",
                VMTemplateRowMapper.instance,
                createIdParameterMapper(id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
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
        return getAll(null, false, VmEntityType.TEMPLATE);
    }

    @Override
    public List<VmTemplate> getAll(Guid userID, boolean isFiltered, VmEntityType entityType) {
        return getCallsHandler().executeReadList("GetAllFromVmTemplates",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("entity_type", entityType.name())
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<VmTemplate> getVmTemplatesByIds(List<Guid> templatesIds) {
        return getCallsHandler().executeReadList("GetVmTemplatesByIds",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vm_templates_ids", StringUtils.join(templatesIds, ',')));
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
        return getJdbcTemplate().query(query, VMTemplateRowMapper.instance);
    }

    @Override
    public List<VmTemplate> getAllForVdsGroup(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplateByVdsGroupId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", id));
    }

    @Override
    public List<VmTemplate> getAllForStoragePool(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplatesByStoragePoolId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", id));
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
    public List<VmTemplate> getAllTemplatesWithDisksOnOtherStorageDomain(Guid storageDomainGuid) {
        return getCallsHandler().executeReadList("GetAllVmTemplatesWithDisksOnOtherStorageDomain",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainGuid));
    }

    @Override
    public List<VmTemplate> getAllWithoutIcon() {
        return getCallsHandler().executeReadList("GetVmTemplatesWithoutIcon",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource());
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmTemplate template) {
        return createBaseParametersMapper(template)
                .addValue("child_count", template.getChildCount())
                .addValue("name", template.getName())
                .addValue("status", template.getStatus())
                .addValue("is_disabled", template.isDisabled())
                .addValue("template_type", template.getTemplateType().name())
                .addValue("base_template_id", template.getBaseTemplateId())
                .addValue("template_version_name", template.getTemplateVersionName());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vmt_guid", id);
    }

    @Override
    public void updateStatus(Guid id, VmTemplateStatus status) {
        getCallsHandler().executeModification("UpdateVmTemplateStatus", createIdParameterMapper(id).addValue("status", status));
    }

    @Override
    public List<VmTemplate> getTemplatesWithPermittedAction(Guid userId, ActionGroup actionGroup) {
        return getCallsHandler().executeReadList("fn_perms_get_templates_with_permitted_action",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId()));
    }

    @Override
    public int getCount() {
        return getCallsHandler().executeRead("GetTemplateCount",
                getIntegerMapper(),
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<VmTemplate> getAllForNetwork(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplatesByNetworkId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("network_id", id));
    }

    @Override
    public List<VmTemplate> getAllForVnicProfile(Guid vnicProfileId) {
        return getCallsHandler().executeReadList("GetVmTemplatesByVnicProfileId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vnic_profile_id", vnicProfileId));
    }

    @Override
    public List<VmTemplate> getTemplateVersionsForBaseTemplate(Guid id) {
        return getCallsHandler().executeReadList("GetTemplateVersionsForBaseTemplate",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("base_template_id", id));
    }

    @Override
    public VmTemplate getTemplateWithLatestVersionInChain(Guid id) {
        return getCallsHandler().executeRead("GetTemplateWithLatestVersionInChain",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("template_id", id));
    }

    private final static class VMTemplateRowMapper extends AbstractVmRowMapper<VmTemplate> {
        public static final VMTemplateRowMapper instance = new VMTemplateRowMapper();

        @Override
        public VmTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            final VmTemplate entity = new VmTemplate();
            map(rs, entity);

            entity.setId(getGuidDefaultEmpty(rs, "vmt_guid"));
            entity.setChildCount(rs.getInt("child_count"));
            entity.setName(rs.getString("name"));
            entity.setVdsGroupId(getGuid(rs, "vds_group_id"));
            entity.setStatus(VmTemplateStatus.forValue(rs.getInt("status")));
            entity.setVdsGroupName(rs.getString("vds_group_name"));
            entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
            entity.setStoragePoolName(rs.getString("storage_pool_name"));
            entity.setQuotaName(rs.getString("quota_name"));
            entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
            entity.setDisabled(rs.getBoolean("is_disabled"));
            entity.setTrustedService(rs.getBoolean("trusted_service"));
            entity.setTemplateType(VmEntityType.valueOf(rs.getString("entity_type")));
            entity.setClusterArch(ArchitectureType.forValue(rs.getInt("architecture")));
            entity.setBaseTemplateId(getGuidDefaultEmpty(rs, "base_template_id"));
            entity.setTemplateVersionNumber(rs.getInt("template_version_number"));
            entity.setTemplateVersionName(rs.getString("template_version_name"));
            return entity;
        }
    }

    @Override
    protected RowMapper<VmTemplate> createEntityRowMapper() {
        return VMTemplateRowMapper.instance;
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

    private static final class VMTemplateWithPlugInfoRowMapper implements RowMapper<VMTemplateWithPlugInfo> {
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
