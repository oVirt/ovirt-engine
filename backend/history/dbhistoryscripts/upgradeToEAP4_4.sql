
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[history_configuration] (
      var_name varchar(50),
      var_value varchar(255)
)
GO

CREATE TABLE [dbo].[vm_configuration](
      [vm_guid] [uniqueidentifier] NOT NULL,
      [vm_name] [nvarchar](40) NOT NULL,
      [vmt_guid] [uniqueidentifier] NOT NULL,   -- ***?
      [os] [int] NOT NULL CONSTRAINT [DEF_vm_static_os]  DEFAULT ((0)),
      [description] [nvarchar](255) NULL,
      [domain] [nvarchar](40) NULL,
      [creation_date] [datetime] NULL,
      [vds_group_id] [int] NOT NULL,                  -- ***
      [is_initialized] [bit] NULL,
      [is_auto_suspend] [bit] NULL DEFAULT ((0)),
      [usb_policy] [int] NULL,
      [time_zone] [varchar](40) NULL,
      [is_stateless] [bit] NULL,
      [fail_back] [bit] NULL,
      [dedicated_vm_for_vds] [int] NULL,
      [auto_startup] [bit] NULL,
      [_create_date] [datetime] NULL,
  	  [_update_date] [datetime] NULL,
	  [_delete_date] [datetime] NULL
 CONSTRAINT [PK_vm_configuration] PRIMARY KEY CLUSTERED
(
      [vm_guid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
 
CREATE TABLE [dbo].[vm_history](
      [id] bigint identity(0,1) primary key NOT NULL,
      [aggregation_level] [tinyint] NOT NULL,		  -- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] [datetime] NOT NULL,
      [vm_guid] [uniqueidentifier] NOT NULL,
      [vds_group_id] [int] NOT NULL,  
	  [dedicated_vm_for_vds] [int] NULL,                -- ***
      [status] [int] NOT NULL,                        -- ***
      [mem_size_mb] [int] NOT NULL,
      [num_of_monitors] [int] NOT NULL,
      [num_of_cpus] [int] NOT NULL,
--!!  [status] [int] NOT NULL,                        -- *** - moved up
--    [vm_ip] [nvarchar](255) NULL,
--    [vm_host] [varchar](255) NULL,
--    [vm_pid] [int] NULL,
      [vm_last_up_time] [datetime] NULL,
      [vm_last_boot_time] [datetime] NULL,
--    [guest_cur_user_name] [nvarchar](40) NULL,
--    [guest_cur_user_id] [uniqueidentifier] NULL,
      [guest_last_login_time] [datetime] NULL,
--    [guest_last_logout_time] [datetime] NULL,
      [guest_os] [nvarchar](255) NULL,
--!!  [run_on_vds] [int] NULL,                        -- *** - moved up
--    [migrating_to_vds] [int] NULL,
--    [app_list] [nvarchar](2048) NULL,
--    [display] [int] NULL,
--    [acpi_enable] [bit] NULL,
--    [session] [int] NULL,
--    [display_ip] [varchar](255) NULL,
--    [display_type] [int] NULL,
--    [kvm_enable] [bit] NULL,
--    [boot_device] [int] NULL,
--    [display_secure_port] [int] NULL,
--    [utc_diff] [int] NULL,
--    [last_vds_run_on] [int] NULL,
--    [client_ip] [varchar](255) NULL,
      [guest_requested_memory] [int] NULL, 
      [cpu_user] [decimal](18, 0) NULL CONSTRAINT [DEF_vm_statistics_cpu_user]  DEFAULT ((0)),
      [cpu_sys] [decimal](18, 0) NULL CONSTRAINT [DEF_vm_statistics_cpu_sys]  DEFAULT ((0)),
      [elapsed_time] [decimal](18, 0) NULL CONSTRAINT [DEF_vm_statistics_elapsed_time]  DEFAULT ((0)),
      [usage_network_percent] [int] NULL CONSTRAINT [DEF_vm_statistics_usage_network_percent]  DEFAULT ((0)),
      [usage_mem_percent] [int] NULL CONSTRAINT [DEF_vm_statistics_usage_mem_percent]  DEFAULT ((0)),
      [usage_cpu_percent] [int] NULL CONSTRAINT [DEF_vm_statistics_usage_cpu_percent]  DEFAULT ((0))

) ON [PRIMARY]
 
CREATE NONCLUSTERED INDEX [IDX_vm_history_history_datetime] ON [dbo].[vm_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO

CREATE TABLE [dbo].[vds_group_configuration](
      [vds_group_id] [int]  NOT NULL,
      [name] [nvarchar](40) NOT NULL,
      [description] [nvarchar](255) NULL,
      [cpu_name] [nvarchar](255) NULL,
      [_create_date] [datetime] NULL,
  	  [_update_date] [datetime] NULL,
	  [_delete_date] [datetime] NULL,
 CONSTRAINT [PK_vds_groups] PRIMARY KEY CLUSTERED
(
      [vds_group_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
 
CREATE TABLE [dbo].[vds_configuration](
      [vds_id] [int]  NOT NULL,
      [vds_name] [nvarchar](40) NOT NULL,
      [ip] [nvarchar](40) NULL,
      [vds_unique_id] [nvarchar](36) NULL,
      [host_name] [nvarchar](255) NOT NULL,
      [port] [int] NOT NULL,
      [vds_group_id] [int] NOT NULL,
      [server_SSL_enabled] [bit] NULL,
      [vds_type] [int] NOT NULL CONSTRAINT [DF_vds_static_vds_type]  DEFAULT ((0)),
      [subnet_mask] [nvarchar](255) NULL,
      [_create_date] [datetime] NULL,
  	  [_update_date] [datetime] NULL,
	  [_delete_date] [datetime] NULL
 CONSTRAINT [PK_vds_configuration] PRIMARY KEY CLUSTERED
(
      [vds_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
 
CREATE TABLE [dbo].[vds_history](
	  [id] bigint identity(0,1) primary key NOT NULL,
      [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] [datetime] NOT NULL,
      [vds_id] [int] NOT NULL,
      [status] [int] NOT NULL,                  -- ***
      [software_version] [varchar](40) NULL,    -- *** 
      [cpu_cores] [int] NULL,
      [cpu_model] [nvarchar](255) NULL,
      [cpu_speed_mh] [decimal](18, 0) NULL,
--    [if_total_speed] [nvarchar](40) NULL,
--    [kvm_enabled] [bit] NULL,
      [physical_mem_mb] [int] NULL,
      [mem_commited] [int] NULL CONSTRAINT [DEF_vds_dynamic_mem_commited]  DEFAULT ((0)),
      [vm_active] [int] NULL CONSTRAINT [DEF_vds_dynamic_vm_active]  DEFAULT ((0)),
      [vm_count] [int] NULL CONSTRAINT [DEF_vds_dynamic_vm_count]  DEFAULT ((0)),
      [vm_migrating] [int] NULL CONSTRAINT [DEF_vds_dynamic_vm_migrating]  DEFAULT ((0)),
      [reserved_mem] [int] NULL,
      [guest_overhead] [int] NULL,
--!!  [software_version] [varchar](40) NULL,    -- *** - moved up
--    [version_name] [varchar](40) NULL,
--    [build_name] [varchar](40) NULL,
--    [previous_status] [int] NULL,
--    [cpu_flags] [nvarchar](max) NULL,
      [cpu_idle] [decimal](18, 0) NULL CONSTRAINT [DEF_vds_statistics_cpu_idle]  DEFAULT ((0)),
      [cpu_load] [decimal](18, 0) NULL CONSTRAINT [DEF_vds_statistics_cpu_load]  DEFAULT ((0)),
      [cpu_sys] [decimal](18, 0) NULL CONSTRAINT [DEF_vds_statistics_cpu_sys]  DEFAULT ((0)),
      [cpu_user] [decimal](18, 0) NULL CONSTRAINT [DEF_vds_statistics_cpu_user]  DEFAULT ((0)),
      [elapsed_time] [int] NULL CONSTRAINT [DEF_vds_statistics_elapsed_time]  DEFAULT ((0)),
      [usage_mem_percent] [int] NULL CONSTRAINT [DEF_vds_statistics_usage_mem_percent]  DEFAULT ((0)),
      [usage_cpu_percent] [int] NULL CONSTRAINT [DEF_vds_statistics_usage_cpu_percent]  DEFAULT ((0)),
      [usage_network_percent] [int] NULL,
      [mem_available] [bigint] NULL,
      [mem_shared] [bigint] NULL

) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_vds_history_history_datetime] ON [dbo].[vds_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO


CREATE TABLE  [dbo].[vm_interface_configuration](
	  [id] uniqueidentifier primary key NOT NULL,
	  [vm_guid] [uniqueidentifier] NOT NULL,
	  [mac_addr] [nvarchar] (20) NULL,
	  [name] [nvarchar] (50) NOT  NULL,
	  [network_name] [nvarchar] (50) NULL,
      [_create_date] [datetime] NULL,
  	  [_update_date] [datetime] NULL,
	  [_delete_date] [datetime] NULL
) ON [PRIMARY]
GO


CREATE TABLE  [dbo].[vds_interface_configuration](
	  [id] uniqueidentifier primary key NOT NULL,
	  [vds_id] [int] NOT NULL,
	  [mac_addr] [nvarchar] (20) NULL,
	  [name] [nvarchar] (50) NOT  NULL,
	  [network_name] [nvarchar] (50) NULL,
      [is_bond] [bit] NULL,
	  [bond_name] [nvarchar] (50) NULL, 
      [bond_type] [int] NULL, 
	  [vlan_id] [int] NULL, 
	  [addr][nvarchar] (20) NULL, 
	  [gateway][nvarchar] (20) NULL, 
	  [type][int] NULL, 	  
      [_create_date] [datetime] NULL,
  	  [_update_date] [datetime] NULL,
	  [_delete_date] [datetime] NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[vm_interface_history](
	  [id] bigint identity(0,1) primary key NOT NULL,
      [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] [datetime] NOT NULL,
	  [interface_id][uniqueidentifier] NOT NULL,
      [vm_guid] [uniqueidentifier] NOT NULL,
      [rx_rate] [decimal] (18,0) NULL, 
	  [tx_rate][decimal] (18,0) NULL, 
	  [rx_drop][decimal] (18,0) NULL, 
	  [tx_drop][decimal] (18,0) NULL

) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_vm_interface_history_history_datetime] ON [dbo].[vm_interface_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO

CREATE TABLE [dbo].[vds_interface_history](
	  [id] bigint identity(0,1) primary key NOT NULL,
      [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] [datetime] NOT NULL,
	  [interface_id][uniqueidentifier] NOT NULL,
      [vds_id] [int] NOT NULL,
      [rx_rate] [decimal] (18,0) NULL, 
	  [tx_rate][decimal] (18,0) NULL, 
	  [rx_drop][decimal] (18,0) NULL, 
	  [tx_drop][decimal] (18,0) NULL

) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_vds_interface_history_history_datetime] ON [dbo].[vds_interface_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO

