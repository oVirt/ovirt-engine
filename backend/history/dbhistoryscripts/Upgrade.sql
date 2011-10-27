
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET NOCOUNT ON
GO
print 'INT to UUID script started...'
go

/****************************************************
	           INT ID to UUID Upgrade
****************************************************/


--1) rename all relevant tables adding a suffix "___old"
--2) add a new table per entity with same id name (uniqueidentifier) and <name>___old column at last place
--3) insert data from ___old tables to new created with newid() for new ID


print 'Renaming all relevant tables adding a suffix "___old...'
go


declare @type int
select @type =  system_type_id from sys.types where name = 'int'
if (exists (select name from sys.columns where name = 'vds_group_id' and 
                                         object_id = object_id('vds_group_configuration') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vds_group_configuration' ,'vds_group_configuration___old'	
end

if (exists (select name from sys.columns where name = 'vds_id' and 
                                         object_id = object_id('vds_configuration') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vds_configuration' ,'vds_configuration___old'	
end
go

print 'Creating new tables for changed entities...'
go


declare @type int
select @type =  system_type_id from sys.types where name = 'int'

	--2) add a new table per entity with same id name (uniqueidentifier) and <name>___old column at last place


if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'vds_group_configuration'))
begin
	CREATE TABLE [dbo].[vds_group_configuration](
		  [vds_group_id] uniqueidentifier  NOT NULL,
		  [name] [nvarchar](40) NOT NULL,
		  [description] [nvarchar](255) NULL,
		  [cpu_name] [nvarchar](255) NULL,
		  [storage_pool_id] [uniqueidentifier] NULL,
		  [compatibility_version] [nvarchar](40) NOT NULL DEFAULT '2.2',
		  [vds_group_id___old] [int]  NOT NULL,
		  [_create_date] [datetime] NULL,
  		  [_update_date] [datetime] NULL,
		  [_delete_date] [datetime] NULL,
		  
	)
end

if (not exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'vds_configuration'))
begin
	CREATE TABLE [dbo].[vds_configuration](
		  [vds_id] uniqueidentifier  NOT NULL,
		  [vds_name] [nvarchar](255) NOT NULL,
		  [pm_ip_address] [nvarchar](40) NULL,
		  [vds_unique_id] [nvarchar](128) NULL,
		  [host_name] [nvarchar](255) NOT NULL,
		  [port] [int] NOT NULL,
		  [vds_group_id] uniqueidentifier NOT NULL,
		  [vds_type] [int] NOT NULL  DEFAULT 0,
		  [subnet_mask] [nvarchar](255) NULL,
		  [cpu_flags] nvarchar(max)  NULL,
		  [vds_id___old] [int]  NOT NULL,
		  [vds_group_id___old] [int] NOT NULL,
		  [_create_date] [datetime] NULL,
  		  [_update_date] [datetime] NULL,
		  [_delete_date] [datetime] NULL
	)
end
go

--4) insert data from ___old tables to new created with newid() for new uniqueidentifier
print 'Poulating new tables with data from old tables...'
go

declare @type int
select @type =  system_type_id from sys.types where name = 'uniqueidentifier'
if (exists (select name from sys.columns where name = 'vds_group_id' and 
                                         object_id = object_id('vds_group_configuration') and
                                          system_type_id = @type))
begin
	insert into vds_group_configuration (vds_group_id, name, description, cpu_name, storage_pool_id, compatibility_version, vds_group_id___old, _create_date, _update_date, _delete_date)
						   (select newid(), name, description, cpu_name, storage_pool_id, compatibility_version, vds_group_id, _create_date, _update_date, _delete_date from vds_group_configuration___old
							where [name] not in (select [name] from vds_group_configuration))
end

if (exists (select name from sys.columns where name = 'vds_id' and 
                                         object_id = object_id('vds_configuration') and
                                          system_type_id = @type))
begin
	insert into vds_configuration (vds_id, vds_name, pm_ip_address, vds_unique_id, host_name, port, vds_group_id, vds_type, subnet_mask, cpu_flags, vds_id___old, vds_group_id___old, _create_date, _update_date, _delete_date)
						   (select newid(), vds_name, pm_ip_address, vds_unique_id, host_name, port, newid(), vds_type, subnet_mask, cpu_flags, vds_id, vds_group_id, _create_date, _update_date, _delete_date from vds_configuration___old
							where vds_name not in (select vds_name from vds_configuration))
end
go

--update
print 'Updating references...'
go

update vds_group_configuration set vds_group_id = (select vds_group_id from $(db)..vds_groups
								   where  vds_group_configuration.vds_group_id___old = $(db)..vds_groups.vds_group_id___old)

update vds_configuration set vds_id = (select vds_id from $(db)..vds_static 
								   where  vds_configuration.vds_id___old = $(db)..vds_static.vds_id___old)

update vds_configuration set vds_group_id = (select vds_group_id from vds_group_configuration 
								   where  vds_configuration.vds_group_id___old =  vds_group_configuration.vds_group_id___old)

go

-- Update refrences in other tables 

declare @type int
select @type =  system_type_id from sys.types where name = 'int'
if (not exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vm_configuration ') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vm_configuration.vds_group_id' , 'vds_group_id___old'
	alter table vm_configuration add  vds_group_id uniqueidentifier not null default '00000000-0000-0000-0000-000000000000'
end
if (not exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vds_configuration ') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vds_configuration.vds_group_id' , 'vds_group_id___old'
	alter table vds_configuration add  vds_group_id uniqueidentifier not null default '00000000-0000-0000-0000-000000000000'
end
if (not exists (select name from sys.columns where name = 'dedicated_vm_for_vds___old' and 
                                         object_id = object_id('vm_configuration ') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vm_configuration.dedicated_vm_for_vds' , 'dedicated_vm_for_vds___old'
	alter table vm_configuration add  dedicated_vm_for_vds uniqueidentifier null 
end

if (not exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_interface_configuration') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vds_interface_configuration.vds_id' , 'vds_id___old'
	alter table vds_interface_configuration add  vds_id uniqueidentifier not null default '00000000-0000-0000-0000-000000000000'
end
go

--setting defaults
declare @type int
select @type =  system_type_id from sys.types where name = 'int'
if ((exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vm_configuration') and
                                          system_type_id = @type)) and 
    (not exists(SELECT [name] FROM syscolumns WHERE id = object_id('vm_configuration') and 
    [name] = 'vds_group_id___old' AND cdefault > 0)))
begin
	alter table vm_configuration add constraint DF_vm_configuration_vds_group_id___old default  -100 for vds_group_id___old
end

if ((exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vds_configuration') and
                                          system_type_id = @type)) and 
    (not exists(SELECT [name] FROM syscolumns WHERE id = object_id('vds_configuration') and 
    [name] = 'vds_group_id___old' AND cdefault > 0)))
begin
	alter table vds_configuration add constraint DF_vds_configuration_vds_group_id___old default  -100 for vds_group_id___old
end

if ((exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_interface_configuration') and
                                          system_type_id = @type))and 
    (not exists(SELECT [name] FROM syscolumns WHERE id = object_id('vds_interface_configuration') and 
    [name] = 'vds_id___old' AND cdefault > 0)))
begin
	alter table vds_interface_configuration add constraint DF_vds_interface_configuration__vds_id___old default  -100 for vds_id___old
end
go

declare @type int
select @type =  system_type_id from sys.types where name = 'int'
if (exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vm_configuration') and
                                          system_type_id = @type))
begin
	update vm_configuration set vds_group_id = (select vds_group_id from vds_group_configuration 
								   where  vm_configuration.vds_group_id___old = vds_group_configuration.vds_group_id___old)
end

if (exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vds_configuration') and
                                          system_type_id = @type))
begin
	update vds_configuration set vds_group_id = (select vds_group_id from vds_group_configuration 
								   where  vds_configuration.vds_group_id___old = vds_group_configuration.vds_group_id___old)
end

if (exists (select name from sys.columns where name = 'dedicated_vm_for_vds___old' and 
                                         object_id = object_id('vm_configuration') and
                                          system_type_id = @type))
begin
	update vm_configuration set dedicated_vm_for_vds = (select vds_id from vds_configuration 
								   where  vm_configuration.dedicated_vm_for_vds___old is not null and
								   vm_configuration.dedicated_vm_for_vds___old = vds_configuration.vds_id___old)
end

if (exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_interface_configuration') and
                                          system_type_id = @type))
begin
	update vds_interface_configuration set vds_id = (select vds_id from vds_configuration 
								   where  vds_interface_configuration.vds_id___old = vds_configuration.vds_id___old)
end
go

-- upgrade history tables: vm_history, vds_history and vds_interface_history

declare @type int
select @type =  system_type_id from sys.types where name = 'int'
if (not exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vm_history') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vm_history.vds_group_id' , 'vds_group_id___old'
	alter table vm_history add  vds_group_id uniqueidentifier not null default '00000000-0000-0000-0000-000000000000'
end
if (not exists (select name from sys.columns where name = 'dedicated_vm_for_vds___old' and 
                                         object_id = object_id('vm_history') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vm_history.dedicated_vm_for_vds' , 'dedicated_vm_for_vds___old'
	alter table vm_history add  dedicated_vm_for_vds uniqueidentifier null 
end
if (not exists (select name from sys.columns where name = 'run_on_vds___old' and 
                                         object_id = object_id('vm_history') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vm_history.run_on_vds' , 'run_on_vds___old'
	alter table vm_history add  run_on_vds uniqueidentifier null 
end
if (not exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_history') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vds_history.vds_id' , 'vds_id___old'
	alter table vds_history add  vds_id uniqueidentifier not null default '00000000-0000-0000-0000-000000000000'
end
if (not exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_interface_history') and
                                          system_type_id = @type))
begin
	exec sp_rename 'vds_interface_history.vds_id' , 'vds_id___old'
	alter table vds_interface_history add  vds_id uniqueidentifier not null default '00000000-0000-0000-0000-000000000000'
end
go

--setting defaults
declare @type int
select @type =  system_type_id from sys.types where name = 'int'
if ((exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vm_history') and
                                          system_type_id = @type)) and 
    (not exists(SELECT [name] FROM syscolumns WHERE id = object_id('vm_history') and 
    [name] = 'vds_group_id___old' AND cdefault > 0)))
begin
	alter table vm_history add constraint DF_vm_history_vds_group_id___old default  -100 for vds_group_id___old
end

if ((exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_interface_history') and
                                          system_type_id = @type)) and 
    (not exists(SELECT [name] FROM syscolumns WHERE id = object_id('vds_interface_history') and 
    [name] = 'vds_id___old' AND cdefault > 0)))
begin
	alter table vds_interface_history add constraint DF_vds_interface_history_vds_id___old default  -100 for vds_id___old
end
go

-- upadte history references
declare @type int
select @type =  system_type_id from sys.types where name = 'int'
if (exists (select name from sys.columns where name = 'vds_group_id___old' and 
                                         object_id = object_id('vm_history') and
                                          system_type_id = @type))
begin
	update vm_history set vds_group_id = (select vds_group_id from vds_group_configuration 
								   where  vm_history.vds_group_id___old = vds_group_configuration.vds_group_id___old)
end

if (exists (select name from sys.columns where name = 'dedicated_vm_for_vds___old' and 
                                         object_id = object_id('vm_history') and
                                          system_type_id = @type))
begin
	update vm_history set dedicated_vm_for_vds = (select vds_id from vds_configuration 
								   where  vm_history.dedicated_vm_for_vds___old = vds_configuration.vds_id___old)
end

if (exists (select name from sys.columns where name = 'run_on_vds___old' and 
                                         object_id = object_id('vm_history') and
                                          system_type_id = @type))
begin
	update vm_history set run_on_vds = (select vds_id from vds_configuration 
								   where  vm_history.run_on_vds___old = vds_configuration.vds_id___old)
end

if (exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_history') and
                                          system_type_id = @type))
begin
	update vds_history set vds_id = (select vds_id from vds_configuration 
								   where  vds_history.vds_id___old = vds_configuration.vds_id___old)
end

if (exists (select name from sys.columns where name = 'vds_id___old' and 
                                         object_id = object_id('vds_interface_history') and
                                          system_type_id = @type))
begin
	update vds_interface_history set vds_id = (select vds_id from vds_configuration 
								   where  vds_interface_history.vds_id___old = vds_configuration.vds_id___old)
end


-- Cleanup
declare @table varchar(128)
declare @col varchar(128)
declare @statement nvarchar(max)
DECLARE DropOldColumnsCursor CURSOR FOR
	select object_name(object_id) as [table], [name]  from sys.columns where name like '%___old' 	
OPEN DropOldColumnsCursor

FETCH NEXT FROM DropOldColumnsCursor INTO @table, @col

WHILE @@FETCH_STATUS = 0
BEGIN
	if ((@table = 'vds_group_configuration' and @col = 'vds_group_id___old') or
		(@table = 'vds_configuration' and @col = 'vds_id___old'))
	begin -- do not delete *___old fields that served as PK
			FETCH NEXT FROM DropOldColumnsCursor INTO  @table, @col
			continue
	end
	else
	begin

		-- find first if there are constrainst defined on the column and delete them
		declare @name nvarchar(128)	
		declare @actual_col_name varchar(128)
		set @actual_col_name = substring(@col,1,len(@col) - 6)
		-- find constraint name
		select top 1 @name = O.name 
		from sysobjects AS O
		left join sysobjects AS T
			on O.parent_obj = T.id
		where isnull(objectproperty(O.id,'IsMSShipped'),1) = 0
			and O.name not like '%dtproper%'
			and O.name not like 'dt[_]%'
			and T.name = @table
			and (O.name like 'DF_%' + @table + '__' + @actual_col_name + '%' or
                 O.name like 'DF_%' + @table + '__' + @col + '%' or
				 O.name like 'DF_%' + @table + '_' + @actual_col_name + '%' or
                 O.name like 'DF_%' + @table + '_' + @col + '%')
		-- delete if found
		if not @name is null
		begin
			select @statement = 'ALTER TABLE ' + @table + ' DROP CONSTRAINT [' + @name + ']'
			print 'Executing ' + @statement + '...'
			execute sp_executesql @statement
			set @name = null
		end

		set @statement = 'ALTER TABLE ' + @table + ' DROP Column ' + @col
		print 'Executing ' + @statement + '...'
		exec sp_executesql @statement
	end
	FETCH NEXT FROM DropOldColumnsCursor INTO  @table, @col
END
CLOSE DropOldColumnsCursor
DEALLOCATE DropOldColumnsCursor
go

/****************************************************
	        END of INT ID to UUID Upgrade
****************************************************/
waitfor delay '00:00:5'
print 'INT to UUID script completed...'
go
