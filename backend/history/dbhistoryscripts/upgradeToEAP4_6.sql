
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET NOCOUNT ON
GO


print 'Upgrade script Started...'

--Bug 603091 fix
exec sp_rename 'vds_history.usage_mem_percent', mem
exec sp_rename 'vds_history.usage_cpu_percent', cpu
exec sp_rename 'vds_history.vms_cores_count', cores
go

exec sp_rename 'vds_history.mem', vms_cores_count
exec sp_rename 'vds_history.cpu', usage_mem_percent
exec sp_rename 'vds_history.cores', usage_cpu_percent
go

-- end of fix

-- add time in status to vm history table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_history' and COLUMN_NAME = 'time_in_status'))
begin
	ALTER TABLE vm_history ADD [time_in_status] int NOT NULL DEFAULT ((1))
end
go

-- add max memory usage to vm history table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_history' and COLUMN_NAME = 'max_usage_mem'))
begin
	ALTER TABLE vm_history ADD [max_usage_mem] int NULL
end
go

-- add max cpu usage to vm history table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_history' and COLUMN_NAME = 'max_usage_cpu'))
begin
	ALTER TABLE vm_history ADD [max_usage_cpu] int NULL
end
go


-- add time in status vds history table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_history' and COLUMN_NAME = 'time_in_status'))
begin
	ALTER TABLE vds_history ADD [time_in_status] int NOT NULL DEFAULT ((1))
end
go

-- add max memory usage to vds history table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_history' and COLUMN_NAME = 'max_usage_mem'))
begin
	ALTER TABLE vds_history ADD [max_usage_mem] int NULL
end
go

-- add max cpu usage to vds history table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_history' and COLUMN_NAME = 'max_usage_cpu'))
begin
	ALTER TABLE vds_history ADD [max_usage_cpu] int NULL
end
go


-- add columns to vm_configuration table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_configuration' and COLUMN_NAME = 'template_name'))
begin
	ALTER TABLE vm_configuration ADD [template_name] [nchar](40) NULL
end
go

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_configuration' and COLUMN_NAME = 'mem_size_mb'))
begin
	ALTER TABLE vm_configuration ADD [mem_size_mb] [int] NULL
end
go

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_configuration' and COLUMN_NAME = 'cpu_per_socket'))
begin
	ALTER TABLE vm_configuration ADD [cpu_per_socket] [int] NULL
end
go

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_configuration' and COLUMN_NAME = 'num_of_sockets'))
begin
	ALTER TABLE vm_configuration ADD [num_of_sockets] [int] NULL
end
go

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_configuration' and COLUMN_NAME = 'vm_type'))
begin
	ALTER TABLE vm_configuration ADD [vm_type] [int] NULL
end
go

-- adding columns to the vds_configuration table
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'host_os'))
begin
	ALTER TABLE vds_configuration ADD [host_os] [nchar](40) NULL
end
go
 
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'kernel_version'))
begin
	ALTER TABLE vds_configuration ADD [kernel_version] [nchar](40) NULL
end
go

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'kvm_version'))
begin
	ALTER TABLE vds_configuration ADD [kvm_version] [nchar](40) NULL
end
go

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'software_version'))
begin
	ALTER TABLE vds_configuration ADD [software_version] [nchar](40) NULL
end
go	

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'cpu_model'))
begin
	ALTER TABLE vds_configuration ADD [cpu_model] [nchar](255) NULL
end
go	 

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'physical_mem_mb'))
begin
	ALTER TABLE vds_configuration ADD [physical_mem_mb] [int] NULL
end
go 

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'cpu_cores'))
begin
	ALTER TABLE vds_configuration ADD [cpu_cores] [int] NULL
end
go 

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_disk_history' and COLUMN_NAME = 'storage_id'))
begin
	ALTER TABLE vm_disk_history ADD [storage_id] [uniqueidentifier] NULL
end
go 

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_disk_history' and COLUMN_NAME = 'read_rate_max'))
begin
	ALTER TABLE vm_disk_history ADD read_rate_max [int] NULL
end
go  

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_disk_history' and COLUMN_NAME = 'write_rate_max'))
begin
	ALTER TABLE vm_disk_history ADD write_rate_max [int] NULL
end
go 
 
if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_interface_history' and COLUMN_NAME = 'rx_rate_max'))
begin
	ALTER TABLE vds_interface_history ADD rx_rate_max [decimal](18, 0) NULL
end
go  

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_interface_history' and COLUMN_NAME = 'tx_rate_max'))
begin
	ALTER TABLE vds_interface_history ADD tx_rate_max [decimal](18, 0) NULL
end
go  

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_interface_history' and COLUMN_NAME = 'rx_rate_max'))
begin
	ALTER TABLE vm_interface_history ADD rx_rate_max [decimal](18, 0) NULL
end
go  

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vm_interface_history' and COLUMN_NAME = 'tx_rate_max'))
begin
	ALTER TABLE vm_interface_history ADD tx_rate_max [decimal](18, 0) NULL
end
go  

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'history_configuration' and COLUMN_NAME = 'var_datetime'))
begin
	ALTER TABLE history_configuration ADD var_datetime [datetime] NULL
end
go  

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'tag_details' and COLUMN_NAME = '_create_date'))
begin
	ALTER TABLE tag_details ADD _create_date [datetime] NOT NULL
end
go  

if (not exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'tag_details' and COLUMN_NAME = '_update_date'))
begin
	ALTER TABLE tag_details ADD _update_date [datetime] NULL
end
go

if (exists (select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'vds_configuration' and COLUMN_NAME = 'subnet_mask'))
begin
	ALTER TABLE vds_configuration DROP COLUMN subnet_mask
end
go

-- create primary key constraint in history configuration table
IF not EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK_history_configuration' AND TABLE_NAME='history_configuration')
BEGIN
	UPDATE history_configuration SET var_name='' WHERE var_name IS NULL
	ALTER TABLE history_configuration ALTER COLUMN var_name varchar(50) NOT NULL
END
go

IF not EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA='dbo' AND CONSTRAINT_NAME='PK_history_configuration' AND TABLE_NAME='history_configuration')
BEGIN
	ALTER TABLE history_configuration ADD CONSTRAINT PK_history_configuration PRIMARY KEY (var_name)
END
go

-- create tables
if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'storage_domain_configuration'))
begin
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
end
GO


if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'storage_domain_history'))
begin
CREATE TABLE [dbo].[storage_domain_history](
	  [id] bigint identity(0,1) primary key NOT NULL,
      [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
      [history_datetime] [datetime] NOT NULL,
	  [storage_domain_id] [uniqueidentifier] NOT NULL,
	  [available_disk_size] [int] NULL,
	  [used_disk_size] [int] NULL,
) ON [PRIMARY]
end
GO

IF (NOT EXISTS (SELECT [name] FROM sysindexes WHERE [name] = N'IDX_storage_domain_history_datetime'))
begin
CREATE NONCLUSTERED INDEX [IDX_storage_domain_history_datetime] ON [dbo].[storage_domain_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO

if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'datacenter_configuration'))
begin
CREATE TABLE [dbo].[datacenter_configuration](
	[datacenter_id] [uniqueidentifier] primary key NOT NULL,
	[name] [nchar](40) NOT NULL,
	[description] [nchar](255) NOT NULL,	
	[storage_pool_type] [int] NOT NULL,
	[_create_date] [datetime] NULL,
	[_update_date] [datetime] NULL,
	[_delete_date] [datetime] NULL
) ON [PRIMARY]
end
GO

if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'datacenter_history'))
begin
CREATE TABLE [dbo].[datacenter_history](
	[id] bigint identity(0,1) primary key NOT NULL,
    [aggregation_level] [tinyint] NOT NULL,	-- 0 - base, 1 - minute, 2 - hourly, 3 - daily, etc.
    [history_datetime] [datetime] NOT NULL,
	[datacenter_id] [uniqueidentifier] NOT NULL,
	[status] [int] NOT NULL,
    [time_in_status] [int] NOT NULL DEFAULT ((1)),
) ON [PRIMARY]
end
GO

IF (NOT EXISTS (SELECT [name] FROM sysindexes WHERE [name] = N'IDX_datacenter_history_datetime'))
begin
CREATE NONCLUSTERED INDEX [IDX_datacenter_history_datetime] ON [dbo].[datacenter_history] 
(
	[history_datetime] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]
end
GO

if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'enum_translator'))
begin
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
end
GO

if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'period'))
begin
CREATE TABLE [dbo].[period](
	[MONTH_NAME] [nchar](20) NOT NULL,
	[MONTH_STARTDATE] [datetime] NOT NULL,
	[MONTH_ENDDATE] [datetime] NOT NULL,
	[QUARTER_ENDDATE] [datetime] NOT NULL
) ON [PRIMARY]
end
GO

if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'datacenter_storage_domain_map'))
begin
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
end
GO


if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'tag_details'))
begin
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
end
GO


if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'tag_relations_history'))
begin
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

CREATE CLUSTERED INDEX [IX_tag_relations_history] ON [dbo].[tag_relations_history]
(
	[entity_id] ASC,
	[attach_date] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]

CREATE NONCLUSTERED INDEX [IX_tag_relations_history_1] ON [dbo].[tag_relations_history]
(
	[entity_type] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
end
GO

if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'tags_path_history'))
begin
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

CREATE CLUSTERED INDEX [IX_tags_path_history] ON [dbo].[tags_path_history]
(
	[tag_level] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]

CREATE NONCLUSTERED INDEX [IX_tags_path_history_1] ON [dbo].[tags_path_history]
(
	[tag_id] ASC,
	[attach_date] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
end
GO

print 'Upgrade script completed...'
go
