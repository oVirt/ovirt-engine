----------------------------------------------------------------
-- [vm_ovf_generations] Table

Create or replace FUNCTION UpdateOvfGenerations(v_vms_ids VARCHAR(5000), v_vms_db_generations VARCHAR(5000))
    RETURNS VOID
    AS $procedure$
DECLARE
curs_vmids CURSOR FOR SELECT * FROM fnSplitterUuid(v_vms_ids);
curs_newovfgen CURSOR FOR SELECT * FROM fnSplitter(v_vms_db_generations);
id UUID;
new_ovf_gen BIGINT;
BEGIN
 OPEN curs_vmids;
 OPEN curs_newovfgen;
LOOP
    FETCH curs_vmids INTO id;
    FETCH curs_newovfgen INTO new_ovf_gen;
    IF NOT FOUND THEN
     EXIT;
    END IF;
    UPDATE vm_ovf_generations
    SET ovf_generation = new_ovf_gen WHERE vm_guid = id;
END LOOP;
CLOSE curs_vmids;
CLOSE curs_newovfgen;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetIdsForOvfDeletion(v_storage_pool_id UUID) RETURNS SETOF UUID
   AS $procedure$
BEGIN
RETURN QUERY SELECT ovf.vm_guid as vm_guid
   FROM vm_ovf_generations ovf
   WHERE ovf.storage_pool_id = v_storage_pool_id AND ovf.vm_guid NOT IN (SELECT vm_guid FROM vm_static);
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetOvfGeneration(v_vm_id UUID) RETURNS SETOF BIGINT
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm.ovf_generation
   FROM vm_ovf_generations vm
   WHERE vm.vm_guid = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;








Create or replace FUNCTION GetVmTemplatesIdsForOvfUpdate(v_storage_pool_id UUID) RETURNS SETOF UUID
   AS $procedure$
BEGIN
RETURN QUERY SELECT templates.vmt_guid as vm_guid
   FROM vm_templates_view templates, vm_ovf_generations generations
   WHERE generations.vm_guid = templates.vmt_guid
   AND templates.db_generation > generations.ovf_generation
   AND templates.storage_pool_id = v_storage_pool_id;
END; $procedure$
LANGUAGE plpgsql;








Create or replace FUNCTION GetVmsIdsForOvfUpdate(v_storage_pool_id UUID) RETURNS SETOF UUID
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm.vm_guid as vm_guid
   FROM vms vm, vm_ovf_generations ovf_gen
   WHERE vm.vm_guid = ovf_gen.vm_guid
         AND vm.db_generation >  ovf_gen.ovf_generation
         AND vm.storage_pool_id = v_storage_pool_id;
END; $procedure$
LANGUAGE plpgsql;









Create or replace FUNCTION DeleteOvfGenerations(v_vms_ids VARCHAR(5000))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM vm_ovf_generations WHERE vm_guid IN (SELECT * FROM fnSplitterUuid(v_vms_ids))
    -- needed here to ensure that vm with the same id hasn't been added by import vm/template command
    AND vm_guid NOT IN (SELECT vm_guid FROM vm_static);
END; $procedure$
LANGUAGE plpgsql;







----------------------------------------------------------------
-- [vm_statistics] Table
--





Create or replace FUNCTION InsertVmStatistics(v_cpu_sys DECIMAL(18,0) ,
	v_cpu_user DECIMAL(18,0) ,
	v_elapsed_time DECIMAL(18,0) ,
	v_usage_cpu_percent INTEGER ,
	v_usage_mem_percent INTEGER ,
	v_usage_network_percent INTEGER ,
	v_disks_usage TEXT,
	v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_statistics(cpu_sys, cpu_user, elapsed_time, usage_cpu_percent, usage_mem_percent, usage_network_percent, disks_usage, vm_guid)
	VALUES(v_cpu_sys, v_cpu_user, v_elapsed_time, v_usage_cpu_percent, v_usage_mem_percent, v_usage_network_percent, v_disks_usage, v_vm_guid);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVmStatistics(v_cpu_sys DECIMAL(18,0) ,
	v_cpu_user DECIMAL(18,0) ,
	v_elapsed_time DECIMAL(18,0) ,
	v_usage_cpu_percent INTEGER ,
	v_usage_mem_percent INTEGER ,
	v_usage_network_percent INTEGER ,
	v_disks_usage TEXT ,
	v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_statistics
      SET cpu_sys = v_cpu_sys,cpu_user = v_cpu_user,elapsed_time = v_elapsed_time,
      usage_cpu_percent = v_usage_cpu_percent,usage_mem_percent = v_usage_mem_percent,
      usage_network_percent = v_usage_network_percent,disks_usage = v_disks_usage, _update_date = LOCALTIMESTAMP
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVmStatistics(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM vm_statistics
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVmStatistics() RETURNS SETOF vm_statistics
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_statistics.*
      FROM vm_statistics;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmStatisticsByVmGuid(v_vm_guid UUID) RETURNS SETOF vm_statistics
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_statistics.*
      FROM vm_statistics
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [vm_dynamic] Table
--



Create or replace FUNCTION InsertVmDynamic(v_app_list VARCHAR(4000) ,
	v_guest_cur_user_name VARCHAR(255) ,
	v_console_cur_user_name VARCHAR(255) ,
	v_guest_last_login_time TIMESTAMP WITH TIME ZONE ,
	v_guest_last_logout_time TIMESTAMP WITH TIME ZONE ,
        v_console_user_id UUID,
	v_guest_os VARCHAR(255) ,
	v_migrating_to_vds UUID ,
	v_run_on_vds UUID ,
	v_status INTEGER,
	v_vm_guid UUID,
	v_vm_host VARCHAR(255) ,
	v_vm_ip VARCHAR(255) ,
	v_last_start_time TIMESTAMP WITH TIME ZONE ,
	v_vm_pid INTEGER ,
	v_display INTEGER ,
	v_acpi_enable BOOLEAN ,
	v_session INTEGER ,
	v_display_ip VARCHAR(255) ,
	v_display_type INTEGER ,
	v_kvm_enable BOOLEAN ,
	v_boot_sequence INTEGER ,
	v_display_secure_port INTEGER ,
	v_utc_diff INTEGER ,
	v_last_vds_run_on UUID ,
	v_client_ip VARCHAR(255),
	v_guest_requested_memory  INTEGER ,
	v_hibernation_vol_handle VARCHAR(255) ,
	v_exit_status INTEGER,
	v_pause_status INTEGER,
	v_exit_message VARCHAR(4000),
	v_guest_agent_nics_hash INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_dynamic(app_list, guest_cur_user_name, console_cur_user_name, guest_last_login_time, guest_last_logout_time, console_user_id, guest_os, migrating_to_vds, RUN_ON_VDS, status, vm_guid, vm_host, vm_ip, last_start_time, vm_pid, display, acpi_enable, session, display_ip, display_type, kvm_enable, boot_sequence, display_secure_port, utc_diff, last_vds_run_on, client_ip, guest_requested_memory, hibernation_vol_handle,exit_status,pause_status,exit_message, guest_agent_nics_hash)
	VALUES(v_app_list, v_guest_cur_user_name, v_console_cur_user_name, v_guest_last_login_time, v_guest_last_logout_time, v_console_user_id, v_guest_os, v_migrating_to_vds, v_run_on_vds, v_status, v_vm_guid, v_vm_host, v_vm_ip, v_last_start_time, v_vm_pid, v_display, v_acpi_enable, v_session, v_display_ip, v_display_type, v_kvm_enable, v_boot_sequence, v_display_secure_port, v_utc_diff, v_last_vds_run_on, v_client_ip, v_guest_requested_memory, v_hibernation_vol_handle, v_exit_status, v_pause_status, v_exit_message, v_guest_agent_nics_hash);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVmDynamic(v_app_list VARCHAR(4000) ,
	v_guest_cur_user_name VARCHAR(255) ,
	v_console_cur_user_name VARCHAR(255) ,
	v_guest_last_login_time TIMESTAMP WITH TIME ZONE ,
	v_guest_last_logout_time TIMESTAMP WITH TIME ZONE ,
        v_console_user_id UUID,
	v_guest_os VARCHAR(255) ,
	v_migrating_to_vds UUID ,
	v_run_on_vds UUID ,
	v_status INTEGER,
	v_vm_guid UUID,
	v_vm_host VARCHAR(255) ,
	v_vm_ip VARCHAR(255) ,
	v_last_start_time TIMESTAMP WITH TIME ZONE ,
	v_vm_pid INTEGER ,
	v_display INTEGER ,
	v_acpi_enable BOOLEAN ,
	v_session INTEGER ,
	v_display_ip VARCHAR(255) ,
	v_display_type INTEGER ,
	v_kvm_enable BOOLEAN ,
	v_boot_sequence INTEGER ,
	v_display_secure_port INTEGER ,
	v_utc_diff INTEGER ,
	v_last_vds_run_on UUID ,
	v_client_ip VARCHAR(255) ,
	v_guest_requested_memory INTEGER ,
	v_hibernation_vol_handle VARCHAR(255) ,
	v_exit_status INTEGER,
	v_pause_status INTEGER,
	v_exit_message VARCHAR(4000),
        v_hash VARCHAR(30),
    v_guest_agent_nics_hash INTEGER)
RETURNS VOID

	--The [vm_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_dynamic
      SET app_list = v_app_list,guest_cur_user_name = v_guest_cur_user_name,
      console_cur_user_name = v_console_cur_user_name,
      guest_last_login_time = v_guest_last_login_time,
      guest_last_logout_time = v_guest_last_logout_time,
      console_user_id = v_console_user_id,
      guest_os = v_guest_os,migrating_to_vds = v_migrating_to_vds,RUN_ON_VDS = v_run_on_vds,
      status = v_status,vm_host = v_vm_host,vm_ip = v_vm_ip,
      last_start_time = v_last_start_time,
      vm_pid = v_vm_pid,display = v_display,acpi_enable = v_acpi_enable,
      session = v_session,display_ip = v_display_ip,
      display_type = v_display_type,kvm_enable = v_kvm_enable,boot_sequence = v_boot_sequence,
      display_secure_port = v_display_secure_port,
      utc_diff = v_utc_diff,last_vds_run_on = v_last_vds_run_on,client_ip = v_client_ip,
      guest_requested_memory = v_guest_requested_memory,
      hibernation_vol_handle = v_hibernation_vol_handle,exit_status = v_exit_status,
      pause_status = v_pause_status,exit_message = v_exit_message, hash=v_hash, guest_agent_nics_hash = v_guest_agent_nics_hash
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateConsoleUserWithOptimisticLocking(
    v_vm_guid UUID,
    v_console_user_id UUID,
    v_guest_cur_user_name VARCHAR(255),
    v_console_cur_user_name VARCHAR(255),
    OUT v_updated BOOLEAN)
    AS $procedure$
BEGIN
    UPDATE vm_dynamic SET
        console_user_id = v_console_user_id,
        guest_cur_user_name = v_guest_cur_user_name,
        console_cur_user_name = v_console_cur_user_name
    WHERE
        vm_guid = v_vm_guid AND
        (console_user_id = v_console_user_id OR console_user_id IS NULL);
    v_updated := FOUND;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION UpdateVmDynamicStatus(
	v_vm_guid UUID,
	v_status INTEGER)
RETURNS VOID

   AS $procedure$
BEGIN
      UPDATE vm_dynamic
      SET
      status = v_status
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION DeleteVmDynamic(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM vm_dynamic
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVmDynamic() RETURNS SETOF vm_dynamic
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_dynamic.*
      FROM vm_dynamic;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmDynamicByVmGuid(v_vm_guid UUID) RETURNS SETOF vm_dynamic
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_dynamic.*
      FROM vm_dynamic
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [vm_static] Table
--


Create or replace FUNCTION InsertVmStatic(v_description VARCHAR(4000) ,
 v_mem_size_mb INTEGER,
 v_os INTEGER,
 v_vds_group_id UUID,
 v_vm_guid UUID,
 v_vm_name VARCHAR(255),
 v_vmt_guid UUID,
 v_domain  VARCHAR(40),
 v_creation_date TIMESTAMP WITH TIME ZONE,
 v_num_of_monitors INTEGER,
 v_allow_console_reconnect BOOLEAN,
 v_is_initialized BOOLEAN,
    v_is_auto_suspend BOOLEAN,
    v_num_of_sockets INTEGER,
    v_cpu_per_socket INTEGER,
 v_usb_policy INTEGER,
 v_time_zone VARCHAR(40) ,
 v_auto_startup BOOLEAN,
 v_is_stateless BOOLEAN,
 v_is_smartcard_enabled BOOLEAN,
 v_is_delete_protected BOOLEAN,
 v_dedicated_vm_for_vds UUID ,
    v_fail_back BOOLEAN ,
    v_vm_type INTEGER ,
 v_nice_level INTEGER,
    v_default_boot_sequence INTEGER,
 v_default_display_type INTEGER,
 v_priority INTEGER,
    v_iso_path VARCHAR(4000) ,
    v_origin INTEGER ,
    v_initrd_url    VARCHAR(4000) ,
    v_kernel_url    VARCHAR(4000) ,
    v_kernel_params VARCHAR(4000) ,
    v_migration_support INTEGER ,
    v_predefined_properties VARCHAR(4000) ,
    v_userdefined_properties VARCHAR(4000),
    v_min_allocated_mem INTEGER,
    v_quota_id UUID,
    v_cpu_pinning VARCHAR(4000),
    v_host_cpu_flags BOOLEAN,
    v_tunnel_migration BOOLEAN,
    v_vnc_keyboard_layout VARCHAR(16))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_static(description, mem_size_mb, os, vds_group_id, vm_guid, VM_NAME, vmt_guid,domain,creation_date,num_of_monitors,allow_console_reconnect,is_initialized,is_auto_suspend,num_of_sockets,cpu_per_socket,usb_policy, time_zone,auto_startup,is_stateless,dedicated_vm_for_vds, fail_back, default_boot_sequence, vm_type, nice_level, default_display_type, priority,iso_path,origin,initrd_url,kernel_url,kernel_params,migration_support,predefined_properties,userdefined_properties,min_allocated_mem, entity_type, quota_id, cpu_pinning, is_smartcard_enabled,is_delete_protected,host_cpu_flags, tunnel_migration, vnc_keyboard_layout)
	VALUES(v_description,  v_mem_size_mb, v_os, v_vds_group_id, v_vm_guid, v_vm_name, v_vmt_guid, v_domain, v_creation_date, v_num_of_monitors, v_allow_console_reconnect, v_is_initialized, v_is_auto_suspend, v_num_of_sockets, v_cpu_per_socket, v_usb_policy, v_time_zone, v_auto_startup,v_is_stateless,v_dedicated_vm_for_vds,v_fail_back, v_default_boot_sequence, v_vm_type, v_nice_level, v_default_display_type, v_priority,v_iso_path,v_origin,v_initrd_url,v_kernel_url,v_kernel_params,v_migration_support,v_predefined_properties,v_userdefined_properties,v_min_allocated_mem, 'VM', v_quota_id, v_cpu_pinning, v_is_smartcard_enabled,v_is_delete_protected,v_host_cpu_flags, v_tunnel_migration, v_vnc_keyboard_layout);
-- perform deletion from vm_ovf_generations to ensure that no record exists when performing insert to avoid PK violation.
DELETE FROM vm_ovf_generations gen WHERE gen.vm_guid = v_vm_guid;
INSERT INTO vm_ovf_generations(vm_guid, storage_pool_id) VALUES (v_vm_guid, (SELECT storage_pool_id FROM vds_groups vg WHERE vg.vds_group_id = v_vds_group_id));
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION IncrementDbGeneration(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET db_generation  = db_generation + 1
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetDbGeneration(v_vm_guid UUID)
RETURNS SETOF BIGINT
   AS $procedure$
BEGIN
      RETURN QUERY SELECT db_generation
      FROM vm_static
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION IncrementDbGenerationForAllInStoragePool(v_storage_pool_id UUID)
RETURNS VOID
   AS $procedure$
DECLARE
     curs CURSOR FOR SELECT vms.vm_guid FROM vm_static vms
                     WHERE vms.vds_group_id IN (SELECT vgs.vds_group_id FROM vds_groups vgs
                                                WHERE vgs.storage_pool_id=v_storage_pool_id)
                     ORDER BY vm_guid;
     id UUID;
BEGIN
      OPEN curs;
      LOOP
         FETCH curs INTO id;
         IF NOT FOUND THEN
            EXIT;
         END IF;
         UPDATE vm_static SET db_generation  = db_generation + 1 WHERE vm_guid = id;
      END LOOP;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION UpdateVmStatic(v_description VARCHAR(4000) ,
 v_mem_size_mb INTEGER,
 v_os INTEGER,
 v_vds_group_id UUID,
 v_vm_guid UUID,
 v_vm_name VARCHAR(255),
 v_vmt_guid UUID,
 v_domain  VARCHAR(40),
 v_creation_date TIMESTAMP WITH TIME ZONE,
 v_num_of_monitors INTEGER,
 v_allow_console_reconnect BOOLEAN,
 v_is_initialized BOOLEAN,
 v_is_auto_suspend BOOLEAN,
    v_num_of_sockets INTEGER,
    v_cpu_per_socket INTEGER,
 v_usb_policy  INTEGER,
 v_time_zone VARCHAR(40) ,
 v_auto_startup BOOLEAN,
 v_is_stateless BOOLEAN,
 v_is_smartcard_enabled BOOLEAN,
 v_is_delete_protected BOOLEAN,
 v_dedicated_vm_for_vds UUID ,
    v_fail_back BOOLEAN ,
    v_vm_type INTEGER ,
    v_nice_level INTEGER,
    v_default_boot_sequence INTEGER,
 v_default_display_type INTEGER,
 v_priority INTEGER,
    v_iso_path VARCHAR(4000) ,
    v_origin INTEGER ,
    v_initrd_url    VARCHAR(4000) ,
    v_kernel_url    VARCHAR(4000) ,
    v_kernel_params VARCHAR(4000) ,
    v_migration_support INTEGER ,
v_predefined_properties VARCHAR(4000),
v_userdefined_properties VARCHAR(4000),
v_min_allocated_mem INTEGER,
v_quota_id UUID,
v_cpu_pinning VARCHAR(4000),
v_host_cpu_flags BOOLEAN,
v_tunnel_migration BOOLEAN,
v_vnc_keyboard_layout	VARCHAR(16))
RETURNS VOID

	--The [vm_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET description = v_description,mem_size_mb = v_mem_size_mb,os = v_os,vds_group_id = v_vds_group_id,
      VM_NAME = v_vm_name,vmt_guid = v_vmt_guid,
      domain = v_domain,creation_date = v_creation_date,num_of_monitors = v_num_of_monitors,
      allow_console_reconnect = v_allow_console_reconnect,
      is_initialized = v_is_initialized,is_auto_suspend = v_is_auto_suspend,
      num_of_sockets = v_num_of_sockets,cpu_per_socket = v_cpu_per_socket,
      usb_policy = v_usb_policy,time_zone = v_time_zone,auto_startup = v_auto_startup,
      is_stateless = v_is_stateless,dedicated_vm_for_vds = v_dedicated_vm_for_vds,
      fail_back = v_fail_back,vm_type = v_vm_type,
      _update_date = LOCALTIMESTAMP,default_boot_sequence = v_default_boot_sequence,
      default_display_type = v_default_display_type,
      priority = v_priority,iso_path = v_iso_path,origin = v_origin,
      initrd_url = v_initrd_url,kernel_url = v_kernel_url,
      kernel_params = v_kernel_params,migration_support = v_migration_support,
      predefined_properties = v_predefined_properties,userdefined_properties = v_userdefined_properties,
      min_allocated_mem = v_min_allocated_mem, quota_id = v_quota_id, cpu_pinning = v_cpu_pinning, is_smartcard_enabled = v_is_smartcard_enabled,
      is_delete_protected = v_is_delete_protected, host_cpu_flags = v_host_cpu_flags, tunnel_migration = v_tunnel_migration,
      vnc_keyboard_layout = v_vnc_keyboard_layout
      WHERE vm_guid = v_vm_guid
      AND   entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVmStatic(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
			-- Get (and keep) a shared lock with "right to upgrade to exclusive"
            -- in order to force locking parent before children
      select   vm_guid INTO v_val FROM vm_static  WHERE vm_guid = v_vm_guid     FOR UPDATE;
      DELETE FROM vm_static
      WHERE vm_guid = v_vm_guid
      AND   entity_type = 'VM';

			-- delete VM permissions --
      DELETE FROM permissions where object_id = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVmStatic() RETURNS SETOF vm_static
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static.*
   FROM vm_static
   WHERE entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmStaticByVmGuid(v_vm_guid UUID) RETURNS SETOF vm_static
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static.*
   FROM vm_static
   WHERE vm_guid = v_vm_guid
   AND   entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;




DROP TYPE IF EXISTS GetNamesOfVmStaticDedicatedToVds_rs CASCADE;
CREATE TYPE GetNamesOfVmStaticDedicatedToVds_rs AS (vm_name CHARACTER VARYING);
Create or replace FUNCTION GetNamesOfVmStaticDedicatedToVds(v_vds_id UUID) RETURNS SETOF GetNamesOfVmStaticDedicatedToVds_rs
   AS $procedure$
BEGIN
   RETURN QUERY
      SELECT vm_name
      FROM vm_static
      WHERE dedicated_vm_for_vds = v_vds_id
      AND   migration_support = 2
      AND   entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetAllFromVmStaticByStoragePoolId(v_sp_id uuid) RETURNS SETOF vm_static
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static.*
   FROM vm_static INNER JOIN
        vm_dynamic ON vm_static.vm_guid = vm_dynamic.vm_guid INNER JOIN
        vds_groups ON vm_static.vds_group_id = vds_groups.vds_group_id LEFT OUTER JOIN
        storage_pool ON vm_static.vds_group_id = vds_groups.vds_group_id
        and vds_groups.storage_pool_id = storage_pool.id
   WHERE v_sp_id = storage_pool.id
   AND   entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmStaticByName(v_vm_name VARCHAR(255)) RETURNS SETOF vm_static
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static.*
   FROM vm_static
   WHERE VM_NAME = v_vm_name
   AND   entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmStaticByVdsGroup(v_vds_group_id UUID) RETURNS SETOF vm_static
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static.*
   FROM vm_static
   WHERE vds_group_id = v_vds_group_id
   AND   entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmStaticWithFailbackByVdsId(v_vds_id UUID) RETURNS SETOF vm_static
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_static.*
   FROM vm_static
   WHERE dedicated_vm_for_vds = v_vds_id and fail_back = TRUE
   AND   entity_type = 'VM';

END; $procedure$
LANGUAGE plpgsql;


-----------------------------------------------------------------------------------------
---   [vms] - view
-----------------------------------------------------------------------------------------





Create or replace FUNCTION GetAllFromVms(v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM user_vm_permissions_view
                                       WHERE user_id = v_user_id AND entity_id = vm_guid))
   ORDER BY vm_guid;
END; $procedure$
LANGUAGE plpgsql;








Create or replace FUNCTION GetVmsByIds(v_vms_ids VARCHAR(5000)) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm.*
             FROM vms vm
             WHERE vm.vm_guid IN (SELECT * from fnSplitterUuid(v_vms_ids));
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmByVmGuid(v_vm_guid UUID, v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE vm_guid = v_vm_guid
   AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_vm_permissions_view
                                       WHERE  user_id = v_user_id AND entity_id = v_vm_guid));
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVmByVmNameForDataCenter(v_data_center_id UUID, v_vm_name VARCHAR(255), v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE vm_name = v_vm_name
   AND   (v_data_center_id is null OR storage_pool_id = v_data_center_id)
   AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_vm_permissions_view
                                       WHERE  user_id = v_user_id AND entity_id = vms.vm_guid));
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVmsByVmtGuid(v_vmt_guid UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE vmt_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmsByUserId(v_user_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY select vms.* from vms
   inner join permissions on vms.vm_guid = permissions.object_id
   WHERE permissions.ad_element_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetVmsByUserIdWithGroupsAndUserRoles(v_user_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   from vms
   inner join permissions_view as perms on vms.vm_guid = perms.object_id
   WHERE (perms.ad_element_id = v_user_id
   or perms.ad_element_id in(select id from getUserAndGroupsById(v_user_id)))
   and perms.role_type = 2;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmsRunningOnVds(v_vds_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY SELECT DISTINCT vms.*
   FROM vms
   WHERE run_on_vds = v_vds_id;

END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVmsRunningOnOrMigratingToVds(v_vds_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
    -- use migrating_to_vds column when the VM is in status Migrating From
    RETURN QUERY SELECT DISTINCT V.* FROM VMS V
    WHERE V.RUN_ON_VDS=V_VDS_ID OR (V.STATUS = 5 AND V.MIGRATING_TO_VDS=V_VDS_ID)
    ORDER BY V.VM_NAME;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVmsDynamicRunningOnVds(v_vds_id UUID) RETURNS SETOF vm_dynamic
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_dynamic.*
      FROM vm_dynamic
      WHERE RUN_ON_VDS = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION InsertVm(v_description VARCHAR(4000) ,
 v_mem_size_mb INTEGER,
 v_os INTEGER,
 v_vds_group_id UUID,
 v_vm_guid UUID,
 v_vm_name VARCHAR(255),
 v_vmt_guid UUID,
 v_num_of_monitors INTEGER,
 v_allow_console_reconnect BOOLEAN,
 v_is_initialized   BOOLEAN,
 v_is_auto_suspend   BOOLEAN,
    v_num_of_sockets INTEGER,
    v_cpu_per_socket INTEGER,
 v_usb_policy INTEGER,
 v_time_zone VARCHAR(40) ,
 v_auto_startup BOOLEAN,
 v_is_stateless BOOLEAN,
 v_is_smartcard_enabled BOOLEAN,
 v_is_delete_protected BOOLEAN,
 v_dedicated_vm_for_vds UUID ,
    v_fail_back BOOLEAN ,
    v_vm_type INTEGER ,
    v_nice_level INTEGER,
    v_default_boot_sequence INTEGER,
 v_default_display_type INTEGER,
 v_priority INTEGER,
    v_iso_path VARCHAR(4000) ,
    v_origin INTEGER ,
    v_initrd_url VARCHAR(4000) ,
    v_kernel_url VARCHAR(4000) ,
    v_kernel_params VARCHAR(4000) ,
    v_migration_support INTEGER ,
    v_predefined_properties VARCHAR(4000) ,
    v_userdefined_properties VARCHAR(4000),
    v_min_allocated_mem INTEGER,
    v_cpu_pinning varchar(4000),
    v_host_cpu_flags BOOLEAN,
    v_tunnel_migration BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_static(description, mem_size_mb, os, vds_group_id, vm_guid, VM_NAME, vmt_guid, num_of_monitors, allow_console_reconnect, is_initialized, is_auto_suspend, num_of_sockets, cpu_per_socket, usb_policy, time_zone,auto_startup,is_stateless,dedicated_vm_for_vds,fail_back,vm_type,nice_level,default_boot_sequence,default_display_type,priority,iso_path,origin,initrd_url,kernel_url,kernel_params,migration_support,predefined_properties,userdefined_properties,min_allocated_mem,cpu_pinning,is_smartcard_enabled,is_delete_protected,host_cpu_flags, tunnel_migration)
	VALUES(v_description, v_mem_size_mb, v_os, v_vds_group_id, v_vm_guid, v_vm_name, v_vmt_guid, v_num_of_monitors, v_num_of_monitors, v_is_initialized, v_is_auto_suspend, v_num_of_sockets, v_cpu_per_socket, v_usb_policy, v_time_zone,v_auto_startup,v_is_stateless,v_dedicated_vm_for_vds,v_fail_back,v_vm_type,v_nice_level,v_default_boot_sequence,v_default_display_type,v_priority,v_iso_path,v_origin,v_initrd_url,v_kernel_url,v_kernel_params,v_migration_support,v_predefined_properties,v_userdefined_properties,v_min_allocated_mem,v_cpu_pinning,v_is_smartcard_enabled,v_is_delete_protected,v_host_cpu_flags, v_tunnel_migration);

      INSERT INTO vm_dynamic(vm_guid, status) VALUES(v_vm_guid, 0);

      INSERT INTO vm_statistics(vm_guid) VALUES(v_vm_guid);

      INSERT INTO vm_ovf_generations(vm_guid, storage_pool_id) VALUES (v_vm_guid, (select storage_pool_id from vds_groups vg where vg.vds_group_id = v_vds_group_id));

      UPDATE vm_static
      SET child_count =(SELECT COUNT(*) FROM vm_static WHERE vmt_guid = v_vmt_guid)
      WHERE vm_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVm(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_vmt_guid  UUID;
BEGIN
      select   vm_static.vmt_guid INTO v_vmt_guid FROM vm_static WHERE vm_guid = v_vm_guid;
      UPDATE vm_static
      SET child_count =(SELECT COUNT(*) FROM vm_static WHERE vmt_guid = v_vmt_guid) -1
      WHERE vm_guid = v_vmt_guid;
      DELETE FROM tags_vm_map
      WHERE vm_id = v_vm_guid;
      DELETE
      FROM   snapshots
      WHERE  vm_id = v_vm_guid;
      DELETE FROM vm_statistics WHERE vm_guid = v_vm_guid;
      DELETE FROM vm_dynamic WHERE vm_guid = v_vm_guid;
      DELETE FROM vm_static WHERE vm_guid = v_vm_guid;
      DELETE FROM permissions where object_id = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;










Create or replace FUNCTION GetVmsByAdGroupNames(v_ad_group_names VARCHAR(250)) RETURNS SETOF vms
   AS $procedure$
BEGIN
RETURN QUERY select distinct vms.* from vms
   inner join permissions on vms.vm_guid = permissions.object_id
   inner join ad_groups on ad_groups.id = permissions.ad_element_id
   WHERE     (ad_groups.name in(select Id from fnSplitter(v_ad_group_names)));
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsByDiskId(v_disk_guid UUID) RETURNS SETOF vms_with_plug_info
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms_with_plug_info.*
      FROM vms_with_plug_info
      WHERE device_id = v_disk_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmByHibernationImageId(v_image_id VARCHAR(4000)) RETURNS SETOF vms
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms.*
      FROM vms
      WHERE hibernation_vol_handle like '%' || coalesce(v_image_id,'') || '%';
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetActiveVmsByStorageDomainId(v_storage_domain_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms.*
      FROM vms
      INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
      INNER JOIN images i ON i.image_group_id = vd.device_id AND i.active = TRUE
      inner join image_storage_domain_map on i.image_guid = image_storage_domain_map.image_id
      WHERE status <> 0 and image_storage_domain_map.storage_domain_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmsByStorageDomainId(v_storage_domain_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vms.*
      FROM vms
      INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
      INNER JOIN images ON images.image_group_id = vd.device_id AND images.active = TRUE
      inner join image_storage_domain_map on images.image_guid = image_storage_domain_map.image_id
      where image_storage_domain_map.storage_domain_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION getAllVmsRelatedToQuotaId(v_quota_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vms.*
      FROM vms
      WHERE quota_id = v_quota_id
      UNION
      SELECT DISTINCT vms.*
      FROM vms
      INNER JOIN vm_device vd ON vd.vm_id = vms.vm_guid
      INNER JOIN images ON images.image_group_id = vd.device_id AND images.active = TRUE
      WHERE images.quota_id = v_quota_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION UpdateIsInitialized(v_vm_guid UUID,
 v_is_initialized BOOLEAN)
RETURNS VOID

	--The [vm_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_static
      SET is_initialized = v_is_initialized
      WHERE vm_guid = v_vm_guid
      AND   entity_type = 'VM';
END; $procedure$
LANGUAGE plpgsql;





DROP TYPE IF EXISTS GetOrderedVmGuidsForRunMultipleActions_rs CASCADE;
CREATE TYPE GetOrderedVmGuidsForRunMultipleActions_rs AS (vm_guid UUID);
Create or replace FUNCTION GetOrderedVmGuidsForRunMultipleActions(v_vm_guids VARCHAR(4000)) RETURNS SETOF GetOrderedVmGuidsForRunMultipleActions_rs
   AS $procedure$
   DECLARE
   v_ordered_guids GetOrderedVmGuidsForRunMultipleActions_rs;
BEGIN
   FOR v_ordered_guids IN EXECUTE 'SELECT vm_guid from vm_static where vm_guid in( ' || v_vm_guids || ' ) AND entity_type = ''VM''  order by auto_startup desc,priority desc, migration_support desc' LOOP
      RETURN NEXT v_ordered_guids;
   END LOOP;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsByNetworkId(v_network_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vms
   WHERE EXISTS (
      SELECT 1
      FROM vm_interface
      INNER JOIN network
      ON network.name = vm_interface.network_name
      INNER JOIN network_cluster
      ON network.id = network_cluster.network_id
      WHERE network_id = v_network_id
      AND vms.vds_group_id = network_cluster.cluster_id
      AND vm_interface.vm_guid = vms.vm_guid);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmsByVdsGroupId(v_vds_group_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vms.*
      FROM vms
      WHERE vds_group_id = v_vds_group_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVmsByVmPoolId(v_vm_pool_id UUID) RETURNS SETOF vms
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vms.*
      FROM vms
      WHERE vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;

