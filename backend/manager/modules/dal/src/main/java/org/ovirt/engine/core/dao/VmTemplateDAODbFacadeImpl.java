package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.AbstractVmRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VmTemplateDAODbFacadeImpl</code> provides a concrete implementation of {@link VmTemplateDAO}.
 */
public class VmTemplateDAODbFacadeImpl extends BaseDAODbFacade implements VmTemplateDAO {

    @Override
    public VmTemplate get(Guid id) {
        return getCallsHandler().executeRead("GetVmTemplateByVmtGuid",
                new VMTemplateRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vmt_guid", id));
    }

    @Override
    public List<VmTemplate> getAll() {
        return getCallsHandler().executeReadList("GetAllFromVmTemplates",
                new VMTemplateRowMapper(),
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<VmTemplate> getAllForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplatesByStorageDomainId",
                new VMTemplateRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", id));
    }

    @Override
    public List<VmTemplate> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, new VMTemplateRowMapper());
    }

    @Override
    public List<VmTemplate> getAllForVdsGroup(Guid id) {
        return getCallsHandler().executeReadList("GetVmTemplateByVdsGroupId",
                new VMTemplateRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", id));
    }

    @Override
    public List<VmTemplate> getAllTemplatesRelatedToQuotaId(Guid quotaId) {
        return getCallsHandler().executeReadList("GetAllVmTemplatesRelatedToQuotaId",
                new VMTemplateRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("quota_id", quotaId));
    }

    @Override
    public void save(VmTemplate template) {
        getCallsHandler().executeModification("InsertVmTemplate", getInsertOrUpdateParameters(template));
    }

    private MapSqlParameterSource getInsertOrUpdateParameters(VmTemplate template) {
        return getCustomMapSqlParameterSource()
                .addValue("child_count", template.getchild_count())
                .addValue("creation_date", template.getcreation_date())
                .addValue("description", template.getdescription())
                .addValue("mem_size_mb", template.getmem_size_mb())
                .addValue("name", template.getname())
                .addValue("num_of_sockets", template.getnum_of_sockets())
                .addValue("cpu_per_socket", template.getcpu_per_socket())
                .addValue("os", template.getos())
                .addValue("vmt_guid", template.getId())
                .addValue("vds_group_id", template.getvds_group_id())
                .addValue("domain", template.getdomain())
                .addValue("num_of_monitors", template.getnum_of_monitors())
                .addValue("status", template.getstatus())
                .addValue("usb_policy", template.getusb_policy())
                .addValue("time_zone", template.gettime_zone())
                .addValue("fail_back", template.getfail_back())
                .addValue("is_auto_suspend", template.getis_auto_suspend())
                .addValue("vm_type", template.getvm_type())
                .addValue("hypervisor_type", template.gethypervisor_type())
                .addValue("operation_mode", template.getoperation_mode())
                .addValue("nice_level", template.getnice_level())
                .addValue("default_boot_sequence",
                        template.getdefault_boot_sequence())
                .addValue("default_display_type",
                        template.getdefault_display_type())
                .addValue("priority", template.getpriority())
                .addValue("auto_startup", template.getauto_startup())
                .addValue("is_stateless", template.getis_stateless())
                .addValue("iso_path", template.getiso_path())
                .addValue("origin", template.getorigin())
                .addValue("initrd_url", template.getinitrd_url())
                .addValue("kernel_url", template.getkernel_url())
                .addValue("kernel_params", template.getkernel_params())
                .addValue("quota_id", template.getQuotaId());
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
                new VMTemplateRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId()));
    }

    final static class VMTemplateRowMapper extends AbstractVmRowMapper<VmTemplate> {

        @Override
        public VmTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            final VmTemplate entity = new VmTemplate();
            map(rs, entity);

            entity.setId(Guid.createGuidFromString(rs.getString("vmt_guid")));
            entity.setchild_count(rs.getInt("child_count"));
            entity.setmem_size_mb(rs.getInt("mem_size_mb"));
            entity.setname(rs.getString("name"));
            // entity.setnum_of_cpus(rs.getInt("num_of_cpus"));
            entity.setos(VmOsType.forValue(rs.getInt("os")));
            entity.setvds_group_id(Guid.createGuidFromString(rs.getString("vds_group_id")));
            entity.setdomain(rs.getString("domain"));
            entity.setnum_of_monitors(rs.getInt("num_of_monitors"));
            entity.setstatus(VmTemplateStatus.forValue(rs.getInt("status")));
            entity.setvds_group_name(rs.getString("vds_group_name"));
            entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
            entity.setstorage_pool_name(rs.getString("storage_pool_name"));
            entity.setdefault_display_type(DisplayType.forValue(rs.getInt("default_display_type")));
            entity.setQuotaId(Guid.createGuidFromString(rs.getString("quota_id")));
            entity.setQuotaName(rs.getString("quota_name"));
            return entity;
        }
    }
}
