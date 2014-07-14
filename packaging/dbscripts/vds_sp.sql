

----------------------------------------------------------------
-- [vds_statistics] Table
--





Create or replace FUNCTION InsertVdsStatistics(v_cpu_idle DECIMAL(18,0) ,
 v_cpu_load DECIMAL(18,0) ,
 v_cpu_sys DECIMAL(18,0) ,
 v_cpu_user DECIMAL(18,0) ,
 v_usage_cpu_percent INTEGER ,
 v_usage_mem_percent INTEGER ,
 v_usage_network_percent INTEGER ,
 v_vds_id UUID,
 v_mem_available BIGINT ,
 v_mem_free BIGINT,
 v_mem_shared BIGINT ,
    v_swap_free BIGINT ,
 v_swap_total BIGINT ,
 v_ksm_cpu_percent INTEGER ,
 v_ksm_pages BIGINT ,
 v_ksm_state BOOLEAN,
 v_anonymous_hugepages INTEGER,
 v_boot_time BIGINT,
 v_ha_score INTEGER,
 v_ha_configured BOOLEAN,
 v_ha_active BOOLEAN,
 v_ha_global_maintenance BOOLEAN,
 v_ha_local_maintenance BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
INSERT INTO vds_statistics(cpu_idle, cpu_load, cpu_sys, cpu_user, usage_cpu_percent, usage_mem_percent, usage_network_percent, vds_id, mem_available, mem_free, mem_shared,swap_free,swap_total,ksm_cpu_percent,ksm_pages,ksm_state, anonymous_hugepages, boot_time, ha_score, ha_configured, ha_active, ha_global_maintenance, ha_local_maintenance)
	VALUES(v_cpu_idle, v_cpu_load, v_cpu_sys, v_cpu_user, v_usage_cpu_percent, v_usage_mem_percent, v_usage_network_percent, v_vds_id, v_mem_available, v_mem_free, v_mem_shared,v_swap_free,v_swap_total,v_ksm_cpu_percent,v_ksm_pages,v_ksm_state, v_anonymous_hugepages, v_boot_time, v_ha_score, v_ha_configured, v_ha_active, v_ha_global_maintenance, v_ha_local_maintenance);
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVdsStatistics(v_cpu_idle DECIMAL(18,0) ,
 v_cpu_load DECIMAL(18,0) ,
 v_cpu_sys DECIMAL(18,0) ,
 v_cpu_user DECIMAL(18,0) ,
 v_usage_cpu_percent INTEGER ,
 v_usage_mem_percent INTEGER ,
 v_usage_network_percent INTEGER ,
 v_vds_id UUID,
 v_mem_available BIGINT ,
 v_mem_free BIGINT,
 v_mem_shared BIGINT ,
 v_swap_free BIGINT ,
 v_swap_total BIGINT ,
 v_ksm_cpu_percent INTEGER ,
 v_ksm_pages BIGINT ,
 v_ksm_state BOOLEAN,
 v_anonymous_hugepages INTEGER,
 v_boot_time BIGINT,
 v_ha_score INTEGER,
 v_ha_configured BOOLEAN,
 v_ha_active BOOLEAN,
 v_ha_global_maintenance BOOLEAN,
 v_ha_local_maintenance BOOLEAN)
RETURNS VOID

	--The [vds_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN

   BEGIN
      UPDATE vds_statistics
      SET cpu_idle = v_cpu_idle,cpu_load = v_cpu_load,cpu_sys = v_cpu_sys,
      cpu_user = v_cpu_user,usage_cpu_percent = v_usage_cpu_percent,usage_mem_percent = v_usage_mem_percent,
      usage_network_percent = v_usage_network_percent,
      mem_available = v_mem_available, mem_free = v_mem_free, mem_shared = v_mem_shared,
      swap_free = v_swap_free,swap_total = v_swap_total,ksm_cpu_percent = v_ksm_cpu_percent,
      ksm_pages = v_ksm_pages,ksm_state = v_ksm_state, anonymous_hugepages = v_anonymous_hugepages,
      boot_time = v_boot_time,
      ha_score = v_ha_score, ha_configured = v_ha_configured, ha_active = v_ha_active,
      ha_global_maintenance = v_ha_global_maintenance, ha_local_maintenance = v_ha_local_maintenance,
      _update_date = LOCALTIMESTAMP
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVdsStatistics(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM vds_statistics
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVdsStatistics() RETURNS SETOF vds_statistics STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_statistics.*
      FROM vds_statistics;
   END;


   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdsStatisticsByVdsId(v_vds_id UUID) RETURNS SETOF vds_statistics STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_statistics.*
      FROM vds_statistics
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




----------------------------------------------------------------
-- [vds_dynamic] Table
--


Create or replace FUNCTION InsertVdsDynamic(v_cpu_cores INTEGER ,
 v_cpu_threads INTEGER ,
 v_cpu_model VARCHAR(255) ,
 v_cpu_speed_mh DECIMAL(18,0) ,
 v_if_total_speed VARCHAR(40) ,
 v_kvm_enabled BOOLEAN ,
 v_mem_commited INTEGER ,
 v_physical_mem_mb INTEGER ,
 v_status INTEGER,
 v_vds_id UUID,
 v_vm_active INTEGER ,
 v_vm_count INTEGER ,
 v_vms_cores_count INTEGER ,
 v_vm_migrating INTEGER ,
 v_reserved_mem INTEGER ,
 v_guest_overhead INTEGER ,
 v_rpm_version VARCHAR(255),
 v_software_version VARCHAR(40) ,
 v_version_name VARCHAR(40) ,
 v_build_name VARCHAR(40) ,
 v_previous_status INTEGER ,
 v_cpu_flags VARCHAR(4000) ,
 v_cpu_over_commit_time_stamp TIMESTAMP WITH TIME ZONE ,
 v_pending_vcpus_count INTEGER ,
 v_pending_vmem_size INTEGER ,
 v_cpu_sockets INTEGER ,
 v_net_config_dirty BOOLEAN ,
 v_supported_cluster_levels VARCHAR(40) ,
 v_supported_engines VARCHAR(40) ,
 v_host_os VARCHAR(4000) ,
 v_kvm_version VARCHAR(4000) ,
 v_libvirt_version VARCHAR(4000) ,
 v_spice_version VARCHAR(4000) ,
 v_gluster_version VARCHAR(4000) ,
 v_kernel_version VARCHAR(4000) ,
 v_iscsi_initiator_name VARCHAR(4000) ,
 v_transparent_hugepages_state INTEGER ,
 v_hooks VARCHAR(4000),
 v_hw_manufacturer VARCHAR(255),
 v_hw_product_name VARCHAR(255),
 v_hw_version VARCHAR(255),
 v_hw_serial_number VARCHAR(255),
 v_hw_uuid VARCHAR(255),
 v_hw_family VARCHAR(255),
 v_hbas VARCHAR(255),
 v_supported_emulated_machines VARCHAR(255),
 v_controlled_by_pm_policy BOOLEAN,
 v_kdump_status SMALLINT,
 v_selinux_enforce_mode INTEGER,
 v_auto_numa_balancing SMALLINT,
 v_is_numa_supported BOOLEAN,
 v_supported_rng_sources VARCHAR(255),
 v_is_live_snapshot_supported BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
INSERT INTO vds_dynamic(cpu_cores, cpu_threads, cpu_model, cpu_speed_mh, if_total_speed, kvm_enabled, mem_commited, physical_mem_mb,	status, vds_id, vm_active, vm_count, vm_migrating, reserved_mem, guest_overhead, rpm_version, software_version, version_name, build_name, previous_status, cpu_flags, cpu_over_commit_time_stamp, vms_cores_count, pending_vcpus_count, pending_vmem_size, cpu_sockets,net_config_dirty, supported_cluster_levels, supported_engines, host_os, kvm_version, libvirt_version, spice_version, gluster_version, kernel_version, iscsi_initiator_name, transparent_hugepages_state, hooks, hw_manufacturer, hw_product_name, hw_version, hw_serial_number, hw_uuid, hw_family, hbas, supported_emulated_machines, controlled_by_pm_policy, kdump_status, selinux_enforce_mode, auto_numa_balancing, is_numa_supported, supported_rng_sources, is_live_snapshot_supported)
	VALUES(v_cpu_cores,	v_cpu_threads, v_cpu_model,	v_cpu_speed_mh,	v_if_total_speed, v_kvm_enabled, v_mem_commited, v_physical_mem_mb,	v_status, v_vds_id, v_vm_active, v_vm_count, v_vm_migrating,	v_reserved_mem, v_guest_overhead, v_rpm_version, v_software_version, v_version_name, v_build_name, v_previous_status, v_cpu_flags, v_cpu_over_commit_time_stamp, v_vms_cores_count,v_pending_vcpus_count, v_pending_vmem_size, v_cpu_sockets, v_net_config_dirty, v_supported_cluster_levels, v_supported_engines, v_host_os, v_kvm_version, v_libvirt_version, v_spice_version, v_gluster_version, v_kernel_version, v_iscsi_initiator_name, v_transparent_hugepages_state, v_hooks, v_hw_manufacturer, v_hw_product_name, v_hw_version, v_hw_serial_number, v_hw_uuid, v_hw_family, v_hbas, v_supported_emulated_machines, v_controlled_by_pm_policy, v_kdump_status, v_selinux_enforce_mode, v_auto_numa_balancing, v_is_numa_supported, v_supported_rng_sources, v_is_live_snapshot_supported);
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateVdsDynamicPowerManagementPolicyFlag(
 v_vds_id UUID,
 v_controlled_by_pm_policy BOOLEAN
) RETURNS VOID AS $procedure$
BEGIN
  BEGIN
    UPDATE vds_dynamic
    SET controlled_by_pm_policy = v_controlled_by_pm_policy
    WHERE vds_id = v_vds_id;
  END;
  RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVdsDynamic(v_cpu_cores INTEGER ,
 v_cpu_threads INTEGER ,
 v_cpu_model VARCHAR(255) ,
 v_cpu_speed_mh DECIMAL(18,0) ,
 v_if_total_speed VARCHAR(40) ,
 v_kvm_enabled BOOLEAN ,
 v_mem_commited INTEGER ,
 v_physical_mem_mb INTEGER ,
 v_status INTEGER,
 v_vds_id UUID,
 v_vm_active INTEGER ,
 v_vm_count INTEGER ,
 v_vms_cores_count INTEGER ,
 v_vm_migrating INTEGER ,
 v_reserved_mem INTEGER ,
 v_guest_overhead INTEGER ,
 v_rpm_version VARCHAR(255),
 v_software_version VARCHAR(40) ,
 v_version_name VARCHAR(40) ,
 v_build_name VARCHAR(40) ,
 v_previous_status INTEGER ,
 v_cpu_flags VARCHAR(4000) ,
 v_cpu_over_commit_time_stamp TIMESTAMP WITH TIME ZONE ,
 v_pending_vcpus_count INTEGER ,
 v_pending_vmem_size INTEGER ,
 v_cpu_sockets INTEGER ,
 v_net_config_dirty BOOLEAN ,
 v_supported_cluster_levels VARCHAR(40) ,
 v_supported_engines VARCHAR(40) ,
 v_host_os VARCHAR(4000) ,
 v_kvm_version VARCHAR(4000) ,
 v_libvirt_version VARCHAR(4000) ,
 v_spice_version VARCHAR(4000) ,
 v_gluster_version VARCHAR(4000) ,
 v_kernel_version VARCHAR(4000) ,
 v_iscsi_initiator_name VARCHAR(4000) ,
 v_transparent_hugepages_state INTEGER ,
 v_hooks VARCHAR(4000),
 v_non_operational_reason INTEGER,
 v_hw_manufacturer VARCHAR(255),
 v_hw_product_name VARCHAR(255),
 v_hw_version VARCHAR(255),
 v_hw_serial_number VARCHAR(255),
 v_hw_uuid VARCHAR(255),
 v_hw_family VARCHAR(255),
 v_hbas VARCHAR(255),
 v_supported_emulated_machines VARCHAR(255),
 v_kdump_status SMALLINT,
 v_selinux_enforce_mode INTEGER,
 v_auto_numa_balancing SMALLINT,
 v_is_numa_supported BOOLEAN,
 v_supported_rng_sources VARCHAR(255),
 v_is_live_snapshot_supported BOOLEAN)
RETURNS VOID

	--The [vds_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN

   BEGIN
      UPDATE vds_dynamic
      SET cpu_cores = v_cpu_cores,cpu_threads = v_cpu_threads,
      cpu_model = v_cpu_model,cpu_speed_mh = v_cpu_speed_mh,
      if_total_speed = v_if_total_speed,kvm_enabled = v_kvm_enabled,
      mem_commited = v_mem_commited,physical_mem_mb = v_physical_mem_mb,
      status = v_status,vm_active = v_vm_active,vm_count = v_vm_count,
      vm_migrating = v_vm_migrating,reserved_mem = v_reserved_mem,
      guest_overhead = v_guest_overhead,rpm_version = v_rpm_version, software_version = v_software_version,
      version_name = v_version_name,build_name = v_build_name,previous_status = v_previous_status,
      cpu_flags = v_cpu_flags,cpu_over_commit_time_stamp = v_cpu_over_commit_time_stamp,
      vms_cores_count = v_vms_cores_count,pending_vcpus_count = v_pending_vcpus_count,
      pending_vmem_size = v_pending_vmem_size,
      cpu_sockets = v_cpu_sockets,net_config_dirty = v_net_config_dirty,
      supported_cluster_levels = v_supported_cluster_levels,
      supported_engines = v_supported_engines,host_os = v_host_os,
      kvm_version = v_kvm_version,libvirt_version = v_libvirt_version,spice_version = v_spice_version,
      gluster_version = v_gluster_version,
      kernel_version = v_kernel_version,iscsi_initiator_name = v_iscsi_initiator_name,
      transparent_hugepages_state = v_transparent_hugepages_state,
      hooks = v_hooks,
      _update_date = LOCALTIMESTAMP,non_operational_reason = v_non_operational_reason,
      hw_manufacturer = v_hw_manufacturer, hw_product_name = v_hw_product_name,
      hw_version = v_hw_version, hw_serial_number = v_hw_serial_number,
      hw_uuid = v_hw_uuid, hw_family = v_hw_family, hbas = v_hbas, supported_emulated_machines = v_supported_emulated_machines,
      kdump_status = v_kdump_status, selinux_enforce_mode = v_selinux_enforce_mode,
      auto_numa_balancing = v_auto_numa_balancing,
      is_numa_supported = v_is_numa_supported,
      supported_rng_sources = v_supported_rng_sources,
      is_live_snapshot_supported = v_is_live_snapshot_supported
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVdsDynamic(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM vds_dynamic
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVdsDynamic() RETURNS SETOF vds_dynamic STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_dynamic.*
      FROM vds_dynamic;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdsDynamicByVdsId(v_vds_id UUID) RETURNS SETOF vds_dynamic STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_dynamic.*
      FROM vds_dynamic
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [vds_static] Table
--


Create or replace FUNCTION InsertVdsStatic(
    v_free_text_comment text,
    v_vds_id UUID,
    v_host_name VARCHAR(255),
    v_ip VARCHAR(255) ,
    v_vds_unique_id VARCHAR(128) ,
    v_port INTEGER,
    v_protocol SMALLINT,
    v_vds_group_id UUID,
    v_vds_name VARCHAR(255),
    v_server_SSL_enabled BOOLEAN ,
    v_vds_type INTEGER,
    v_vds_strength INTEGER,
    v_pm_type VARCHAR(20) ,
    v_pm_user VARCHAR(50) ,
    v_pm_password VARCHAR(50) ,
    v_pm_port INTEGER ,
    v_pm_options VARCHAR(4000) ,
    v_pm_enabled BOOLEAN,
    v_pm_proxy_preferences VARCHAR(255),
    v_pm_secondary_ip VARCHAR(255),
    v_pm_secondary_type VARCHAR(20),
    v_pm_secondary_user VARCHAR(50),
    v_pm_secondary_password text,
    v_pm_secondary_port INTEGER,
    v_pm_secondary_options VARCHAR(4000),
    v_pm_secondary_concurrent BOOLEAN,
    v_pm_detect_kdump BOOLEAN,
    v_vds_spm_priority INTEGER,
    v_sshKeyFingerprint VARCHAR(128),
    v_console_address VARCHAR(255),
    v_ssh_port INTEGER,
    v_ssh_username VARCHAR(255),
    v_disable_auto_pm BOOLEAN,
    v_host_provider_id UUID)
RETURNS VOID

   AS $procedure$
BEGIN
   IF v_vds_unique_id IS NULL OR NOT EXISTS(SELECT vds_name FROM vds_static WHERE vds_unique_id = v_vds_unique_id) then
      BEGIN
         INSERT INTO vds_static(vds_id,host_name, free_text_comment, ip, vds_unique_id, port, protocol, vds_group_id, vds_name, server_SSL_enabled,
                               vds_type,vds_strength,pm_type,pm_user,pm_password,pm_port,pm_options,pm_enabled,
                               pm_proxy_preferences, pm_secondary_ip, pm_secondary_type, pm_secondary_user,
                               pm_secondary_password, pm_secondary_port, pm_secondary_options, pm_secondary_concurrent, pm_detect_kdump,
                               vds_spm_priority, sshKeyFingerprint, console_address, ssh_port, ssh_username, disable_auto_pm, host_provider_id)
			VALUES(v_vds_id,v_host_name, v_free_text_comment, v_ip, v_vds_unique_id, v_port, v_protocol, v_vds_group_id, v_vds_name, v_server_SSL_enabled,
                               v_vds_type,v_vds_strength,v_pm_type,v_pm_user,v_pm_password,v_pm_port,v_pm_options,v_pm_enabled,
                               v_pm_proxy_preferences, v_pm_secondary_ip, v_pm_secondary_type, v_pm_secondary_user,
                               v_pm_secondary_password, v_pm_secondary_port, v_pm_secondary_options, v_pm_secondary_concurrent, v_pm_detect_kdump,
                               v_vds_spm_priority, v_sshKeyFingerprint, v_console_address, v_ssh_port, v_ssh_username, v_disable_auto_pm, v_host_provider_id);
      END;
   end if;
   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVdsStatic(v_host_name VARCHAR(255),
    v_free_text_comment text,
	v_ip VARCHAR(255) ,
    v_vds_unique_id VARCHAR(128),
    v_port INTEGER,
    v_protocol SMALLINT,
    v_vds_group_id UUID,
    v_vds_id UUID,
    v_vds_name VARCHAR(255),
    v_server_SSL_enabled BOOLEAN ,
    v_vds_type INTEGER,
    v_vds_strength INTEGER,
    v_pm_type VARCHAR(20) ,
    v_pm_user VARCHAR(50) ,
    v_pm_password VARCHAR(50) ,
    v_pm_port INTEGER ,
    v_pm_options VARCHAR(4000) ,
    v_pm_enabled BOOLEAN,
    v_pm_proxy_preferences VARCHAR(255),
    v_pm_secondary_ip VARCHAR(255),
    v_pm_secondary_type VARCHAR(20),
    v_pm_secondary_user VARCHAR(50),
    v_pm_secondary_password text,
    v_pm_secondary_port INTEGER,
    v_pm_secondary_options VARCHAR(4000),
    v_pm_secondary_concurrent BOOLEAN,
    v_pm_detect_kdump BOOLEAN,
    v_otp_validity BIGINT,
    v_vds_spm_priority INTEGER,
    v_sshKeyFingerprint VARCHAR(128),
    v_console_address VARCHAR(255),
    v_ssh_port INTEGER,
    v_ssh_username VARCHAR(255),
    v_disable_auto_pm BOOLEAN,
    v_host_provider_id UUID)
RETURNS VOID

	--The [vds_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN

   BEGIN
      UPDATE vds_static
      SET host_name = v_host_name, free_text_comment = v_free_text_comment, ip = v_ip,vds_unique_id = v_vds_unique_id,
      port = v_port, protocol = v_protocol, vds_group_id = v_vds_group_id,vds_name = v_vds_name,server_SSL_enabled = v_server_SSL_enabled,
      vds_type = v_vds_type,
      _update_date = LOCALTIMESTAMP,vds_strength = v_vds_strength,
      pm_type = v_pm_type,pm_user = v_pm_user,pm_password = v_pm_password,
      pm_port = v_pm_port,pm_options = v_pm_options,pm_enabled = v_pm_enabled, pm_proxy_preferences = v_pm_proxy_preferences,
      pm_secondary_ip = v_pm_secondary_ip, pm_secondary_type = v_pm_secondary_type,
      pm_secondary_user = v_pm_secondary_user, pm_secondary_password = v_pm_secondary_password,
      pm_secondary_port = v_pm_secondary_port, pm_secondary_options = v_pm_secondary_options,
      pm_secondary_concurrent = v_pm_secondary_concurrent, pm_detect_kdump = v_pm_detect_kdump,
      otp_validity = v_otp_validity, vds_spm_priority = v_vds_spm_priority, sshKeyFingerprint = v_sshKeyFingerprint, host_provider_id = v_host_provider_id,
      console_address = v_console_address, ssh_port = v_ssh_port, ssh_username = v_ssh_username, disable_auto_pm = v_disable_auto_pm
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVdsStatic(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      UPDATE vm_static
      SET dedicated_vm_for_vds = null,
          migration_support = 0
      WHERE dedicated_vm_for_vds = v_vds_id;
      DELETE FROM tags_vds_map
      WHERE vds_id = v_vds_id;
   -- Delete all Vds Alerts from the database
      PERFORM DeleteAuditLogAlertsByVdsID(v_vds_id);
      DELETE FROM vds_static
      WHERE vds_id = v_vds_id;
	-- delete VDS permissions --
      DELETE FROM permissions where object_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllFromVdsStatic() RETURNS SETOF vds_static STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vds_static.*
   FROM vds_static;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdsStaticByVdsId(v_vds_id UUID) RETURNS SETOF vds_static STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_static.*
      FROM vds_static
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVdsStaticByHostName(v_host_name VARCHAR(255)) RETURNS SETOF vds_static STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vds_static.*
   FROM vds_static
   WHERE host_name = v_host_name;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVdsStaticByVdsName(v_host_name VARCHAR(255)) RETURNS SETOF vds_static STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vds_static.*
   FROM vds_static
   WHERE vds_name = v_host_name;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsStaticByIp(v_ip VARCHAR(40)) RETURNS SETOF vds_static STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_static.*
      FROM vds_static
      WHERE ip = v_ip;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdsByUniqueID(v_vds_unique_id VARCHAR(128)) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_unique_id = v_vds_unique_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVdsStaticByVdsGroupId(v_vds_group_id UUID) RETURNS SETOF vds_static STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds_static.*
      FROM vds_static vds_static
      WHERE vds_group_id = v_vds_group_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




---------------------------------------------------------------------------------------------------
--    [vds] - view
---------------------------------------------------------------------------------------------------



CREATE OR REPLACE FUNCTION GetUpAndPrioritizedVds(v_storage_pool_id UUID) RETURNS SETOF vds STABLE
AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT vds.*
      FROM vds vds, vds_groups vdsgroup
      WHERE (vds.status = 3) AND (vds.storage_pool_id = v_storage_pool_id) AND (vds_spm_priority IS NULL OR vds_spm_priority > -1)
      AND vds.vds_group_id = vdsgroup.vds_group_id AND vdsgroup.virt_service = true
      ORDER BY vds_spm_priority DESC, RANDOM();
   END;
   RETURN;
END; $procedure$
  LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromVds(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                          FROM user_vds_permissions_view
                                          WHERE user_id = v_user_id AND entity_id = vds_id));
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVdsByVdsId(v_vds_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds STABLE
   AS $procedure$
DECLARE
v_columns text[];
BEGIN
    BEGIN
      if (v_is_filtered) then
          RETURN QUERY SELECT DISTINCT (rec).*
          FROM fn_db_mask_object('vds') as q (rec vds)
          WHERE (rec).vds_id = v_vds_id
          AND EXISTS (SELECT 1
              FROM   user_vds_permissions_view
              WHERE  user_id = v_user_id AND entity_id = v_vds_id);
      else
          RETURN QUERY SELECT DISTINCT vds.*
          FROM vds
          WHERE vds_id = v_vds_id;
      end if;
    END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVdsWithoutMigratingVmsByVdsGroupId(v_vds_group_id UUID) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN


	-- this sp returns all vds in given cluster that have no pending vms and no vms in migration states
	BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_group_id = v_vds_group_id and
      pending_vcpus_count = 0
      and	vds.status = 3
      and	vds_id not in(select distinct RUN_ON_VDS from vm_dynamic
         where status in(5,6,11,12));
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteVds(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      UPDATE vm_static
      SET dedicated_vm_for_vds = null
      WHERE dedicated_vm_for_vds = v_vds_id;
      DELETE FROM tags_vds_map
      WHERE vds_id = v_vds_id;
   -- Delete all Vds Alerts from the database
      PERFORM DeleteAuditLogAlertsByVdsID(v_vds_id);
      DELETE FROM vds_statistics WHERE vds_id = v_vds_id;
      DELETE FROM vds_dynamic WHERE vds_id = v_vds_id;
      DELETE FROM vds_static WHERE vds_id = v_vds_id;
      DELETE FROM permissions where object_id = v_vds_id;
   END;
   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByType(v_vds_type INTEGER) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_type = v_vds_type;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByName(v_vds_name VARCHAR(255)) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE vds_name = v_vds_name;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByHostName(v_host_name VARCHAR(255)) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE host_name = v_host_name;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsByIp(v_ip VARCHAR(40)) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM vds
      WHERE ip = v_ip;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdsByVdsGroupId(v_vds_group_id UUID, v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
	-- this sp returns all vds for a given cluster
   BEGIN
      if (v_is_filtered) then
          RETURN QUERY SELECT DISTINCT (rec).*
          FROM fn_db_mask_object('vds') as q (rec vds)
          WHERE (rec).vds_group_id = v_vds_group_id
          AND EXISTS (SELECT 1
              FROM   user_vds_permissions_view
              WHERE  user_id = v_user_id AND entity_id = (rec).vds_id);
      else
          RETURN QUERY SELECT DISTINCT vds.*
          FROM vds
          WHERE vds_group_id = v_vds_group_id;
      end if;
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVdsByStoragePoolId(v_storage_pool_id UUID, v_user_id UUID, v_is_filtered boolean) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT DISTINCT vds.*
      FROM   vds
      WHERE  storage_pool_id = v_storage_pool_id
      AND    (NOT v_is_filtered OR EXISTS (SELECT 1
                                           FROM   user_vds_permissions_view
                                           WHERE  user_id = v_user_id AND entity_id = vds_id));
   END;
   RETURN;
END; $procedure$
LANGUAGE plpgsql;


-- Returns all VDS for a given cluster and having given status
CREATE OR REPLACE FUNCTION getVdsForVdsGroupWithStatus(v_vds_group_id UUID, v_status integer) RETURNS SETOF vds STABLE
    AS $procedure$
BEGIN
    BEGIN
        RETURN QUERY SELECT vds.*
        FROM vds
        WHERE (status = v_status) AND (vds_group_id = v_vds_group_id)
        ORDER BY vds.vds_id ASC;
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

-- Returns all VDS for a given pool and having given status
CREATE OR REPLACE FUNCTION getVdsByStoragePoolIdWithStatus(v_storage_pool_id UUID, v_status integer) RETURNS SETOF vds STABLE
    AS $procedure$
BEGIN
    BEGIN
        RETURN QUERY SELECT vds.*
        FROM vds
        INNER JOIN vds_groups vdsgroup ON vds.vds_group_id = vdsgroup.vds_group_id
        WHERE (vds.status = v_status) AND (vds.storage_pool_id = v_storage_pool_id)
        AND vdsgroup.virt_service = true;
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getHostsForStorageOperation(v_storage_pool_id UUID, v_local_fs_only BOOLEAN) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
    BEGIN
        RETURN QUERY SELECT vds.*
        FROM vds
        LEFT JOIN vds_groups vg ON vds.vds_group_id = vg.vds_group_id
        LEFT JOIN storage_pool sp ON vds.storage_pool_id = sp.id
        WHERE (v_storage_pool_id IS NULL OR vds.storage_pool_id = v_storage_pool_id)
        AND (vg.virt_service = true)
        AND (NOT v_local_fs_only OR sp.is_local = true)
        AND (v_storage_pool_id IS NOT NULL OR vds.status = 3); -- if DC is unspecified return only hosts with status = UP
    END;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateVdsDynamicStatus(
        v_vds_guid UUID,
        v_status INTEGER)
RETURNS VOID

   AS $procedure$
BEGIN
      UPDATE vds_dynamic
      SET
      status = v_status
      WHERE vds_id = v_vds_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateVdsDynamicNetConfigDirty(
        v_vds_guid UUID,
        v_net_config_dirty BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vds_dynamic
      SET
      net_config_dirty = v_net_config_dirty
      WHERE vds_id = v_vds_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdatePartialVdsDynamicCalc(
        v_vds_guid UUID,
        v_vmCount INTEGER,
        v_pendingVcpusCount INTEGER,
        v_pendingVmemSize INTEGER,
        v_memCommited INTEGER,
        v_vmsCoresCount INTEGER)
RETURNS VOID
   AS $procedure$
DECLARE
    sign int;
BEGIN
	IF (v_memCommited = 0 ) THEN
	  sign = 0;
	ELSEIF (v_memCommited > 0) THEN
	  sign = 1;
	ELSE
	  sign = -1;
	END IF;

    UPDATE vds_dynamic
    SET
      vm_count = GREATEST(vm_count + v_vmCount, 0),
      pending_vcpus_count = GREATEST(pending_vcpus_count + v_pendingVcpusCount, 0),
      pending_vmem_size = GREATEST(pending_vmem_size + v_pendingVmemSize, 0),
      mem_commited = GREATEST(mem_commited + sign * (abs(v_memCommited) + guest_overhead), 0),
      vms_cores_count = GREATEST(vms_cores_count + v_vmsCoresCount, 0)
    WHERE vds_id = v_vds_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVdsByNetworkId(v_network_id UUID) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM vds
   WHERE EXISTS (
      SELECT 1
      FROM vds_interface
      INNER JOIN network
      ON network.name = vds_interface.network_name
      INNER JOIN network_cluster
      ON network.id = network_cluster.network_id
      WHERE network_id = v_network_id
      AND vds.vds_group_id = network_cluster.cluster_id
      AND vds_interface.vds_id = vds.vds_id);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVdsWithoutNetwork(v_network_id UUID) RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vds.*
   FROM vds
   INNER JOIN network_cluster
   ON vds.vds_group_id = network_cluster.cluster_id
   WHERE network_cluster.network_id = v_network_id
   AND NOT EXISTS (
      SELECT 1
      FROM vds_interface
      INNER JOIN network
      ON network.name = vds_interface.network_name
      INNER JOIN network_cluster
      ON network.id = network_cluster.network_id
      WHERE network_cluster.network_id = v_network_id
      AND vds.vds_group_id = network_cluster.cluster_id
      AND vds_interface.vds_id = vds.vds_id);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateCpuFlags(
  v_vds_id UUID,
  v_cpu_flags VARCHAR(4000))
  RETURNS VOID
AS $procedure$
BEGIN
      UPDATE vds_dynamic
      SET
      cpu_flags = v_cpu_flags
      WHERE vds_id = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [vds_cpu_statistics] Table
--


Create or replace FUNCTION InsertVdsCpuStatistics(v_vds_cpu_id UUID,
 v_vds_id UUID,
 v_cpu_core_id INTEGER,
 v_cpu_sys DECIMAL(18,0),
 v_cpu_user DECIMAL(18,0),
 v_cpu_idle DECIMAL(18,0),
 v_usage_cpu_percent INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
      INSERT INTO vds_cpu_statistics(vds_cpu_id, vds_id, cpu_core_id, cpu_sys, cpu_user, cpu_idle, usage_cpu_percent)
      VALUES(v_vds_cpu_id, v_vds_id, v_cpu_core_id, v_cpu_sys, v_cpu_user, v_cpu_idle, v_usage_cpu_percent);
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVdsCpuStatistics(v_vds_id UUID,
 v_cpu_core_id INTEGER,
 v_cpu_sys DECIMAL(18,0),
 v_cpu_user DECIMAL(18,0),
 v_cpu_idle DECIMAL(18,0),
 v_usage_cpu_percent INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN

   BEGIN
      UPDATE vds_cpu_statistics
      SET cpu_sys = v_cpu_sys, cpu_user = v_cpu_user, cpu_idle = v_cpu_idle,
      usage_cpu_percent = v_usage_cpu_percent
      WHERE vds_id = v_vds_id and cpu_core_id = v_cpu_core_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteVdsCpuStatisticsByVdsId(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      DELETE FROM vds_cpu_statistics
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVdsCpuStatisticsByVdsId(v_vds_id UUID) RETURNS SETOF vds_cpu_statistics STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT vds_cpu_statistics.*
      FROM vds_cpu_statistics
      WHERE vds_id = v_vds_id;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromVdsCpuStatistics() RETURNS SETOF vds_cpu_statistics STABLE
   AS $procedure$
BEGIN
   BEGIN
      RETURN QUERY SELECT vds_cpu_statistics.*
      FROM vds_cpu_statistics;
   END;

   RETURN;
END; $procedure$
LANGUAGE plpgsql;
