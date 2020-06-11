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
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code VmTemplateDaoImpl} provides a concrete implementation of {@link VmTemplateDao}.
 */
@Named
@Singleton
public class VmTemplateDaoImpl extends VmBaseDao<VmTemplate> implements VmTemplateDao {

    public VmTemplateDaoImpl() {
        super("VmTemplate");
        setProcedureNameForRemove("DeleteVmTemplates");
    }

    @Override
    public VmTemplate get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public InstanceType getInstanceType(Guid id, Guid userID, boolean isFiltered) {
        VmTemplate result = get(id, userID, isFiltered);
        if (result != null && result.getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            result = null;
        }
        return result;
    }

    @Override
    public InstanceType getInstanceType(Guid id) {
        return getInstanceType(id, null, false);
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
    public VmTemplate getByName(String name, Guid datacenterId, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetVmTemplateByVmtName",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vmt_name", name)
                        .addValue("storage_pool_id", datacenterId)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public VmTemplate getInstanceTypeByName(String name, Guid userID, boolean isFiltered) {
        return getByName(name, null, userID, isFiltered);
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
    public List<VmTemplate> getAllForCluster(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplateByClusterId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", id));
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
        Map<Boolean, VmTemplate> result = new HashMap<>();
        if (plugInfo != null) {
            result.put(plugInfo.isPlugged(), plugInfo.getVmTemplate());
        }
        return result;
    }

    private VMTemplateWithPlugInfo getVMTemplatesWithPlugInfo(Guid imageId) {
        return getCallsHandler().executeRead("GetVmTemplatesByImageId",
                vmTemplateWithPlugInfoRowMapper,
                getCustomMapSqlParameterSource().addValue("image_guid", imageId));
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
                .addValue("template_version_name", template.getTemplateVersionName())
                .addValue("is_template_sealed", template.isSealed());
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
                SingleColumnRowMapper.newInstance(Integer.class),
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

    @Override
    public void shiftBaseTemplate(Guid baseTemplateId) {
        getCallsHandler().executeModification("UpdateVmTemplateShiftBaseTemplate",
                getCustomMapSqlParameterSource().addValue("base_template_id", baseTemplateId));
    }

    @Override
    public List<VmTemplate> getAllForCpuProfile(Guid cpuProfileId) {
        return getCallsHandler().executeReadList("GetVmTemplatesByCpuProfileId",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cpu_profile_id", cpuProfileId));
    }

    @Override
    public List<VmTemplate> getAllForDiskProfile(Guid diskProfileId) {
        return getCallsHandler().executeReadList("GetAllVmTemplatesRelatedToDiskProfile",
                VMTemplateRowMapper.instance, getCustomMapSqlParameterSource()
                        .addValue("disk_profile_id", diskProfileId));
    }

    @Override
    public List<VmTemplate> getAllWithLeaseOnStorageDomain(Guid storageDomainId) {
        return getCallsHandler().executeReadList("GetTemplatesWithLeaseOnStorageDomain",
                VMTemplateRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("storage_domain_id", storageDomainId));
    }


    private static final class VMTemplateRowMapper extends AbstractVmRowMapper<VmTemplate> {
        public static final VMTemplateRowMapper instance = new VMTemplateRowMapper();

        @Override
        public VmTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            final VmTemplate entity = new VmTemplate();
            map(rs, entity);

            entity.setId(getGuidDefaultEmpty(rs, "vmt_guid"));
            entity.setChildCount(rs.getInt("child_count"));
            entity.setName(rs.getString("name"));
            entity.setClusterId(getGuid(rs, "cluster_id"));
            entity.setStatus(VmTemplateStatus.forValue(rs.getInt("status")));
            entity.setClusterName(rs.getString("cluster_name"));
            entity.setClusterCompatibilityVersion(new VersionRowMapper("cluster_compatibility_version").mapRow(rs, rowNum));
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
            entity.setSealed(rs.getBoolean("is_template_sealed"));
            entity.setClusterBiosType(BiosType.forValue(rs.getInt("cluster_bios_type")));
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

    private static final RowMapper<VMTemplateWithPlugInfo> vmTemplateWithPlugInfoRowMapper = (rs, rowNum) -> {
        VMTemplateWithPlugInfo entity = new VMTemplateWithPlugInfo();

        entity.setPlugged(rs.getBoolean("is_plugged"));
        entity.setVmTemplate(VMTemplateRowMapper.instance.mapRow(rs, rowNum));
        return entity;
    };
}
