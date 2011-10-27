SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO


CREATE TABLE [dbo].[history_configuration](
	[var_name] [varchar](50) NOT NULL,
	[var_value] [varchar](255) NULL,
	[var_datetime] [datetime] NULL,
 CONSTRAINT [PK_history_configuration] PRIMARY KEY CLUSTERED 
(
	[var_name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [dbo].[vm_configuration](
	[vm_guid] [uniqueidentifier] NOT NULL,
	[vm_name] [nvarchar](255) NOT NULL,
	[vmt_guid] [uniqueidentifier] NOT NULL,
	[os] [int] NOT NULL CONSTRAINT [DEF_vm_static_os]  DEFAULT ((0)),
	[description] [nvarchar](255) NULL,
	[domain] [nvarchar](40) NULL,
	[creation_date] [datetime] NULL,
	[is_initialized] [bit] NULL,
	[is_auto_suspend] [bit] NULL DEFAULT ((0)),
	[usb_policy] [int] NULL,
	[time_zone] [varchar](40) NULL,
	[is_stateless] [bit] NULL,
	[fail_back] [bit] NULL,
	[auto_startup] [bit] NULL,
	[priority] [int] NOT NULL,
	[_create_date] [datetime] NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL,
	[vds_group_id] [uniqueidentifier] NOT NULL ,
	[dedicated_vm_for_vds] [uniqueidentifier] NULL ,
        [template_name] [nchar](40) NULL,
	[mem_size_mb] [int] NULL,
	[cpu_per_socket] [int] NULL,
	[num_of_sockets] [int] NULL,
	[vm_type] [int] NULL,
 CONSTRAINT [PK_vm_configuration] PRIMARY KEY CLUSTERED
(
      [vm_guid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
 
CREATE TABLE [dbo].[vm_history](
    [id] bigint identity(0,1) primary key NOT NULL,
	[aggregation_level] [tinyint] NOT NULL,
	[history_datetime] [datetime] NOT NULL,
	[vm_guid] [uniqueidentifier] NOT NULL,
	[status] [int] NOT NULL,
	[mem_size_mb] [int] NOT NULL,
	[num_of_monitors] [int] NOT NULL,
	[num_of_sockets] [int] NOT NULL DEFAULT ((1)),
	[num_of_cpus] [int] NOT NULL DEFAULT ((1)),
	[vm_last_up_time] [datetime] NULL,
	[vm_last_boot_time] [datetime] NULL,
	[guest_os] [nvarchar](255) NULL,
	[vm_ip] [nvarchar](255) NULL,
	[guest_cur_user_name] [nvarchar](255) NULL,
	[usage_mem_percent] [int] NULL CONSTRAINT [DEF_vm_statistics_usage_mem_percent]  DEFAULT ((0)),
	[usage_cpu_percent] [int] NULL CONSTRAINT [DEF_vm_statistics_usage_cpu_percent]  DEFAULT ((0)),
	[vds_group_id] [uniqueidentifier] NOT NULL ,
	[dedicated_vm_for_vds] [uniqueidentifier] NULL ,
	[run_on_vds] [uniqueidentifier] NULL ,
    [time_in_status] [int] NOT NULL DEFAULT ((1)),
    [max_usage_mem] [int] NULL,
	[max_usage_cpu] [int] NULL,
) ON [PRIMARY]
 
CREATE NONCLUSTERED INDEX [IDX_vm_history_history_datetime] ON [dbo].[vm_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO

CREATE TABLE [dbo].[vds_group_configuration](
	[vds_group_id] [uniqueidentifier] NOT NULL,
	[name] [nvarchar](40) NOT NULL,
	[description] [nvarchar](255) NULL,
	[cpu_name] [nvarchar](255) NULL,
	[storage_pool_id] [uniqueidentifier] NULL,
	[compatibility_version] [nvarchar](40) NOT NULL DEFAULT ('2.2'),
	[vds_group_id___old] [int] NOT NULL,
	[_create_date] [datetime] NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL,
 CONSTRAINT [PK_vds_groups] PRIMARY KEY CLUSTERED
(
      [vds_group_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

 
CREATE TABLE [dbo].[vds_configuration](
	[vds_id] [uniqueidentifier] NOT NULL,
	[vds_name] [nvarchar](255) NOT NULL,
	[pm_ip_address] [nvarchar](40) NULL,
	[vds_unique_id] [nvarchar](128) NULL,
	[host_name] [nvarchar](255) NOT NULL,
	[port] [int] NOT NULL,
	[vds_group_id] [uniqueidentifier] NOT NULL,
	[vds_type] [int] NOT NULL DEFAULT ((0)),
	[cpu_flags] [nvarchar](max) NULL,
	[vds_id___old] [int] NOT NULL,
	[_create_date] [datetime] NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL,
    [host_os] [nchar](40) NULL,
	[kernel_version] [nchar](40) NULL,
	[kvm_version] [nchar](40) NULL,
    [software_version] [nchar](40) NULL,
	[cpu_model] [nchar](255) NULL,
	[physical_mem_mb] [int] NULL,
    [cpu_cores] [int] NULL,
 CONSTRAINT [PK_vds_configuration] PRIMARY KEY CLUSTERED
(
      [vds_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

 
CREATE TABLE [dbo].[vds_history](
	  [id] bigint identity(0,1) primary key NOT NULL,
	[aggregation_level] [tinyint] NOT NULL,
	[history_datetime] [datetime] NOT NULL,
	[status] [int] NOT NULL,
	[software_version] [varchar](40) NULL,
	[cpu_cores] [int] NULL,
	[physical_mem_mb] [int] NULL,
	[vm_active] [int] NULL CONSTRAINT [DEF_vds_dynamic_vm_active]  DEFAULT ((0)),
	[vm_count] [int] NULL CONSTRAINT [DEF_vds_dynamic_vm_count]  DEFAULT ((0)),
	[vms_cores_count] [int] NULL CONSTRAINT [DEF_vds_statistics_usage_mem_percent]  DEFAULT ((0)),
	[usage_mem_percent] [int] NULL CONSTRAINT [DEF_vds_statistics_usage_cpu_percent]  DEFAULT ((0)),
	[usage_cpu_percent] [int] NULL,
	[ksm_cpu_percent] [int] NULL DEFAULT ((0)),
	[vds_id] [uniqueidentifier] NOT NULL,
    [time_in_status] [int] NOT NULL DEFAULT ((1)),
    [max_usage_mem] [int] NULL,
	[max_usage_cpu] [int] NULL,
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_vds_history_history_datetime] ON [dbo].[vds_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO


CREATE TABLE  [dbo].[vm_interface_configuration](
	[id] [uniqueidentifier] NOT NULL,
	[vm_guid] [uniqueidentifier] NULL,
	[mac_addr] [nvarchar](20) NULL,
	[name] [nvarchar](50) NOT NULL,
	[network_name] [nvarchar](50) NULL,
	[_create_date] [datetime] NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL,
) ON [PRIMARY]
GO


CREATE TABLE  [dbo].[vds_interface_configuration](
	[id] uniqueidentifier primary key NOT NULL,
	[mac_addr] [nvarchar](20) NULL,
	[name] [nvarchar](50) NOT NULL,
	[network_name] [nvarchar](50) NULL,
	[is_bond] [bit] NULL,
	[bond_name] [nvarchar](50) NULL,
	[bond_type] [int] NULL,
	[vlan_id] [int] NULL,
	[addr] [nvarchar](20) NULL,
	[gateway] [nvarchar](20) NULL,
	[type] [int] NULL,
	[_create_date] [datetime] NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL,
	[vds_id] [uniqueidentifier] NOT NULL,
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[vm_interface_history](
	 [id] bigint identity(0,1) primary key NOT NULL,
	[aggregation_level] [tinyint] NOT NULL,
	[history_datetime] [datetime] NOT NULL,
	[interface_id] [uniqueidentifier] NOT NULL,
	[vm_guid] [uniqueidentifier] NOT NULL,
	[rx_rate] [decimal](18, 0) NULL,
	[tx_rate] [decimal](18, 0) NULL,
	[speed] [int] NULL,
	[type] [int] NULL,
	[rx_rate_max] [decimal](18, 0) NULL,
	[tx_rate_max] [decimal](18, 0) NULL,
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_vm_interface_history_history_datetime] ON [dbo].[vm_interface_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO

CREATE TABLE [dbo].[vds_interface_history](
	[id] bigint identity(0,1) primary key NOT NULL,
	[aggregation_level] [tinyint] NOT NULL,
	[history_datetime] [datetime] NOT NULL,
	[interface_id] [uniqueidentifier] NOT NULL,
	[rx_rate] [decimal](18, 0) NULL,
	[tx_rate] [decimal](18, 0) NULL,
	[speed] [int] NULL,
	[vds_id] [uniqueidentifier] NOT NULL,
	[rx_rate_max] [decimal](18, 0) NULL,
	[tx_rate_max] [decimal](18, 0) NULL
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_vds_interface_history_history_datetime] ON [dbo].[vds_interface_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO


CREATE TABLE vm_disk_configuration (
	[image_guid] [uniqueidentifier]  NOT NULL,
	[creation_date] [datetime] NOT NULL,
	[description] nvarchar(max) NULL,
	[internal_drive_mapping] nvarchar(50) NULL,
	[volume_format] int NULL,
	[volume_type] int NULL, 
    [_create_date] [datetime] NULL,
  	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL,
CONSTRAINT [PK_vm_disk_configuration] PRIMARY KEY CLUSTERED
	(
      [image_guid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [dbo].[vm_disk_history](
	  [id] bigint identity(0,1) primary key NOT NULL,
      [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] [datetime] NOT NULL,
	  [vm_guid] [uniqueidentifier]  NOT NULL,
	  [image_guid] [uniqueidentifier]  NOT NULL,
	  [actual_size] [bigint] NOT NULL,
	  [size] [bigint] NOT NULL,
	  [disk_interface][int] NOT NULL,
	  [disk_type][int] NOT NULL,
	  [imageStatus] int NULL,  
	  [read_rate] [int] NULL,
	  [write_rate]  [int] NULL,
	  [storage_id] [uniqueidentifier] NULL,
	  [read_rate_max] [int] NULL,
	  [write_rate_max]  [int] NULL
)ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_vm_disk_history_history_datetime] ON [dbo].[vm_disk_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO	

CREATE TABLE [dbo].[storage_domain_configuration](
	  [storage_domain_id] [uniqueidentifier] NOT NULL,
	  [storage_name] [nvarchar](250) NOT NULL,
	  [storage_domain_type] [int] NOT NULL,
	  [storage_type] [int] NOT NULL,
      [_create_date] [datetime] NULL,
	  [_update_date] [datetime] NULL,
	  [_delete_date] [datetime] NULL,
 CONSTRAINT [pk_storage_domain] PRIMARY KEY CLUSTERED 
(
	[storage_domain_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[storage_domain_history](
	  [id] bigint identity(0,1) primary key NOT NULL,
      [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] [datetime] NOT NULL,
	  [storage_domain_id] [uniqueidentifier] NOT NULL,
	  [available_disk_size] [int] NULL,
	  [used_disk_size] [int] NULL,
) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IDX_storage_domain_history_datetime] ON [dbo].[storage_domain_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]
GO

CREATE TABLE [dbo].[datacenter_configuration](
	[datacenter_id] [uniqueidentifier] primary key NOT NULL,
	[name] [nchar](40) NOT NULL,
	[description] [nchar](255) NOT NULL,	
	[storage_pool_type] [int] NOT NULL,
    [_create_date] [datetime] NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[datacenter_history](
	[id] bigint identity(0,1) primary key NOT NULL,
    [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
    [history_datetime] [datetime] NOT NULL,
	[datacenter_id] [uniqueidentifier] NOT NULL,
	[status] [int] NOT NULL,
    [time_in_status] [int] NOT NULL DEFAULT ((1)),
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_datacenter_history_datetime] ON [dbo].[datacenter_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]
GO


CREATE TABLE [dbo].[enum_translator](
	[enum_type] [nchar](40) NOT NULL,
	[enum_key] [int] NOT NULL,
	[language_code] [nvarchar](40) NOT NULL,
	[value] [nchar](40) NOT NULL,
 CONSTRAINT [PK_enums] PRIMARY KEY CLUSTERED 
(
	[enum_type] ASC,
	[enum_key] ASC,
	[language_code] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[period](
	[MONTH_NAME] [nchar](20) NOT NULL,
	[MONTH_STARTDATE] [datetime] NOT NULL,
	[MONTH_ENDDATE] [datetime] NOT NULL,
	[QUARTER_ENDDATE] [datetime] NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[datacenter_storage_domain_map](
	[storage_id] [uniqueidentifier] NOT NULL,
	[datacenter_id] [uniqueidentifier] NOT NULL,
	[attach_date] [datetime] NOT NULL,
	[detach_date] [datetime] NULL,
 CONSTRAINT [PK_datacenter_storage_domain_map] PRIMARY KEY CLUSTERED 
(
	[storage_id] ASC,
	[datacenter_id] ASC,
	[attach_date] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[tag_details](
	[tag_id] [uniqueidentifier] NOT NULL,
	[tag_name] [nvarchar](50) NOT NULL,
	[tag_description] [nvarchar](max) NULL,
	[_create_date] [datetime] NOT NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL,
 CONSTRAINT [PK_tag_details] PRIMARY KEY CLUSTERED
(
	[tag_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO


CREATE TABLE [dbo].[tag_relations_history](
	[id] [int] NOT NULL IDENTITY(1,1),
	[entity_id] [uniqueidentifier] NOT NULL,
	[entity_type] [int] NOT NULL,
	[parent_id] [uniqueidentifier] NULL,
	[attach_date] [datetime] NOT NULL,
	[detach_date] [datetime] NULL,
 CONSTRAINT [PK_tag_relations_history] PRIMARY KEY NONCLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE CLUSTERED INDEX [IX_tag_relations_history] ON [dbo].[tag_relations_history]
(
	[entity_id] ASC,
	[attach_date] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IX_tag_relations_history_1] ON [dbo].[tag_relations_history]
(
	[entity_type] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

CREATE TABLE [dbo].[tags_path_history](
	[id] [int] NOT NULL IDENTITY(1,1),
	[tag_id] [uniqueidentifier] NOT NULL,
	[tag_path] [nvarchar](max) NULL,
	[tag_level] [int] NOT NULL,
	[attach_date] [datetime] NULL,
	[detach_date] [datetime] NULL,
 CONSTRAINT [PK_tags_path_history] PRIMARY KEY NONCLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE CLUSTERED INDEX [IX_tags_path_history] ON [dbo].[tags_path_history]
(
	[tag_level] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IX_tags_path_history_1] ON [dbo].[tags_path_history]
(
	[tag_id] ASC,
	[attach_date] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO
