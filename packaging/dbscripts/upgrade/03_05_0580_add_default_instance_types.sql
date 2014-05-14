
Create or replace FUNCTION do_insert_instance_type(v_name VARCHAR(255), v_description VARCHAR(4000), v_mem_size integer, v_num_of_sockets integer, v_cpu_per_socket integer) RETURNS VOID
   AS $procedure$
DECLARE
	v_instance_type_id UUID;
BEGIN
v_instance_type_id := uuid_generate_v1();

INSERT INTO vm_static
            (vm_guid,
             vm_name,
             mem_size_mb,
             vmt_guid,
             os,
             description,
             vds_group_id,
             creation_date,
             num_of_monitors,
             is_initialized,
             is_auto_suspend,
             num_of_sockets,
             cpu_per_socket,
             usb_policy,
             time_zone,
             is_stateless,
             fail_back,
             _create_date,
             _update_date,
             dedicated_vm_for_vds,
             auto_startup,
             vm_type,
             nice_level,
             default_boot_sequence,
             default_display_type,
             priority,
             iso_path,
             origin,
             initrd_url,
             kernel_url,
             kernel_params,
             migration_support,
             userdefined_properties,
             predefined_properties,
             min_allocated_mem,
             entity_type,
             child_count,
             template_status,
             quota_id,
             allow_console_reconnect,
             cpu_pinning,
             is_smartcard_enabled)
VALUES      (v_instance_type_id,
             v_name,
             v_mem_size,
             v_instance_type_id,
             0,
             v_description,
             NULL,
             '2014-05-05 00:00:00+03',
             1,
             NULL,
             false,
             v_num_of_sockets,
             v_cpu_per_socket,
             1,
             NULL,
             NULL,
             false,
             '2013-12-25 15:31:54.367179+02',
             '2013-12-25 15:31:53.239308+02',
             NULL,
             NULL,
             0,
             0,
             0,
             1,
             0,
             '',
             0,
             NULL,
             NULL,
             NULL,
             0,
             NULL,
             NULL,
             0,
             'INSTANCE_TYPE',
             0,
             0,
             NULL,
             false,
             NULL,
             false);

INSERT INTO permissions(id,
                         role_id,
                         ad_element_id,
                         object_id,
                         object_type_id)
	     SELECT uuid_generate_v1(),
             'DEF00009-0000-0000-0000-DEF000000009', -- UserTemplateBasedVm
             'EEE00000-0000-0000-0000-123456789EEE', -- Everyone
             v_instance_type_id,
             4 -- template
      ;

RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION insert_default_instance_types() RETURNS VOID
   AS $procedure$

BEGIN

perform do_insert_instance_type('Tiny', 'Tiny instance type', 512, 1, 1);
perform do_insert_instance_type('Small', 'Small instance type', 2048, 1, 1);
perform do_insert_instance_type('Medium', 'Medium instance type', 4096, 1, 1);
perform do_insert_instance_type('Large', 'Large instance type', 8192, 2, 1);
perform do_insert_instance_type('XLarge', 'Extra Large instance type', 16384, 4, 1);

RETURN;
END; $procedure$
LANGUAGE plpgsql;



select insert_default_instance_types();

drop function insert_default_instance_types();
drop function do_insert_instance_type(v_name VARCHAR(255), v_description VARCHAR(4000), v_mem_size integer, v_num_of_sockets integer, v_cpu_per_socket integer);
