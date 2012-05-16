-- adding a table named object_column_white_list that defined displayed columns per object (table or view)
CREATE OR REPLACE FUNCTION __temp_add_object_column_white_list_table()
RETURNS void
AS $function$
BEGIN
   -- This table holds the column white list per object
   IF NOT EXISTS (SELECT * FROM pg_tables WHERE tablename ILIKE 'object_column_white_list') THEN
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
   if not exists (select 1 from object_column_white_list where object_name = 'vds') then
      insert into object_column_white_list(object_name,column_name)
      (select 'vds', column_name
       from information_schema.columns
       where table_name = 'vds' and
       column_name in (
           'anonymous_hugepages', 'build_name', 'cpu_cores', 'cpu_flags', 'cpu_idle', 'cpu_load', 'cpu_model',
           'cpu_over_commit_duration_minutes','cpu_over_commit_time_stamp', 'cpu_sockets', 'cpu_speed_mh',
           'cpu_sys', 'cpu_user', 'guest_overhead', 'high_utilization', 'hooks', 'host_name', 'host_os',
           'hypervisor_type', 'if_total_speed', 'ip', 'iscsi_initiator_name', 'kernel_version',
           'ksm_cpu_percent', 'ksm_pages', 'ksm_state', 'kvm_enabled', 'kvm_version', 'low_utilization',
           'max_vds_memory_over_commit', 'mem_available', 'mem_commited', 'mem_shared', 'net_config_dirty',
           'non_operational_reason', 'otp_validity', 'pending_vcpus_count', 'pending_vmem_size',
           'physical_mem_mb','port', 'previous_status', 'recoverable', 'reserved_mem', 'selection_algorithm',
           'server_ssl_enabled', 'software_version', 'spice_version', 'spm_status', 'status',
           'storage_pool_id','storage_pool_name', 'supported_cluster_levels', 'supported_engines',
           'swap_free','swap_total', 'transparent_hugepages_state','usage_cpu_percent', 'usage_mem_percent',
           'usage_network_percent', 'vds_group_compatibility_version', 'vds_group_cpu_name',
           'vds_group_description', 'vds_group_id', 'vds_group_name', 'vds_id', 'vds_name', 'vds_spm_id',
           'vds_spm_priority', 'vds_strength','vds_type', 'vds_unique_id', 'version_name',
           'vm_active', 'vm_count', 'vm_migrating', 'vms_cores_count'));
   end if;
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


