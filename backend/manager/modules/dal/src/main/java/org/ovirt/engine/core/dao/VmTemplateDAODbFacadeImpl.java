package org.ovirt.engine.core.dao;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.VMTemplateRowMapper;

/**
 * <code>VmTemplateDAODbFacadeImpl</code> provides a concrete implementation of {@link VmTemplateDAO}.
 */
public class VmTemplateDAODbFacadeImpl extends BaseDAODbFacade implements VmTemplateDAO {

    @Override
    public VmTemplate get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vmt_guid", id);

        ParameterizedRowMapper<VmTemplate> mapper = new VMTemplateRowMapper();
        return getCallsHandler().executeRead("GetVmTemplateByVmtGuid", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VmTemplate> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<VmTemplate> mapper = new VMTemplateRowMapper();
        return getCallsHandler().executeReadList("GetAllFromVmTemplates",mapper,parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VmTemplate> getAllForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        ParameterizedRowMapper<VmTemplate> mapper = new VMTemplateRowMapper();
        return getCallsHandler().executeReadList("GetVmTemplatesByStorageDomainId", mapper, parameterSource);
    }

    @Override
    public List<VmTemplate> getAllWithQuery(String query) {
        ParameterizedRowMapper<VmTemplate> mapper = new VMTemplateRowMapper();
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VmTemplate> getAllForVdsGroup(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_group_id", id);

        ParameterizedRowMapper<VmTemplate> mapper = new VMTemplateRowMapper();
        return getCallsHandler().executeReadList("GetVmTemplateByVdsGroupId", mapper, parameterSource);
    }

    @Override
    public void save(VmTemplate template) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
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
                .addValue("kernel_params", template.getkernel_params());

        getCallsHandler().executeModification("InsertVmTemplate", parameterSource);
    }

    @Override
    public void update(VmTemplate template) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
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
                .addValue("kernel_params", template.getkernel_params());

        getCallsHandler().executeModification("UpdateVmTemplate", parameterSource);
    }

    @Override
    public void updateStatus(Guid id, VmTemplateStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vmt_guid", id)
                .addValue("status", status);

        getCallsHandler().executeModification("UpdateVmTemplateStatus", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vmt_guid", id);

        getCallsHandler().executeModification("DeleteVmTemplates", parameterSource);
    }

    @Override
    public List<VmTemplate> getTemplatesWithPermittedAction(Guid userId, ActionGroup actionGroup) {
            MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                    .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId());

            VMTemplateRowMapper mapper = new VMTemplateRowMapper();

            return getCallsHandler().executeReadList("fn_perms_get_templates_with_permitted_action",mapper,parameterSource);
    }
}
