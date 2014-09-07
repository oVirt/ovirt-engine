-- adding a table named object_column_white_list that defined displayed columns per object (table or view)
CREATE OR REPLACE FUNCTION __temp_add_object_column_white_list_table()
RETURNS void
AS $function$
BEGIN
   -- This table holds the column white list per object
   IF EXISTS (SELECT * FROM pg_tables WHERE tablename ILIKE 'object_column_white_list') THEN
       truncate table object_column_white_list;
   ELSE
      CREATE TABLE object_column_white_list
      (
         object_name varchar(128) NOT NULL,
         column_name varchar(128) NOT NULL,
         CONSTRAINT pk_object_column_white_list PRIMARY KEY(object_name,column_name)
      ) WITH OIDS;
   END IF;

   -- this table holds generated sql per object and is truncated upon upgrade to reflect schema changes
   IF EXISTS (SELECT * FROM pg_tables WHERE tablename ILIKE 'object_column_white_list_sql') THEN
       truncate table object_column_white_list_sql;
   ELSE
      CREATE TABLE object_column_white_list_sql
      (
         object_name varchar(128) NOT NULL,
         sql text NOT NULL,
         CONSTRAINT pk_object_column_white_list_sql PRIMARY KEY(object_name)
      ) WITH OIDS;

   END IF;
   -----------------------------------
   -- Initial white list settings  ---
   -----------------------------------
   --            vds view
   -----------------------------------
   --  A new added column will not be displayed for the user unless added specifically.
      insert into object_column_white_list(object_name,column_name)
      (select 'vds', column_name
       from information_schema.columns
       where table_name = 'vds' and
       column_name in (
          'vds_group_id', 'vds_group_name', 'vds_group_description',
          'vds_id', 'vds_name', 'ip', 'vds_unique_id', 'host_name', 'port', 'vds_strength',
          'server_ssl_enabled', 'vds_type', 'pm_type', 'pm_user', 'pm_port', 'pm_options',  'pm_enabled',
          'pm_proxy_preferences', 'pm_secondary_ip', 'pm_secondary_type', 'pm_secondary_user', 'pm_secondary_port',
          'pm_secondary_options', 'pm_secondary_concurrent', 'vds_spm_priority', 'hooks', 'status', 'cpu_cores',
          'cpu_model', 'cpu_speed_mh', 'if_total_speed', 'kvm_enabled', 'physical_mem_mb',
          'pending_vcpus_count', 'pending_vmem_size', 'mem_commited', 'vm_active', 'vm_count',
          'vm_migrating', 'vms_cores_count', 'cpu_over_commit_time_stamp', 'hypervisor_type',
          'net_config_dirty', 'max_vds_memory_over_commit', 'storage_pool_id', 'storage_pool_name', 'reserved_mem',
          'guest_overhead', 'software_version', 'version_name', 'build_name', 'previous_status',
          'cpu_idle', 'cpu_load', 'cpu_sys', 'cpu_user', 'usage_mem_percent', 'usage_cpu_percent',
          'usage_network_percent', 'mem_available', 'mem_shared', 'swap_free', 'swap_total', 'ksm_cpu_percent',
          'ksm_pages', 'ksm_state', 'cpu_flags', 'vds_group_cpu_name', 'cpu_sockets', 'vds_spm_id',
          'otp_validity', 'spm_status', 'supported_cluster_levels', 'supported_engines',
          'vds_group_compatibility_version', 'vds_group_virt_service', 'vds_group_gluster_service', 'host_os', 'kvm_version', 'libvirt_version', 'spice_version', 'kernel_version',
          'iscsi_initiator_name', 'transparent_hugepages_state', 'anonymous_hugepages',
          'non_operational_reason', 'recoverable', 'sshkeyfingerprint', 'count_threads_as_cores', 'cpu_threads',
          'hw_manufacturer', 'hw_product_name', 'hw_version', 'hw_serial_number', 'hw_uuid', 'hw_family', 'ssh_port', 'ssh_username', 'boot_time',
          'pm_detect_kdump', 'protocol'));
-- pm_options are missing
END; $function$
LANGUAGE plpgsql;
SELECT * FROM __temp_add_object_column_white_list_table();
DROP FUNCTION __temp_add_object_column_white_list_table();

-----------------------------------------------------
-- Object white list modification section
-- use fn_db_add_column_to_object_white_list(obj,col)
-- to add new object columns to the white list
-- since this script must remain reentrant
-----------------------------------------------------
select fn_db_add_column_to_object_white_list('vds', 'selinux_enforce_mode');

-- Add new columns for numa feature
select fn_db_add_column_to_object_white_list('vds', 'auto_numa_balancing');
select fn_db_add_column_to_object_white_list('vds', 'is_numa_supported');
select fn_db_add_column_to_object_white_list('vds', 'is_live_snapshot_supported');
select fn_db_add_column_to_object_white_list('vds', 'protocol');

-- allow query of the host supported emulated machines via the user interface (for VM combobox population)
select fn_db_add_column_to_object_white_list('vds', 'supported_emulated_machines');
