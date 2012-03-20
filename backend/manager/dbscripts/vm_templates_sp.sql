

----------------------------------------------------------------
-- [vm_templates] Table
--




Create or replace FUNCTION InsertVmTemplate(v_child_count INTEGER,  
 v_creation_date TIMESTAMP WITH TIME ZONE,  
 v_description VARCHAR(4000) ,  
 v_mem_size_mb INTEGER,  
 v_name VARCHAR(40),  
 v_num_of_sockets INTEGER,  
 v_cpu_per_socket INTEGER,  
 v_os INTEGER,  
 v_vmt_guid UUID,  
 v_vds_group_id UUID,  
 v_domain VARCHAR(40),  
 v_num_of_monitors INTEGER,  
 v_status INTEGER,  
 v_usb_policy INTEGER,  
 v_time_zone VARCHAR(40) ,  
 v_fail_back BOOLEAN ,  
 v_is_auto_suspend BOOLEAN,  
 v_vm_type INTEGER ,  
 v_hypervisor_type INTEGER ,  
 v_operation_mode INTEGER ,  
 v_nice_level INTEGER,  
 v_default_boot_sequence INTEGER,  
 v_default_display_type INTEGER,  
 v_priority INTEGER,  
 v_auto_startup BOOLEAN,  
 v_is_stateless BOOLEAN,  
 v_iso_path VARCHAR(4000) ,  
 v_origin INTEGER ,
 v_initrd_url    VARCHAR(4000) ,
 v_kernel_url    VARCHAR(4000) ,
 v_kernel_params VARCHAR(4000) ,
 v_quota_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT
INTO vm_static(
    vmt_guid,
    child_count,
    creation_date,
    description,
    mem_size_mb,
    vm_name,
    num_of_sockets,
    cpu_per_socket,
    os,
    vm_guid,
    vds_group_id,
    domain,
    num_of_monitors,
    template_status,
    usb_policy,
    time_zone,
    fail_back,
    is_auto_suspend,
    vm_type,
    hypervisor_type,
    operation_mode,
    nice_level,
    default_boot_sequence,
    default_display_type,
    priority,
    auto_startup,
    is_stateless,
    iso_path,
    origin,
    initrd_url,
    kernel_url,
    kernel_params,
    entity_type,
    quota_id)
VALUES(
    -- This field is meaningless for templates for the time being, however we want to keep it not null for VMs.
    -- Thus, since templates are top level elements they "point" to the 'Blank' template.
    '00000000-0000-0000-0000-000000000000',
    v_child_count,
    v_creation_date,
    v_description,
    v_mem_size_mb,
    v_name,
    v_num_of_sockets,
    v_cpu_per_socket,
    v_os,
    v_vmt_guid,
    v_vds_group_id,
    v_domain,
    v_num_of_monitors,
    v_status,
    v_usb_policy,
    v_time_zone,
    v_fail_back,
    v_is_auto_suspend,
    v_vm_type,
    v_hypervisor_type,
    v_operation_mode,
    v_nice_level,
    v_default_boot_sequence,
    v_default_display_type,
    v_priority,
    v_auto_startup,
    v_is_stateless,
    v_iso_path,
    v_origin,
    v_initrd_url,
    v_kernel_url,
    v_kernel_params,
    'TEMPLATE',
    v_quota_id);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION UpdateVmTemplate(v_child_count INTEGER,  
 v_creation_date TIMESTAMP WITH TIME ZONE,  
 v_description VARCHAR(4000) ,  
 v_mem_size_mb INTEGER,  
 v_name VARCHAR(40),  
 v_num_of_sockets INTEGER,  
 v_cpu_per_socket INTEGER,  
 v_os INTEGER,  
 v_vmt_guid UUID,  
 v_vds_group_id UUID,  
 v_domain VARCHAR(40),  
 v_num_of_monitors INTEGER,  
 v_status INTEGER,  
 v_usb_policy INTEGER,  
 v_time_zone VARCHAR(40) ,  
 v_fail_back BOOLEAN ,  
 v_is_auto_suspend BOOLEAN,  
 v_vm_type INTEGER ,  
 v_hypervisor_type INTEGER ,  
 v_operation_mode INTEGER ,  
 v_nice_level INTEGER,  
 v_default_boot_sequence INTEGER,  
 v_default_display_type INTEGER,  
 v_priority INTEGER,  
 v_auto_startup BOOLEAN,  
 v_is_stateless BOOLEAN,  
 v_iso_path VARCHAR(4000) ,  
 v_origin INTEGER ,
 v_initrd_url VARCHAR(4000) ,
 v_kernel_url VARCHAR(4000) ,
 v_kernel_params VARCHAR(4000),
 v_quota_id UUID)
RETURNS VOID

	--The [vm_templates] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET child_count = v_child_count,creation_date = v_creation_date,description = v_description, 
      mem_size_mb = v_mem_size_mb,vm_name = v_name,num_of_sockets = v_num_of_sockets,
      cpu_per_socket = v_cpu_per_socket,os = v_os, 
      vds_group_id = v_vds_group_id,domain = v_domain,num_of_monitors = v_num_of_monitors,
      template_status = v_status,usb_policy = v_usb_policy,time_zone = v_time_zone,
      fail_back = v_fail_back,is_auto_suspend = v_is_auto_suspend,
      vm_type = v_vm_type,hypervisor_type = v_hypervisor_type,operation_mode = v_operation_mode, 
      nice_level = v_nice_level,default_boot_sequence = v_default_boot_sequence, 
      default_display_type = v_default_display_type, 
      priority = v_priority,auto_startup = v_auto_startup,is_stateless = v_is_stateless, 
      iso_path = v_iso_path,origin = v_origin,initrd_url = v_initrd_url, 
      kernel_url = v_kernel_url,kernel_params = v_kernel_params, _update_date = CURRENT_TIMESTAMP, quota_id = v_quota_id
      WHERE vm_guid = v_vmt_guid
      AND   entity_type = 'TEMPLATE';
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVmTemplateStatus(
        v_vmt_guid UUID,
        v_status INTEGER)
RETURNS VOID

   AS $procedure$
BEGIN
      UPDATE vm_static
      SET    template_status = v_status
      WHERE  vm_guid = v_vmt_guid
      AND    entity_type = 'TEMPLATE';
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION DeleteVmTemplates(v_vmt_guid UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
        -- Get (and keep) a shared lock with "right to upgrade to exclusive"
		-- in order to force locking parent before children 
      select   vm_guid INTO v_val FROM vm_static  WHERE vm_guid = v_vmt_guid AND entity_type = 'TEMPLATE' FOR UPDATE;
      DELETE FROM vm_static
      WHERE vm_guid = v_vmt_guid
      AND   entity_type = 'TEMPLATE';
		-- delete Template permissions --
      DELETE FROM permissions where object_id = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVmTemplates() RETURNS SETOF vm_templates_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_templates.*
      FROM vm_templates_view vm_templates
      ORDER BY name;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION getAllVmTemplatesRelatedToQuotaId(v_quota_id UUID) RETURNS SETOF vm_templates_view
   AS $procedure$
BEGIN
	RETURN QUERY SELECT vm_templates.*
      FROM vm_templates_view vm_templates
      WHERE quota_id = v_quota_id
      UNION
      SELECT DISTINCT vm_templates.*
      FROM vm_templates_view vm_templates
      INNER JOIN vm_device vd ON vd.vm_id = vm_templates.vmt_guid
      INNER JOIN images ON images.image_group_id = vd.device_id AND images.active = TRUE
      WHERE images.quota_id = v_quota_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmTemplateByVmtGuid(v_vmt_guid UUID) RETURNS SETOF vm_templates_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_templates.*
      FROM vm_templates_view vm_templates
      WHERE vmt_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVmTemplateByVdsGroupId(v_vds_group_id UUID) RETURNS SETOF vm_templates_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_templates.*
      FROM vm_templates_view vm_templates
      WHERE vds_group_id = v_vds_group_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmTemplatesByImageId(v_image_guid UUID) RETURNS SETOF vm_templates_with_plug_info
   AS $procedure$
BEGIN
      RETURN QUERY SELECT * 
      FROM vm_templates_with_plug_info t
      WHERE t.image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmTemplatesByStorageDomainId(v_storage_domain_id UUID) RETURNS SETOF vm_templates_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vm_templates.*
      FROM vm_templates_view vm_templates
      INNER JOIN vm_device vd ON vd.vm_id = vm_templates.vmt_guid
      INNER JOIN images i ON i.image_group_id = vd.device_id AND i.active = TRUE
      where i.image_guid in(select image_id from image_storage_domain_map where storage_domain_id = v_storage_domain_id);
END; $procedure$
LANGUAGE plpgsql;


--This SP returns all templates with permissions to run the given action by user
Create or replace FUNCTION fn_perms_get_templates_with_permitted_action(v_user_id UUID, v_action_group_id integer) RETURNS SETOF vm_templates_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT * 
      FROM vm_templates_view 
      WHERE (SELECT get_entity_permissions(v_user_id, v_action_group_id, vm_templates_view.vmt_guid, 4)) IS NOT NULL;
END; $procedure$
LANGUAGE plpgsql;


