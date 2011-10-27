SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO


/**************************************
           VERSIONED VIEWS (2.2)
**************************************/

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_configuration_view_2_2]'))
DROP VIEW [dbo].[vm_configuration_view_2_2]
GO
CREATE VIEW [dbo].[vm_configuration_view_2_2]
AS
SELECT     a.vm_guid AS vm_id, a.vm_name, a.vmt_guid AS template_id, a.os AS operating_system, a.description, a.domain AS ad_domain, 
                      b.vds_group_id___old AS cluster_id, a.is_initialized AS initialized, a.is_auto_suspend AS auto_suspend, a.usb_policy, a.time_zone, 
                      a.is_stateless AS stateless, a.fail_back, c.vds_id___old AS default_host, a.auto_startup, a.priority AS high_availability, a._create_date AS create_date, 
                      a._delete_date AS delete_date
FROM         dbo.vm_configuration AS a INNER JOIN
                      dbo.vds_group_configuration AS b ON a.vds_group_id = b.vds_group_id LEFT OUTER JOIN
                      dbo.vds_configuration AS c ON a.dedicated_vm_for_vds = c.vds_id
GO


IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_history_view_2_2]'))
DROP VIEW [dbo].[vm_history_view_2_2]
GO
CREATE VIEW [dbo].[vm_history_view_2_2]
AS
SELECT     a.id AS history_id, a.aggregation_level, a.history_datetime, a.vm_guid AS vm_id, a.status, a.num_of_monitors AS monitors, a.vm_ip, 
                      b.vds_id___old AS currently_running_on_host, a.guest_cur_user_name AS current_user_name, a.vm_last_up_time, a.vm_last_boot_time, 
                      a.num_of_cpus AS total_vcpus, a.usage_cpu_percent AS cpu_usage_percent, a.mem_size_mb AS memory_size, 
                      a.usage_mem_percent AS memory_usage_percent
FROM         dbo.vm_history AS a LEFT OUTER JOIN
                      dbo.vds_configuration AS b ON a.run_on_vds = b.vds_id
GO


IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[cluster_configuration_view_2_2]'))
DROP VIEW [dbo].[cluster_configuration_view_2_2]
GO
CREATE VIEW [dbo].[cluster_configuration_view_2_2]
AS
	  SELECT
      [vds_group_id___old] as [cluster_id],
      [name] as [cluster_name],
      [description] as [description],
      [cpu_name] as [cpu_name],
	  [storage_pool_id] as [datacenter_id],
	  [compatibility_version] as [compatibility_version],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]
	  FROM vds_group_configuration
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_configuration_view_2_2]'))
DROP VIEW [dbo].[host_configuration_view_2_2]
GO
CREATE VIEW [dbo].[host_configuration_view_2_2]
AS
	  SELECT      
	  a.[vds_id___old] as [host_id],
      a.[vds_name] as [host_name],
      a.[vds_unique_id] as [host_unique_id],
      a.[host_name] as [fqn_or_ip],
      a.[port] as [vdsm_port],
      b.[vds_group_id___old] as [cluster_id],
      a.[vds_type] as [host_type],
      null as [subnet_mask],
      a.[cpu_flags] as [cpu_flags],
	  a.[_create_date] as [create_date],
	  a.[_delete_date] as [delete_date] 
	  FROM vds_configuration a,vds_group_configuration b
	  where a.[vds_group_id] = b.[vds_group_id]
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_history_view_2_2]'))
DROP VIEW [dbo].[host_history_view_2_2]
GO
CREATE VIEW [dbo].[host_history_view_2_2]
AS
	  SELECT
      a.[id] as [history_id],
      a.[aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      a.[history_datetime] as [history_datetime],
      b.[vds_id___old] as [host_id],
      a.[status] as [status],                  
	  CASE substring(a.software_version,1,3) 
			 WHEN   '4.4' THEN '2.1' + substring(a.software_version,4,Len(a.software_version)) 
			 WHEN '4.5' THEN '2.2' + substring(a.software_version,4,Len(a.software_version))
			 WHEN '4.9' THEN '2.3' + substring(a.software_version,4,Len(a.software_version))
			 ELSE a.[software_version]  
      END
		  as [software_version],       
      a.[vm_active] as [active_vms],
      a.[vm_count] as [total_vms],
      a.[ksm_cpu_percent] as [ksm_cpu_percent],
	  a.[vms_cores_count]  as [total_vms_vcpus],
      a.[cpu_cores] as [cpu_cores],
      a.[usage_cpu_percent] as [cpu_usage_percent] ,      
      a.[physical_mem_mb] [physical_memory],
      a.[usage_mem_percent] as [memory_usage_percent]
      FROM vds_history a, vds_configuration b
      where a.[vds_id] = 	b.[vds_id]
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_interface_configuration_view_2_2]'))
DROP VIEW [dbo].[vm_interface_configuration_view_2_2]
GO
CREATE VIEW [dbo].[vm_interface_configuration_view_2_2]
AS
	  SELECT
	  [id] as [vm_interface_id],
	  [vm_guid] as [vm_id],
	  [mac_addr] as [mac_address],
	  [name] as [interface_name],
	  [network_name] as [network_name],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]
	  FROM vm_interface_configuration
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_interface_configuration_view_2_2]'))
DROP VIEW [dbo].[host_interface_configuration_view_2_2]
GO
CREATE VIEW [dbo].[host_interface_configuration_view_2_2]
AS
	  SELECT
	  a.[id] as [host_interface_id],
	  b.[vds_id___old] as [host_id],
	  a.[mac_addr] as [mac_address],
	  a.[name] as [interface_name],
	  a.[network_name] as [network_name],
      a.[is_bond] as [bond],
	  a.[bond_name] as [bond_name], 
	  a.[vlan_id] as [vlan_id], 
	  a.[addr] as [ip_address], 
	  a.[gateway] as [gateway], 
	  a.[type]as [type], 	  
	  a.[_create_date] as [create_date],
	  a.[_delete_date] as [delete_date]
	  FROM vds_interface_configuration a , vds_configuration b
	  where a.[vds_id] = b.[vds_id]
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_interface_history_view_2_2]'))
DROP VIEW [dbo].[vm_interface_history_view_2_2]
GO
CREATE VIEW [dbo].[vm_interface_history_view_2_2]
AS

SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
	  [interface_id] as [interface_id],
      [vm_guid] as [vm_id],
      [rx_rate] as [bytes_received_rate], 
	  [tx_rate] as [bytes_transmitted_rate],
      [speed] as [speed],
	  [type]as [type]
	  FROM vm_interface_history
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_interface_history_view_2_2]'))
DROP VIEW [dbo].[host_interface_history_view_2_2]
GO
CREATE VIEW [dbo].[host_interface_history_view_2_2]
AS

	  SELECT
      a.[id] as [history_id],
      a.[aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      a.[history_datetime] as [history_datetime],
	  a.[interface_id] as [interface_id],
      b.[vds_id___old] as [host_id],
      a.[rx_rate] as [bytes_received_rate], 
	  a.[tx_rate] as [bytes_transmitted_rate],
      a.[speed] as [speed]
      FROM vds_interface_history a,vds_configuration b
	  where a.[vds_id] = b.[vds_id]
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_disk_configuration_view_2_2]'))
DROP VIEW [dbo].[vm_disk_configuration_view_2_2]
GO
CREATE VIEW [dbo].[vm_disk_configuration_view_2_2]
AS
	SELECT
	[image_guid] as [disk_id],
	[description] as [description],
	[volume_format] as [format],
	[volume_type] as [disk_type], 
	[_create_date] as [create_date],
	[_delete_date] as [delete_date]
	FROM vm_disk_configuration
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_disk_history_view_2_2]'))
DROP VIEW [dbo].[vm_disk_history_view_2_2]
GO
CREATE VIEW [dbo].[vm_disk_history_view_2_2]
AS
	  SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
	  [vm_guid] as [vm_id],
	  [image_guid] as [disk_id],
	  [actual_size] as [actual_size],
	  [size] as [size],
	  [disk_interface]as [interface],
	  [disk_type] as [disk_type],
	  [imageStatus] as [status],  
	  [read_rate] as [read_rate],
	  [write_rate]  as [write_rate]
	  FROM vm_disk_history
GO


/**************************************
           VERSIONED VIEWS (2.3)
**************************************/
IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[datacenter_configuration_view_2_3]'))
DROP VIEW [dbo].[datacenter_configuration_view_2_3]
GO
CREATE VIEW [dbo].[datacenter_configuration_view_2_3]
AS
	  SELECT 
      [datacenter_id] as [datacenter_id],
      [name] as [datacenter_name],
      [storage_pool_type] as [storage_type],
      [description] as [datacenter_description],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]
	FROM datacenter_configuration
GO


IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[datacenter_history_view_2_3]'))
DROP VIEW [dbo].[datacenter_history_view_2_3]
GO
CREATE VIEW [dbo].[datacenter_history_view_2_3]
AS
	  SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
	  [datacenter_id] as [datacenter_id],
      [status] as [datacenter_status],
	  [time_in_status] as [time_in_status]
	  FROM datacenter_history
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[storage_domain_configuration_view_2_3]'))
DROP VIEW [dbo].[storage_domain_configuration_view_2_3]
GO
CREATE VIEW [dbo].[storage_domain_configuration_view_2_3]
AS
	  SELECT 
      [storage_domain_id] as [storage_domain_id],
      [storage_name] as [storage_domain_name],
      [storage_domain_type] as [storage_domain_type],
      [storage_type] as [storage_type],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]
	FROM storage_domain_configuration
GO


IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[storage_domain_history_view_2_3]'))
DROP VIEW [dbo].[storage_domain_history_view_2_3]
GO
CREATE VIEW [dbo].[storage_domain_history_view_2_3]
AS
	  SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
	  [storage_domain_id] as [storage_domain_id],
      [available_disk_size] as [available_disk_size],
      [used_disk_size] as [used_disk_size]
	  FROM storage_domain_history
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_configuration_view_2_3]'))
DROP VIEW [dbo].[vm_configuration_view_2_3]
GO
CREATE VIEW [dbo].[vm_configuration_view_2_3]
AS
	  SELECT 
      [vm_guid] as [vm_id],
      [vm_name] as [vm_name],
      [vmt_guid] as [template_id],
      [os] as [operating_system],
      [description] as [description],
      [domain] as [ad_domain],
      [vds_group_id] as [cluster_id],                  -- ***
      [is_initialized] as [initialized],
      [is_auto_suspend] as [auto_suspend],
      [usb_policy] as [usb_policy],
      [time_zone] as [time_zone],
      [is_stateless] as [stateless],
      [fail_back] as [fail_back],
      [dedicated_vm_for_vds] as [default_host],
      [auto_startup] as [auto_startup],
      [priority] as [high_availability],
	  [template_name] as [template_name],
	  [mem_size_mb] [memory_size],
	  [cpu_per_socket] as [cpu_per_socket],
	  [num_of_sockets] as [number_of_sockets],
	  [vm_type] [vm_type],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]
	FROM vm_configuration
GO


IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_history_view_2_3]'))
DROP VIEW [dbo].[vm_history_view_2_3]
GO
CREATE VIEW [dbo].[vm_history_view_2_3]
AS
	  SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
      [vm_guid] as [vm_id],
      [status] as [status],
      [num_of_monitors] as [monitors] ,
	  [vm_ip] as [vm_ip],
	  [run_on_vds] as [currently_running_on_host] ,
	  [guest_cur_user_name] as [current_user_name],
      [vm_last_up_time] as [vm_last_up_time] ,
      [vm_last_boot_time] as  [vm_last_boot_time] ,
	  [num_of_cpus] as [total_vcpus],     
      [usage_cpu_percent] as [cpu_usage_percent],      
      [mem_size_mb] as [memory_size] ,
      [usage_mem_percent] as [memory_usage_percent],
	  [time_in_status] as [time_in_status],
	  [max_usage_mem] as [max_memory_usage],
      [max_usage_cpu] as [max_cpu_usage],
      [vds_group_id] AS cluster_id
	  FROM vm_history
GO


IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[cluster_configuration_view_2_3]'))
DROP VIEW [dbo].[cluster_configuration_view_2_3]
GO
CREATE VIEW [dbo].[cluster_configuration_view_2_3]
AS
	  SELECT
      [vds_group_id] as [cluster_id],
      [name] as [cluster_name],
      [description] as [description],
      [cpu_name] as [cpu_name],
	  [storage_pool_id] as [datacenter_id],
	  [compatibility_version] as [compatibility_version],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]
	  FROM vds_group_configuration
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_configuration_view_2_3]'))
DROP VIEW [dbo].[host_configuration_view_2_3]
GO
CREATE VIEW [dbo].[host_configuration_view_2_3]
AS
	  SELECT      
	  [vds_id] as [host_id],
      [vds_name] as [host_name],
      [vds_unique_id] as [host_unique_id],
      [host_name] as [fqn_or_ip],
      [port] as [vdsm_port],
      [vds_group_id] as [cluster_id],
      [vds_type] as [host_type],
	  [host_os] as [host_os],
	  [kernel_version] as [kernel_version],
	  [kvm_version] [kvm_version],
      [software_version] as [vdsm_version],
	  [cpu_model] as [cpu_model],
	  [physical_mem_mb] as [physical_memory],
      [cpu_cores] as [number_of_cores],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date] 
	  FROM vds_configuration
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_history_view_2_3]'))
DROP VIEW [dbo].[host_history_view_2_3]
GO
CREATE VIEW [dbo].[host_history_view_2_3]
AS
	  SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
      [vds_id] as [host_id],
      [status] as [status],                  
	  CASE substring(software_version,1,3) 
			 WHEN '4.4' THEN '2.1' + substring(software_version,4,Len(software_version))
			 WHEN '4.5' THEN '2.2' + substring(software_version,4,Len(software_version)) 
			 WHEN '4.9' THEN '2.3' + substring(software_version,4,Len(software_version))
			 ELSE [software_version]  
      END
		  as [software_version],       
      [vm_active] as [active_vms],
      [vm_count] as [total_vms],
      [ksm_cpu_percent] as [ksm_cpu_percent],
	  [vms_cores_count] as [total_vms_vcpus],
      [cpu_cores] as [cpu_cores],
      [usage_cpu_percent] as [cpu_usage_percent] ,      
      [physical_mem_mb] [physical_memory],
      [usage_mem_percent] as [memory_usage_percent],
	  [time_in_status] as [time_in_status],
	  [max_usage_mem] as [max_memory_usage],
      [max_usage_cpu] as [max_cpu_usage]
      FROM vds_history	
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_interface_configuration_view_2_3]'))
DROP VIEW [dbo].[vm_interface_configuration_view_2_3]
GO
CREATE VIEW [dbo].[vm_interface_configuration_view_2_3]
AS
	  SELECT
	  [id] as [vm_interface_id],
	  [vm_guid] as [vm_id],
	  [mac_addr] as [mac_address],
	  [name] as [interface_name],
	  [network_name] as [network_name],
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]
	  FROM vm_interface_configuration
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_interface_configuration_view_2_3]'))
DROP VIEW [dbo].[host_interface_configuration_view_2_3]
GO
CREATE VIEW [dbo].[host_interface_configuration_view_2_3]
AS
	  SELECT
	  [id] as [host_interface_id],
	  [vds_id] as [host_id],
	  [mac_addr] as [mac_address],
	  [name] as [interface_name],
	  [network_name] as [network_name],
      [is_bond] as [bond],
	  [bond_name] as [bond_name], 
	  [vlan_id] as [vlan_id], 
	  [addr] as [ip_address], 
	  [gateway] as [gateway], 
	  [type]as [type], 	  
	  [_create_date] as [create_date],
	  [_delete_date] as [delete_date]

	  FROM vds_interface_configuration AS a  -- This view filters the bond that are not connected to actual nics.
	  where is_bond is null or (is_bond = 1  -- in linux bond can exist without being actually in use, there is no point 
	  and name in(							 -- in showing them nor to have them in the db.
	  SELECT   bond_name
	  FROM vds_interface_configuration AS b
	  where b.is_bond is null and b.vds_id = a.vds_id))
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_interface_history_view_2_3]'))
DROP VIEW [dbo].[vm_interface_history_view_2_3]
GO
CREATE VIEW [dbo].[vm_interface_history_view_2_3]
AS

SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
	  [interface_id] as [interface_id],
      [vm_guid] as [vm_id],
      [rx_rate] as [bytes_received_rate], 
	  [tx_rate] as [bytes_transmitted_rate],
      [speed] as [speed],
	  [type]as [type],
	  [rx_rate_max] AS [max_bytes_received_rate],
      [tx_rate_max] AS [max_bytes_transmitted_rate]
	  FROM vm_interface_history
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[host_interface_history_view_2_3]'))
DROP VIEW [dbo].[host_interface_history_view_2_3]
GO
CREATE VIEW [dbo].[host_interface_history_view_2_3]
AS

	  SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
	  [interface_id] as [interface_id],
      [vds_id] as [host_id],
      [rx_rate] as [bytes_received_rate], 
	  [tx_rate] as [bytes_transmitted_rate],
      [speed] as [speed],
      [rx_rate_max] AS [max_bytes_received_rate],
      [tx_rate_max] AS [max_bytes_transmitted_rate]

      FROM vds_interface_history
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_disk_configuration_view_2_3]'))
DROP VIEW [dbo].[vm_disk_configuration_view_2_3]
GO
CREATE VIEW [dbo].[vm_disk_configuration_view_2_3]
AS
	SELECT
	[image_guid] as [disk_id],
	[description] as [description],
	[volume_format] as [format],
	[volume_type] as [disk_type], 
	[_create_date] as [create_date],
	[_delete_date] as [delete_date],
	'disk ' + [internal_drive_mapping] as [internal_drive_mapping]
	FROM vm_disk_configuration
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vm_disk_history_view_2_3]'))
DROP VIEW [dbo].[vm_disk_history_view_2_3]
GO
CREATE VIEW [dbo].[vm_disk_history_view_2_3]
AS
	  SELECT
      [id] as [history_id],
      [aggregation_level] as [aggregation_level],		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] as [history_datetime],
	  [vm_guid] as [vm_id],
	  [image_guid] as [disk_id],
	  [actual_size] as [actual_size],
	  [size] as [size],
	  [disk_interface]as [interface],
	  [disk_type] as [disk_type],
	  [imageStatus] as [status],  
	  [read_rate] as [read_rate],
	  [write_rate]  as [write_rate],
	  [read_rate_max] AS [max_read_rate],
	  [write_rate_max] AS [max_write_rate]
	  FROM vm_disk_history
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[enum_translator_view_2_3]'))
DROP VIEW [dbo].[enum_translator_view_2_3]
GO
CREATE VIEW [dbo].[enum_translator_view_2_3]
AS
	SELECT 
	dbo.enum_translator.enum_type,
	dbo.enum_translator.enum_key,
    dbo.enum_translator.value    
    FROM dbo.enum_translator INNER JOIN
         dbo.history_configuration ON
        (dbo.enum_translator.language_code = dbo.history_configuration.var_value
         and dbo.history_configuration.var_name = 'default_language')
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[datacenter_storage_domain_map_view_2_3]'))
DROP VIEW [dbo].[datacenter_storage_domain_map_view_2_3]
GO
CREATE VIEW [dbo].[datacenter_storage_domain_map_view_2_3]
AS
SELECT     *
FROM         dbo.datacenter_storage_domain_map

GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[tag_relations_history_view_2_3]'))
DROP VIEW [dbo].[tag_relations_history_view_2_3]
GO
CREATE VIEW [dbo].[tag_relations_history_view_2_3]
AS
SELECT     entity_id, entity_type, parent_id, attach_date, detach_date
FROM         dbo.tag_relations_history
WHERE	   entity_type in (3,2,5,18)
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[tag_path_history_view_2_3]'))
DROP VIEW [dbo].[tag_path_history_view_2_3]
GO
CREATE VIEW [dbo].[tag_path_history_view_2_3]
AS
SELECT     tag_id, tag_path, tag_level, attach_date, detach_date
FROM         dbo.tags_path_history
GO

IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[tag_details_view_2_3]'))
DROP VIEW [dbo].[tag_details_view_2_3]
GO

CREATE VIEW [dbo].[tag_details_view_2_3]
AS
SELECT     tag_id, tag_name, tag_description, _create_date as create_date, _update_date as update_date, _delete_date as delete_date
FROM         dbo.tag_details

GO



