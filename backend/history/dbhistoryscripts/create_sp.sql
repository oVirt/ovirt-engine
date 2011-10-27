SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


IF NOT EXISTS (SELECT NAME FROM sys.objects WHERE TYPE = 'P' AND NAME = 'update_tags_tables')
BEGIN
	EXEC('CREATE PROCEDURE [dbo].[update_tags_tables] AS RETURN')
END
GO

ALTER PROCEDURE [dbo].[update_tags_tables]
@lastSync datetime,
@thisSync datetime
AS
BEGIN
	BEGIN TRY
		BEGIN TRAN
			declare @host_tag tinyint;
			set @host_tag  = 3;
			declare @vm_tag tinyint;
			set @vm_tag = 2;
			declare @vm_pool_tag tinyint;
			set @vm_pool_tag = 5;
			declare @user_tag tinyint;
			set @user_tag = 15;
			declare @user_group_tag tinyint;
			set @user_group_tag = 17;
			declare @tags tinyint;
			set @tags = 18;
			-- Force dirty-read to prevent locks on vdc database
			SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED

			/*---------------------------------------------
                                Tags-Hosts
            ---------------------------------------------*/

			update [dbo].[tag_relations_history]
			set [detach_date] = @thisSync
			where [entity_type] = @host_tag and [detach_date] is null
			and NOT EXISTS(select 1
						   from $(db).[dbo].[tags_vds_map] SourceDBTable
						   where SourceDBTable.[vds_id] = [dbo].[tag_relations_history].[entity_id]
								 and SourceDBTable.[tag_id] = [dbo].[tag_relations_history].[parent_id])

			insert into [engine_history].[dbo].[tag_relations_history] ([entity_id],
																	   [parent_id],
																	   [entity_type],
																	   [attach_date])
			select SourceDBTable.[vds_id],
				   SourceDBTable.[tag_id],
				   @host_tag,
	               @thisSync
			from  $(db).[dbo].[tags_vds_map] SourceDBTable
			where [_create_date]>@lastSync and [_create_date] <= @thisSync and
				  NOT EXISTS(select 1
						     from [engine_history].[dbo].[tag_relations_history] DestinationDBTable
						     where SourceDBTable.[vds_id] = DestinationDBTable.[entity_id]
							   	   and SourceDBTable.[tag_id] = DestinationDBTable.[parent_id]
								   and DestinationDBTable.[detach_date] IS NULL)


			/*---------------------------------------------
                                Tags-VMs
            ---------------------------------------------*/

			update [dbo].[tag_relations_history]
			set [detach_date] = @thisSync
			where [entity_type] = @vm_tag and [detach_date] is null
			and NOT EXISTS(select 1
						   from $(db).[dbo].[tags_vm_map] SourceDBTable
						   where SourceDBTable.[vm_id] = [dbo].[tag_relations_history].[entity_id]
								 and SourceDBTable.[tag_id] = [dbo].[tag_relations_history].[parent_id])

			insert into [engine_history].[dbo].[tag_relations_history] ([entity_id],
																	   [parent_id],
																	   [entity_type],
																	   [attach_date])
			select SourceDBTable.[vm_id],
				   SourceDBTable.[tag_id],
				   @vm_tag,
	               @thisSync
			from  $(db).[dbo].[tags_vm_map] SourceDBTable
			where [_create_date]>@lastSync and [_create_date] <= @thisSync and
				  NOT EXISTS(select 1
						     from [engine_history].[dbo].[tag_relations_history] DestinationDBTable
						     where SourceDBTable.[vm_id] = DestinationDBTable.[entity_id]
							   	   and SourceDBTable.[tag_id] = DestinationDBTable.[parent_id]
								   and DestinationDBTable.[detach_date] IS NULL)

			/*---------------------------------------------
                                Tags-VM pools
            ---------------------------------------------*/

			update [dbo].[tag_relations_history]
			set [detach_date] = @thisSync
			where [entity_type] = @vm_pool_tag and [detach_date] is null
			and NOT EXISTS(select 1
						   from $(db).[dbo].[tags_vm_pool_map] SourceDBTable
						   where SourceDBTable.[vm_pool_id] = [dbo].[tag_relations_history].[entity_id]
								 and SourceDBTable.[tag_id] = [dbo].[tag_relations_history].[parent_id])

			insert into [engine_history].[dbo].[tag_relations_history] ([entity_id],
																	   [parent_id],
																	   [entity_type],
																	   [attach_date])
			select SourceDBTable.[vm_pool_id],
				   SourceDBTable.[tag_id],
				   @vm_pool_tag,
	               @thisSync
			from  $(db).[dbo].[tags_vm_pool_map] SourceDBTable
			where [_create_date]>@lastSync and [_create_date] <= @thisSync and
				  NOT EXISTS(select 1
						     from [engine_history].[dbo].[tag_relations_history] DestinationDBTable
						     where SourceDBTable.[vm_pool_id] = DestinationDBTable.[entity_id]
							   	   and SourceDBTable.[tag_id] = DestinationDBTable.[parent_id]
								   and DestinationDBTable.[detach_date] IS NULL)

			/*---------------------------------------------
                                Tags-users
            ---------------------------------------------*/

			update [dbo].[tag_relations_history]
			set [detach_date] = @thisSync
			where [entity_type] = @user_tag and [detach_date] is null
			and NOT EXISTS(select 1
						   from $(db).[dbo].[tags_user_map] SourceDBTable
						   where SourceDBTable.[user_id] = [dbo].[tag_relations_history].[entity_id]
								 and SourceDBTable.[tag_id] = [dbo].[tag_relations_history].[parent_id])

			insert into [engine_history].[dbo].[tag_relations_history] ([entity_id],
																	   [parent_id],
																	   [entity_type],
																	   [attach_date])
			select SourceDBTable.[user_id],
				   SourceDBTable.[tag_id],
				   @user_tag,
	               @thisSync
			from  $(db).[dbo].[tags_user_map] SourceDBTable
			where [_create_date]>@lastSync and [_create_date] <= @thisSync and
				  NOT EXISTS(select 1
						     from [engine_history].[dbo].[tag_relations_history] DestinationDBTable
						     where SourceDBTable.[user_id] = DestinationDBTable.[entity_id]
							   	   and SourceDBTable.[tag_id] = DestinationDBTable.[parent_id]
								   and DestinationDBTable.[detach_date] IS NULL)


			/*---------------------------------------------
                                Tags-user groups
            ---------------------------------------------*/

			update [dbo].[tag_relations_history]
			set [detach_date] = @thisSync
			where [entity_type] = @user_group_tag and [detach_date] is null
			and NOT EXISTS(select 1
						   from $(db).[dbo].[tags_user_group_map] SourceDBTable
						   where SourceDBTable.[group_id] = [dbo].[tag_relations_history].[entity_id]
								 and SourceDBTable.[tag_id] = [dbo].[tag_relations_history].[parent_id])

			insert into [engine_history].[dbo].[tag_relations_history] ([entity_id],
																	   [parent_id],
																	   [entity_type],
																	   [attach_date])
			select SourceDBTable.[group_id],
				   SourceDBTable.[tag_id],
				   @user_group_tag,
	               @thisSync
			from  $(db).[dbo].[tags_user_group_map] SourceDBTable
			where [_create_date]>@lastSync and [_create_date] <= @thisSync and
				  NOT EXISTS(select 1
						     from [engine_history].[dbo].[tag_relations_history] DestinationDBTable
						     where SourceDBTable.[group_id] = DestinationDBTable.[entity_id]
							   	   and SourceDBTable.[tag_id] = DestinationDBTable.[parent_id]
								   and DestinationDBTable.[detach_date] IS NULL)

			/*---------------------------------------------
                                Tags Details
            ---------------------------------------------*/

			/* marks deleted*/
			update [dbo].[tag_details]
			set [_delete_date] = @thisSync
			where [_delete_date] IS NULL and [tag_id] not in (select [tag_id]
														      from $(db).[dbo].[tags])

			/* updates table */
			update [dbo].[tag_details]
			set [tag_name] = a.[tag_name],
				[tag_description] = a.[description],
				[_update_date] = a.[_update_date]
			FROM engine.[dbo].[tags] a
			where [dbo].[tag_details].[tag_id] = a.[tag_id] and a.[_update_date] > @lastSync

			/* enters new tags */
			insert into [engine_history].[dbo].[tag_details] ([tag_id],
															 [tag_name],
															 [tag_description],
															 [_create_date])
			select [tag_id],
				   [tag_name],
	               [description],
				   [_create_date]
			from  engine.[dbo].[tags]
			where [_create_date]>@lastSync and [_create_date] <= @thisSync and [tag_id] NOT IN (select [tag_id] from [dbo].[tag_details])

			/*---------------------------------------------
                        Tags Relations History
            ---------------------------------------------*/

			update [dbo].[tag_relations_history]
			set [detach_date] = @thisSync
			where [detach_date] is null and [entity_type] = @tags
			and NOT EXISTS(select 1
						   from $(db).[dbo].[tags] SourceDBTable
						   where SourceDBTable.tag_id = [dbo].[tag_relations_history].[entity_id]
								 and SourceDBTable.parent_id = [dbo].[tag_relations_history].[parent_id])

			insert into [engine_history].[dbo].[tag_relations_history]([entity_id],
																	  [parent_id],
																	  [entity_type],
																      [attach_date])
			select SourceDBTable.[tag_id],
				   SourceDBTable.[parent_id],
				   @tags,
	               @thisSync
			from  $(db).[dbo].[tags] SourceDBTable
			where ([_update_date]>@lastSync and  [_update_date] <= @thisSync) or ([_create_date]>@lastSync and [_create_date] <= @thisSync) and
   			      NOT EXISTS(select 1
						     from [engine_history].[dbo].[tag_relations_history] DestinationDBTable
						     where SourceDBTable.[tag_id] = DestinationDBTable.[entity_id]
							   	   and SourceDBTable.[parent_id] = DestinationDBTable.[parent_id]
								   and DestinationDBTable.[detach_date] IS NULL)
			order by [tag_id___old]

			/*---------------------------------------------
                            Update Path
            ---------------------------------------------*/

			insert into [dbo].[tags_path_history]([tag_id],
												  [tag_path],
												  [tag_level],
												  [attach_date])
			select [entity_id],
				   '/',
				   1,
				   @thisSync
			From   [dbo].[tag_relations_history]
			where  [attach_date] = @thisSync and [parent_id] = '00000000-0000-0000-0000-000000000000'

			UPDATE [dbo].[tags_path_history]
			SET [detach_date] = @thisSync
			Where [detach_date] IS NULL and [attach_date] != @thisSync and [tag_id] in (select a.[entity_id]
																						 from   [dbo].[tag_relations_history] a
																						 where  a.[detach_date] = @thisSync)


			/*The if check if this is the first time the ETL runs so that path initialization will run only once*/
			if (@lastSync != '1/1/2010' or @lastSync != Convert(datetime ,'01/01/2010', 103))
			BEGIN
			/*The while loop checks if all levels that changed or created of the tree where inserted, because only one level of the tree can be inserted in each rotation*/
			WHILE EXISTS(select b.[entity_id]
						 From   [dbo].[tag_relations_history] b
						 where  b.[attach_date] = @thisSync and
								b.[entity_id] NOT IN (select [tag_id] FROM [dbo].[tags_path_history] where [detach_date] IS NULL))
			BEGIN
			insert into [dbo].[tags_path_history]([tag_id],
												  [tag_path],
												  [tag_level],
												  [attach_date])
			select b.[entity_id],
				   a.[tag_path] + CAST(b.[parent_id] AS NVARCHAR(36)) + '/',
					                                          /*36 is the uuid length*/
				   a.[tag_level] + 1,
				   @thisSync
			From   [dbo].[tags_path_history] a, [dbo].[tag_relations_history] b
			where  b.[attach_date] = @thisSync and b.[entity_type] = @tags and
				   b.[parent_id] = a.[tag_id] and b.[entity_id] NOT IN (select [tag_id] FROM [dbo].[tags_path_history] where [detach_date] IS NULL)
			order by  b.[id] ASC
			END

			/*This inserts all children of the changed nodes with their new path*/
			Insert into  [dbo].[tags_path_history]([tag_id],
												  [tag_path],
												  [tag_level],
												  [attach_date])
			select distinct a.[tag_id],
				   c.[new_child_path] + ISNULL(RIGHT(a.[tag_path], LEN(a.[tag_path])-LEN(b.[old_child_path])), ''),
			       LEN(c.[new_child_path] + ISNULL(RIGHT(a.[tag_path], LEN(a.[tag_path])-LEN(b.[old_child_path])), '')) - LEN(REPLACE(c.[new_child_path] + ISNULL(RIGHT(a.[tag_path], LEN(a.[tag_path])-LEN(b.[old_child_path])), ''), '/', '')),
				   @thisSync
			FROM [dbo].[tags_path_history] a,
				 (select e.[tag_path] + CAST(e.[tag_id] AS NVARCHAR(36)) + '/' as old_child_path, e.[tag_id] as parent
					                                              /*36 is the uuid length*/
				  from [dbo].[tags_path_history] e
				  where e.[detach_date] = @thisSync) b,
				 (select [tag_path] + CAST([tag_id] AS NVARCHAR(36)) + '/' as new_child_path, [tag_id] as parent
					                                             /*36 is the uuid length*/
				  from [dbo].[tags_path_history]
				  where [attach_date] = @thisSync) c
			Where a.[tag_path] like b.[old_child_path] + '%' and
				  b.[parent] = c.[parent]
			END
			ELSE
			BEGIN
			/*This adds all the nodes of the tree that are not in the first level, because they were already added and add their parent id as there path to be used in the next section*/
			INSERT INTO [dbo].[tags_path_history]([tag_id],
												  [tag_path],
												  [tag_level],
												  [attach_date])

			select  [tag_id],
					CAST([parent_id] AS NVARCHAR(36)),
					                        /*36 is the uuid length*/
					0,
					 @thisSync

			from 	$(db).[dbo].[tags]
			WHERE	[parent_id] != '00000000-0000-0000-0000-000000000000'

			/*This while loop runs while the update changed one or more nodes and set their path*/
			DECLARE @level_counter int
			SET 	@level_counter = 1

			WHILE	EXISTS(SELECT [tag_level] FROM [dbo].[tags_path_history] WHERE [tag_level] = @level_counter)
			BEGIN
					/*Creates path from parent path*/
					UPDATE [dbo].[tags_path_history]
					SET [dbo].[tags_path_history].[tag_level] = @level_counter + 1,
						[dbo].[tags_path_history].[tag_path] = (SELECT a.[tag_path]
																FROM [dbo].[tags_path_history] a
																WHERE [dbo].[tags_path_history].[tag_path] = CAST(a.[tag_id] AS NVARCHAR(36))
					                                                                                                                /*36 is the uuid length*/
																and a.[tag_level] = @level_counter) + [dbo].[tags_path_history].[tag_path] + '/'
					WHERE [dbo].[tags_path_history].[tag_path] in (SELECT CAST([tag_id] AS NVARCHAR(36))
					                                                                          /*36 is the uuid length*/
																   FROM [dbo].[tags_path_history]
																   WHERE [tag_level] = @level_counter)

					SET @level_counter = @level_counter + 1
			END
			END

			UPDATE [dbo].[tags_path_history]
			SET [detach_date] = @thisSync
			Where [detach_date] is null and [attach_date] != @thisSync and
				  [tag_id] in (select b.[tag_id]
							   from [dbo].[tags_path_history] b
							   where b.[attach_date] = @thisSync)


		COMMIT TRAN
		END TRY
		BEGIN CATCH
			ROLLBACK TRAN
			EXEC $(db).dbo.RethrowError;
		END CATCH
END
GO

/*%%%%************************/

IF NOT EXISTS (SELECT NAME FROM sys.objects WHERE TYPE = 'P' AND NAME = 'dwh_configuration_sync')
BEGIN
	EXEC('CREATE PROCEDURE [dbo].dwh_configuration_sync AS RETURN')
END
GO

ALTER PROCEDURE [dbo].[dwh_configuration_sync] 
AS

BEGIN
	SET NOCOUNT ON;	
	BEGIN TRY
		BEGIN TRAN 
			declare @lastSync datetime
			declare @firstSync datetime
			declare @thisSync datetime
			declare @origin_count int
			declare @target_count int
			-- Force dirty-read to prevent locks on vdc database  
			SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED
			select @thisSync=getdate()
			
			select @lastSync=cast(var_value as datetime) from history_configuration where var_name='lastSync'
			if @lastSync is null
				begin
					set @lastSync='1/1/2010'
					insert history_configuration (var_name,var_value) values('lastSync', cast(@thisSync as varchar))
				end
			else
				-- we can update here since if the transaction will fail this will fail as well
				update history_configuration set var_value = cast(@thisSync as varchar)	where var_name='lastSync'

			select @firstSync=cast(var_value as datetime) from history_configuration where var_name='firstSync'
			if @firstSync is null
				begin
					insert history_configuration (var_name,var_value) values('firstSync', cast(@thisSync as varchar))
				end

			/*---------------------------------------------
                                VM
            ---------------------------------------------*/

			--insert
             
			insert into [vm_configuration]		
			SELECT a.[vm_guid]
				  ,a.[vm_name]
				  ,a.[vmt_guid]
				  ,a.[os]
				  ,a.[description]
				  ,a.[domain]
				  ,a.[creation_date]
				  ,a.[is_initialized]
				  ,a.[is_auto_suspend]
				  ,a.[usb_policy]
				  ,a.[time_zone]
				  ,a.[is_stateless]
				  ,a.[fail_back]
				  ,a.[auto_startup]
                  ,a.[priority]
				  ,a.[_create_date]
				  ,a.[_update_date]
				  ,NULL
				  ,a.[vds_group_id]
				  ,a.[dedicated_vm_for_vds]
				  ,b.[name] template_name
				  ,a.[mem_size_mb]
				  ,a.[cpu_per_socket]
				  ,a.[num_of_sockets]
				  ,a.[vm_type]
			  FROM  $(db).dbo.vm_static a, $(db).dbo.vm_templates b
			  WHERE a._create_date>@lastSync and a._create_date<=@thisSync and
                a.vmt_guid = b.vmt_guid and
				a.vm_guid not in (select vm_guid from vm_configuration) 

			-- update
			update [vm_configuration]
			set
				[vm_name] = a.[vm_name],
				[os] = a.[os],
				[description] = a.[description],
				[domain] = a.[domain],
				[vds_group_id] = a.[vds_group_id],
				[is_initialized] = a.[is_initialized],
				[is_auto_suspend] = a.[is_auto_suspend],
				[usb_policy] = a.[usb_policy],
				[time_zone] = a.[time_zone],
				[is_stateless] = a.[is_stateless],
				[fail_back] = a.[fail_back],
				[dedicated_vm_for_vds] = a.[dedicated_vm_for_vds],
				[auto_startup] = a.[auto_startup],
				[priority] = a.[priority],
				[template_name] = b.[name],
				[mem_size_mb] = a.[mem_size_mb],
				[cpu_per_socket] = a.[cpu_per_socket],
				[num_of_sockets] = a.[num_of_sockets],
				[vm_type] = a.[vm_type]

			from [$(db)].dbo.[vm_static] a, [$(db)].dbo.[vm_templates] b
			WHERE a._update_date>@lastSync and a._update_date<=@thisSync and
				a.vmt_guid = b.vmt_guid and
				[vm_configuration].[vm_guid] = a.vm_guid 
			
			-- Delete

			select @origin_count = count(*) from $(db).dbo.vm_static
			select @target_count = count(*) from vm_configuration where _delete_date is	null
			if @origin_count < @target_count
					update vm_configuration
					set _delete_date= @thisSync
					where vm_configuration._delete_date is null and
						  vm_configuration.vm_guid not in (select vm_guid from	$(db).dbo.vm_static)

			/*---------------------------------------------
                                VDS
            ---------------------------------------------*/

			--insert
			insert into [vds_configuration]
			SELECT   a.[vds_id]
				,a.[vds_name]
				,a.[ip] 
				,a.[vds_unique_id]
				,a.[host_name]
				,a.[port]
				,a.[vds_group_id]
				,a.[vds_type]
                ,b.[cpu_flags]
                ,a.[vds_id___old]
				,a.[_create_date]
				,a.[_update_date]				
				,NULL
				,b.[host_os]
				,b.[kernel_version] 
				,b.[kvm_version] 
    			,b.[software_version] 
				,b.[cpu_model] 
				,b.[physical_mem_mb]
    			,b.[cpu_cores] 
			  FROM  $(db).dbo.vds_static a, $(db).dbo.vds_dynamic b
			  WHERE a.vds_id = b.vds_id and 
                a._create_date>@lastSync and a._create_date<=@thisSync and
				a.vds_id not in (select vds_id from vds_configuration)

			--update

			update [vds_configuration]
			set
				[vds_id___old] = a.[vds_id___old],
				[vds_name] = a.[vds_name],
				[pm_ip_address] = a.[ip],
				[vds_unique_id] = a.[vds_unique_id],
				[host_name] = a.[host_name],
				[port] = a.[port],
				[vds_group_id] = a.[vds_group_id],
				[vds_type] = a.[vds_type],
                [cpu_flags] = b.[cpu_flags],
				[_create_date] = a.[_create_date],
				[_update_date] = a.[_update_date],
				[host_os] = b.[host_os],
				[kernel_version] = b.[kernel_version], 
				[kvm_version] = b.[kvm_version], 
    			[software_version] = b.[software_version], 
				[cpu_model] = b.[cpu_model], 
				[physical_mem_mb] = b.[physical_mem_mb],
    			[cpu_cores] = b.[cpu_cores] 
			from [$(db)].dbo.[vds_static] a, $(db).dbo.vds_dynamic b
			WHERE   a.vds_id = b.vds_id and 
				(a._update_date>@lastSync and a._update_date<=@thisSync or 
				b._update_date>@lastSync and b._update_date<=@thisSync) and
				[vds_configuration].[vds_id] = a.vds_id 

			-- Delete

			select @origin_count = count(*) from $(db).dbo.vds_static
			select @target_count = count(*) from vds_configuration where _delete_date is	null
			if @origin_count < @target_count
					update vds_configuration
					set _delete_date= @thisSync
					where vds_configuration._delete_date is null and
						  vds_configuration.vds_id not in (select vds_id from $(db).dbo.vds_static)

			/*---------------------------------------------
                                VDS GROUPS
            ---------------------------------------------*/

			--insert

			insert into [vds_group_configuration]
			SELECT   [vds_group_id]
				,[name]
				,[description] 
				,[cpu_name]
                ,[storage_pool_id]
				,[compatibility_version]
				,[vds_group_id___old]
				,[_create_date]
				,[_update_date]
				,NULL
			  FROM  $(db).dbo.vds_groups a
			  WHERE a._create_date>@lastSync and a._create_date<=@thisSync and
				a.vds_group_id not in (select vds_group_id from vds_group_configuration)

			--update

			update [vds_group_configuration]
			set
				[name] = a.[name],
				[description] = a.[description],
				[cpu_name] = a.[cpu_name],
                [storage_pool_id] = a.[storage_pool_id],
                [compatibility_version] = a.[compatibility_version],
                [vds_group_id___old] = a.[vds_group_id___old],
				[_create_date] = a.[_create_date],
				[_update_date] = a.[_update_date]
			from [$(db)].dbo.[vds_groups] a
			WHERE a._update_date>@lastSync and a._update_date<=@thisSync and
				[vds_group_configuration].[vds_group_id] = a.vds_group_id 

			-- Delete

			select @origin_count = count(*) from $(db).dbo.vds_groups
			select @target_count = count(*) from vds_group_configuration where _delete_date is	null
			if @origin_count < @target_count
					update vds_group_configuration
					set _delete_date= @thisSync
					where vds_group_configuration._delete_date is null and
					      vds_group_configuration.vds_group_id not in (select vds_group_id from	$(db).dbo.vds_groups)

			/*---------------------------------------------
                                VM Interface
            ---------------------------------------------*/
			
			--Insert
			insert into [vm_interface_configuration]
			SELECT [id]
				,[vm_guid]
				,[mac_addr] 
				,[name]
				,[network_name]
				,[_create_date]
				,[_update_date]
				,NULL
			  FROM  $(db).dbo.vm_interface a
			  WHERE a._create_date>@lastSync and a._create_date<=@thisSync and
				a.id not in (select id from vm_interface_configuration)

			--update

			update [vm_interface_configuration]
			set [id] = a.[id],
				[vm_guid] = a.[vm_guid],
				[mac_addr] = a.[mac_addr],
				[name] = a.[name],
                [network_name] = a.[network_name],
				[_create_date] = a.[_create_date],
				[_update_date] = a.[_update_date]
			from [$(db)].dbo.[vm_interface] a
			WHERE a._update_date>@lastSync and a._update_date<=@thisSync and
				[vm_interface_configuration].[id] = a.id 

			-- Delete

			select @origin_count = count(*) from $(db).dbo.vm_interface
			select @target_count = count(*) from vm_interface_configuration where _delete_date is	null
			if @origin_count < @target_count
					update vm_interface_configuration
					set _delete_date= @thisSync
					where vm_interface_configuration._delete_date is null and
						  vm_interface_configuration.id not in (select id from	$(db).dbo.vm_interface)

			/*---------------------------------------------
                                VDS Interface
            ---------------------------------------------*/
			
			--Insert
			insert into [vds_interface_configuration]
			(id, vds_id, mac_addr, name, network_name, is_bond, bond_name, bond_type, vlan_id, addr, gateway, type, _create_date, _update_date, _delete_date)
			SELECT [id]                  
				,[vds_id]
				,[mac_addr] 
				,[name]
				,[network_name]
			    ,[is_bond]
				,[bond_name]
				,[bond_type]
				,[vlan_id]
				,[addr]
				,[gateway]
				,[type]
				,[_create_date]
				,[_update_date]
				,NULL
			  FROM  $(db).dbo.vds_interface a
			  WHERE a._create_date>@lastSync and a._create_date<=@thisSync and
				a.id not in (select id from vds_interface_configuration)
				and a.is_bond is null or ( a.is_bond = 1 and a.name in(							 
						  SELECT   bond_name
						  FROM $(db).dbo.vds_interface AS b
						  where b.is_bond is null and b.vds_id = a.vds_id))


			--update

			update [vds_interface_configuration]
			set
				[id] = a.[id],
				[vds_id] = a.[vds_id],
				[mac_addr] = a.[mac_addr],
				[name] = a.[name],
                [network_name] = a.[network_name],
				[is_bond] = a.[is_bond],
				[bond_name] = a.[bond_name],
				[bond_type] = a.[bond_type],
				[vlan_id] = a.[vlan_id],
				[addr] = a.[addr],
				[gateway] = a.[gateway],
				[type] = a.[type],
				[_create_date] = a.[_create_date],
				[_update_date] = a.[_update_date]
			from [$(db)].dbo.[vds_interface] a
			WHERE a._update_date>@lastSync and a._update_date<=@thisSync and
				[vds_interface_configuration].[id] = a.id 
				and a.is_bond is null or ( a.is_bond = 1 and a.name in(							 
						  SELECT   bond_name
						  FROM $(db).dbo.vds_interface AS b
						  where b.is_bond is null and b.vds_id = a.vds_id))

			-- Delete

			select @origin_count = count(*) from $(db).dbo.vds_interface
			select @target_count = count(*) from vds_interface_configuration where _delete_date is	null
			if @origin_count < @target_count
					update vds_interface_configuration
					set _delete_date= @thisSync
					where vds_interface_configuration.id not in (select id from	$(db).dbo.vds_interface)

			/*---------------------------------------------
                                VM Disks
            ---------------------------------------------*/
			insert into [vm_disk_configuration]
			SELECT  [image_guid] ,
					[creation_date],
					[description],
					[internal_drive_mapping],
					[volume_format],
					[volume_type] ,
				    [_create_date],
				    [_update_date],
					NULL
			  FROM  $(db).dbo.images a
			  WHERE a._create_date>@lastSync and a._create_date<=@thisSync and
				a.image_guid not in (select image_guid from vm_disk_configuration)

			--update

			update [vm_disk_configuration]
			set
				[image_guid] = a. [image_guid],
				[description] = a.[description],
				[internal_drive_mapping] = a.[internal_drive_mapping],
                [volume_format] = a.[volume_format],
				[volume_type] = a.[volume_type],
				[_create_date] = a.[_create_date],
				[_update_date] = a.[_update_date]
			from [$(db)].dbo.[images] a
			WHERE a._update_date>@lastSync and a._update_date<=@thisSync and
				[vm_disk_configuration].[image_guid] = a.image_guid 

			-- Delete

			select @origin_count = count(*) from $(db).dbo.images
			select @target_count = count(*) from vm_disk_configuration where _delete_date is null
			if @origin_count < @target_count
					update vm_disk_configuration
					set _delete_date= @thisSync
					where vm_disk_configuration._delete_date is null and
						  vm_disk_configuration.image_guid not in (select image_guid from	$(db).dbo.images)

			/*---------------------------------------------
                                Data Center
            ---------------------------------------------*/
			insert into [datacenter_configuration]
			SELECT  [id] datacenter_id,
					[name],
					[description],
					[storage_pool_type],
				    [_create_date],
				    [_update_date],
					NULL
			  FROM  $(db).dbo.storage_pool a
			  WHERE a._create_date>@lastSync and a._create_date<=@thisSync and
				a.id not in (select datacenter_id from datacenter_configuration)

			--update

			update [datacenter_configuration]
			set
				[datacenter_id] = a.[id],
				[name] = a.[name],
				[description] = a.[description],
				[storage_pool_type] = a.[storage_pool_type],
				[_create_date] = a.[_create_date],
				[_update_date] = a.[_update_date]
			from [$(db)].dbo.[storage_pool] a
			WHERE a._update_date>@lastSync and a._update_date<=@thisSync and
				[datacenter_configuration].[datacenter_id] = a.id 

			-- Delete

			select @origin_count = count(*) from $(db).dbo.storage_pool
			select @target_count = count(*) from datacenter_configuration where _delete_date is null
			if @origin_count < @target_count
					update datacenter_configuration
					set _delete_date= @thisSync
					where datacenter_configuration._delete_date is null and
						  datacenter_configuration.datacenter_id not in (select id from	$(db).dbo.storage_pool)

			/*---------------------------------------------
                                Storage Domain
            ---------------------------------------------*/
			insert into [storage_domain_configuration]
			SELECT  [id] storage_domain_id,
					[storage_name],
					[storage_domain_type],
					[storage_type],
					[_create_date],
					[_update_date],
					NULL
			  FROM  $(db).dbo.storage_domain_static a
			  WHERE a._create_date>@lastSync and a._create_date<=@thisSync and
				a.id not in (select storage_domain_id from storage_domain_configuration)

			--update

			update [storage_domain_configuration]
			set
				[storage_domain_id] = a.[id],
				[storage_name] = a.[storage_name],
				[storage_domain_type] = a.[storage_domain_type],
				[_create_date] = a.[_create_date],
				[_update_date] = a.[_update_date]
			from [$(db)].dbo.[storage_domain_static] a
			WHERE a._update_date>@lastSync and a._update_date<=@thisSync and
				[storage_domain_configuration].[storage_domain_id] = a.id 

			-- Delete

			select @origin_count = count(*) from $(db).dbo.storage_domain_static
			select @target_count = count(*) from storage_domain_configuration where _delete_date is null
			if @origin_count < @target_count
					update storage_domain_configuration
					set _delete_date= @thisSync
					where storage_domain_configuration._delete_date is null and
						  storage_domain_configuration.storage_domain_id not in (select id from	$(db).dbo.storage_domain_static)

			/*---------------------------------------------
                   Datacenter and Storage Domain map
            ---------------------------------------------*/
			-- insert 
			-- the query here is different from the othere queries in the procedure because
			-- the primery key is made of two columns. 
			insert into
				[datacenter_storage_domain_map]
				(storage_id,datacenter_id,attach_date)
			select
				storage_id, storage_pool_id,@thisSync
			from
				$(db).dbo.storage_pool_iso_map SourceDBTable
			where
			not exists
			(select
				1
			 from
				[datacenter_storage_domain_map] DestinationDBTable
			 where
				DestinationDBTable.storage_id = SourceDBTable.storage_id and
				DestinationDBTable.datacenter_id = SourceDBTable.storage_pool_id and
				DestinationDBTable.detach_date is null
				)
			-- update 
			-- no update is needed because this table is only a map (all columns are part of the primery key)
			-- in case this changes (status column is added for example) an update query will be needed.
			
			
			-- delete
			update
				[datacenter_storage_domain_map] 
			set 
				detach_date = @thisSync
			where
				not exists
				(select
					1
				 from
					$(db).dbo.storage_pool_iso_map SourceDBTable
				 where
					SourceDBTable.storage_id  = [datacenter_storage_domain_map].storage_id and
					SourceDBTable.storage_pool_id  = [datacenter_storage_domain_map].datacenter_id and
					[datacenter_storage_domain_map].detach_date is null
				)  
			EXEC [dbo].[update_tags_tables] @lastSync, @thisSync
		COMMIT TRAN 
	END TRY
	BEGIN CATCH
		ROLLBACK TRAN 
		EXEC $(db).dbo.RethrowError;
	END CATCH
END
GO

/*%%%%************************/


IF NOT EXISTS (SELECT NAME FROM sys.objects WHERE TYPE = 'P' AND NAME = 'dwh_history_add')
BEGIN
	EXEC('CREATE PROCEDURE [dbo].dwh_history_add AS RETURN')
END
GO

ALTER PROCEDURE [dbo].[dwh_history_add]
AS
BEGIN
	BEGIN TRY
	 declare @BASE_LEVEL tinyint
	 declare @history_datetime datetime 
		-- Force dirty-read to prevent locks on vdc database  
 		SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED
		BEGIN TRAN
		 set @BASE_LEVEL=0 		 
		 set @history_datetime = getdate()
			insert into vm_history
		  select @BASE_LEVEL,@history_datetime, *,1,usage_mem_percent,usage_cpu_percent from $(db).dbo.dwh_vm_history_view 
		  	insert into vds_history
		  select @BASE_LEVEL, @history_datetime, *,1,usage_mem_percent,usage_cpu_percent from $(db).dbo.dwh_vds_history_view 
			insert into vm_interface_history
		  select @BASE_LEVEL, @history_datetime, *,rx_rate,tx_rate from $(db).dbo.dwh_vm_interface_history_view 
			insert into vds_interface_history
		  select @BASE_LEVEL, @history_datetime, *,rx_rate,tx_rate from $(db).dbo.dwh_vds_interface_history_view 
			insert into vm_disk_history
		  select @BASE_LEVEL, @history_datetime, *,read_rate, write_rate from $(db).dbo.dwh_vm_disks_history_view 
			insert into storage_domain_history
		  select @BASE_LEVEL, @history_datetime, * from $(db).dbo.dwh_storage_domain_history_view
			insert into datacenter_history
		  select @BASE_LEVEL, @history_datetime, *,1 from $(db).dbo.dwh_data_center_history_view

		COMMIT TRAN
	END TRY

	BEGIN CATCH
		ROLLBACK TRAN 
		EXEC $(db).dbo.RethrowError;
	END CATCH
END

GO

IF NOT EXISTS (SELECT NAME FROM sys.objects WHERE TYPE = 'P' AND NAME = 'dwh_history_aggregate_level')
BEGIN
	EXEC('CREATE PROCEDURE [dbo].dwh_history_aggregate_level AS RETURN')
END
GO

ALTER PROCEDURE [dbo].[dwh_history_aggregate_level] (
	@Aggregation_level tinyint, @AggrStart datetime, @thisAggr datetime, @AggrGap smallint
)
AS
BEGIN
	BEGIN TRY
		-- logic is implemented in a loop, to allow closing gaps. in most cases, the loop will occur only once
		declare @AggrEnd datetime
        declare @level_to_aggregate smallint
		----IF ($(debug) = 'true')
		----BEGIN	
			print 'dwh_history_aggregate_level - start'
			print '@Aggregation_level: ' + cast(@Aggregation_level as varchar(50))
			print '@AggrGap: ' + cast(@AggrGap as varchar(50))
		----END
		set @AggrStart = dateadd(hh, @AggrGap, @AggrStart)
		while @AggrStart < @thisAggr 
		begin
			BEGIN TRAN 
				set @AggrEnd = dateadd(hh, @AggrGap, @AggrStart)
				----IF ($(debug) = 'true')
				----BEGIN	
					print 'dwh_history_aggregate_level - in while loop'
					print '@AggrStart: ' + cast(@AggrStart as varchar(50))
					print '@AggrEnd: ' + cast(@AggrEnd as varchar(50))
					print '@thisAggr: ' + cast(@thisAggr as varchar(50))
				----END
				-- move the start to next hour/day based on @AggrGap (which is in hours)
				-- (we get @AggrStart which is the end of the last sync, so we need to incremenet by one.
				-- using query instead of view to allow using parameters so won't need to complex/duplicate the queries for different 
				-- time ranges

				--update last day/hour configuration settings
				declare @BASE_LEVEL tinyint; set @BASE_LEVEL=0
				declare @HOUR_LEVEL tinyint; set @HOUR_LEVEL=2
				declare @DAY_LEVEL tinyint; set @DAY_LEVEL=3
				if (@Aggregation_level = @DAY_LEVEL)
				begin
					update history_configuration set var_value = cast(@AggrEnd as varchar)	where var_name='lastDayAggr'
					set @level_to_aggregate = @HOUR_LEVEL
				end
				else if (@Aggregation_level = @HOUR_LEVEL)
				begin
					update history_configuration set var_value = cast(@AggrEnd as varchar)	where var_name='lastHourAggr'
					set @level_to_aggregate = @BASE_LEVEL
				end

				--VM
				print 'updating vm_history...'
				insert into vm_history 
				(aggregation_level,history_datetime,vm_guid,vds_group_id,dedicated_vm_for_vds, status, mem_size_mb, num_of_monitors,
				 num_of_sockets, num_of_cpus, vm_last_up_time, vm_last_boot_time, guest_os, vm_ip, run_on_vds, guest_cur_user_name,
				 usage_mem_percent, usage_cpu_percent, time_in_status, max_usage_mem, max_usage_cpu)
					select @Aggregation_level, @AggrStart, 
							  vm_guid, vds_group_id, NULL, status, AVG(mem_size_mb) AS mem_size_mb_avg, MAX(num_of_monitors) AS num_of_monitors_max, 
							  MAX(num_of_sockets) as num_of_sockets, MAX(num_of_cpus) AS num_of_cpus_max, MAX(vm_last_up_time) AS vm_last_up_time_max, 
							  MAX(vm_last_boot_time) AS vm_last_boot_time_max, MAX(guest_os) AS guest_os_max, NULL,run_on_vds,NULL,  
							  AVG(usage_mem_percent) AS usage_mem_percent_avg, AVG(usage_cpu_percent) AS usage_cpu_percent_avg, SUM(time_in_status),
							  MAX(usage_mem_percent) AS usage_mem_percent_max, MAX(usage_cpu_percent) AS usage_cpu_percent_max
	                          
					FROM         dbo.vm_history
					WHERE history_datetime>=@AggrStart and history_datetime<@AggrEnd and aggregation_level = @level_to_aggregate
					GROUP BY vm_guid, vds_group_id, [status],run_on_vds
				
				--VDS
				print 'updating vds_history...'
				insert into vds_history 
				(aggregation_level, history_datetime, status, software_version, cpu_cores, physical_mem_mb, vm_active,
				 vm_count, usage_mem_percent, usage_cpu_percent, vms_cores_count, ksm_cpu_percent,vds_id, time_in_status,
				 max_usage_mem, max_usage_cpu)
					select @Aggregation_level, @AggrStart,
							  status, software_version, MAX(cpu_cores) AS cpu_cores_max, 
							  AVG(physical_mem_mb) AS physical_mem_mb_avg, AVG(vm_active) 
							  AS vm_active_avg, AVG(vm_count) AS vm_count_avg,  
							  AVG(usage_mem_percent) AS usage_mem_percent_avg, 
							  AVG(usage_cpu_percent) AS usage_cpu_percent_avg,   
							  MAX(vms_cores_count),AVG(ksm_cpu_percent) as ksm_cpu_percent,
							  vds_id, SUM(time_in_status),
							  MAX(usage_mem_percent) AS usage_mem_percent_max, MAX(usage_cpu_percent) AS usage_cpu_percent_max
							  
					FROM		dbo.vds_history
					WHERE history_datetime>=@AggrStart and history_datetime<@AggrEnd and aggregation_level = @level_to_aggregate
					GROUP BY vds_id, status, software_version
				
				--VM Interface
				print 'updating vm_interface_history...'
				insert into vm_interface_history 
					select @Aggregation_level, @AggrStart,
					interface_id, vm_guid,  
					AVG(rx_rate) AS rx_rate_avg, AVG(tx_rate) AS tx_rate_avg,
					AVG(speed) AS speed , [type] as [type],
					MAX(rx_rate) AS rx_rate_max, MAX(tx_rate) AS tx_rate_max
					FROM		dbo.vm_interface_history
					WHERE history_datetime>=@AggrStart and history_datetime<@AggrEnd and aggregation_level = @level_to_aggregate
					GROUP BY vm_guid,interface_id,[type]
				
				--VDS Interface
				print 'updating vds_interface_history...'
				insert into vds_interface_history 
				(aggregation_level, history_datetime, interface_id, vds_id, rx_rate, tx_rate, speed, rx_rate_max, tx_rate_max)
					select @Aggregation_level, @AggrStart,
					interface_id, vds_id,
					AVG(rx_rate) AS rx_rate_avg, AVG(tx_rate) AS tx_rate_avg,
					AVG(speed) as speed,
					MAX(rx_rate) AS rx_rate_max, MAX(tx_rate) AS tx_rate_max
					FROM		dbo.vds_interface_history
					WHERE history_datetime>=@AggrStart and history_datetime<@AggrEnd and aggregation_level = @level_to_aggregate
					GROUP BY vds_id,interface_id
				
				--VM Disks
				print 'updating vm_disk_history...'
				insert into vm_disk_history 
					select @Aggregation_level, @AggrStart,
					vm_guid, image_guid,MAX(actual_size) as actual_size_max,
					MAX(size) as size,disk_interface,disk_type,imageStatus,
					AVG(read_rate) AS read_rate_avg, AVG(write_rate) AS write_rate_avg, storage_id,
					MAX(read_rate) AS read_rate_max, MAX(write_rate) AS write_rate_max
					FROM		dbo.vm_disk_history
					WHERE history_datetime>=@AggrStart and history_datetime<@AggrEnd and aggregation_level = @level_to_aggregate
					GROUP BY vm_guid, image_guid,imageStatus,disk_interface,disk_type, storage_id

				--Datacenter
				print 'updating datacenter_history...'
				insert into datacenter_history 
					select @Aggregation_level, @AggrStart,
					datacenter_id, [status], SUM (time_in_status)
					FROM		dbo.datacenter_history
					WHERE history_datetime>=@AggrStart and history_datetime<@AggrEnd and aggregation_level = @level_to_aggregate
					GROUP BY datacenter_id, [status]
				
				--Storage Domain
				print 'updating storage_domain_history...'
				insert into storage_domain_history 
					select @Aggregation_level, @AggrStart,
					storage_domain_id, MAX(available_disk_size), MAX(used_disk_size)
					FROM		dbo.storage_domain_history
					WHERE history_datetime>=@AggrStart and history_datetime<@AggrEnd and aggregation_level = @level_to_aggregate
					GROUP BY storage_domain_id

				set @AggrStart = @AggrEnd
			COMMIT TRAN 
		end
	END TRY
	BEGIN CATCH
		ROLLBACK TRAN 
		DECLARE 
			@ErrorMessage    NVARCHAR(4000),
			@ErrorNumber     INT,
			@ErrorSeverity   INT,
			@ErrorState      INT,
			@ErrorLine       INT,
			@ErrorProcedure  NVARCHAR(200);

		-- Assign variables to error-handling functions that 
		-- capture information for RAISERROR.
		SELECT 
			@ErrorNumber = ERROR_NUMBER(),
			@ErrorSeverity = ERROR_SEVERITY(),
			@ErrorState = ERROR_STATE(),
			@ErrorLine = ERROR_LINE(),
			@ErrorProcedure = ISNULL(ERROR_PROCEDURE(), '-');

		-- Build the message string that will contain original
		-- error information.
		SELECT @ErrorMessage = 
			N'Error %d, Level %d, State %d, Procedure %s, Line %d, ' + 
				'Message: '+ ERROR_MESSAGE();

		-- Raise an error: msg_str parameter of RAISERROR will contain
		-- the original error information.
		RAISERROR  -- return control to [dwh_history_aggregate] CATCH block.
			(
			@ErrorMessage, 
			@ErrorSeverity, 
			1,               
			@ErrorNumber,    -- parameter: original error number.
			@ErrorSeverity,  -- parameter: original error severity.
			@ErrorState,     -- parameter: original error state.
			@ErrorProcedure, -- parameter: original error procedure name.
			@ErrorLine       -- parameter: original error line number.
			);
	END CATCH
END
GO

IF NOT EXISTS (SELECT NAME FROM sys.objects WHERE TYPE = 'P' AND NAME = 'dwh_history_delete')
BEGIN
	EXEC('CREATE PROCEDURE [dbo].dwh_history_delete AS RETURN')
END
GO

ALTER PROCEDURE [dbo].[dwh_history_delete] (
	@Aggregation_level tinyint, @LastDateToKeep datetime, @DelStart datetime, @DelGap smallint
)
AS
BEGIN
	SET NOCOUNT ON;
	BEGIN TRY
		declare @DelEnd datetime
		while @DelStart < @LastDateToKeep
		begin
			BEGIN TRAN 
				set @DelEnd = dateadd(hh, @DelGap, @DelStart) 
				delete from vm_history where history_datetime>=@DelStart and history_datetime<@DelEnd and aggregation_level = @Aggregation_level
				delete from vm_disk_history where history_datetime>=@DelStart and history_datetime<@DelEnd and aggregation_level = @Aggregation_level
				delete from vds_interface_history where history_datetime>=@DelStart and history_datetime<@DelEnd and aggregation_level = @Aggregation_level
				delete from vm_interface_history where history_datetime>=@DelStart and history_datetime<@DelEnd and aggregation_level = @Aggregation_level
				delete from vds_history where history_datetime>=@DelStart and history_datetime<@DelEnd and aggregation_level = @Aggregation_level
				delete from storage_domain_history where history_datetime>=@DelStart and history_datetime<@DelEnd and aggregation_level = @Aggregation_level
				delete from datacenter_history where history_datetime>=@DelStart and history_datetime<@DelEnd and aggregation_level = @Aggregation_level
				set @DelStart = @DelEnd

				--update last day/hour configuration settings
				declare @BASE_LEVEL tinyint; set @BASE_LEVEL=0
				declare @HOUR_LEVEL tinyint; set @HOUR_LEVEL=2
				declare @DAY_LEVEL tinyint; set @DAY_LEVEL=3
				if (@Aggregation_level = @DAY_LEVEL)
				begin
					update history_configuration set var_value = cast(@DelEnd as varchar)	where var_name='lastDayDel'
				end
				else if (@Aggregation_level = @HOUR_LEVEL)
				begin
					update history_configuration set var_value = cast(@DelEnd as varchar)	where var_name='lastHourDel'
				end
				else if (@Aggregation_level = @BASE_LEVEL)
				begin
					update history_configuration set var_value = cast(@DelEnd as varchar)	where var_name='lastSampleDel'
				end
			COMMIT TRAN
		end
	END TRY
	BEGIN CATCH
		ROLLBACK TRAN
		DECLARE 
			@ErrorMessage    NVARCHAR(4000),
			@ErrorNumber     INT,
			@ErrorSeverity   INT,
			@ErrorState      INT,
			@ErrorLine       INT,
			@ErrorProcedure  NVARCHAR(200);

		-- Assign variables to error-handling functions that 
		-- capture information for RAISERROR.
		SELECT 
			@ErrorNumber = ERROR_NUMBER(),
			@ErrorSeverity = ERROR_SEVERITY(),
			@ErrorState = ERROR_STATE(),
			@ErrorLine = ERROR_LINE(),
			@ErrorProcedure = ISNULL(ERROR_PROCEDURE(), '-');

		-- Build the message string that will contain original
		-- error information.
		SELECT @ErrorMessage = 
			N'Error %d, Level %d, State %d, Procedure %s, Line %d, ' + 
				'Message: '+ ERROR_MESSAGE();

		-- Raise an error: msg_str parameter of RAISERROR will contain
		-- the original error information.
		RAISERROR  -- return control to [dwh_history_aggregate] CATCH block.
			(
			@ErrorMessage, 
			@ErrorSeverity, 
			1,               
			@ErrorNumber,    -- parameter: original error number.
			@ErrorSeverity,  -- parameter: original error severity.
			@ErrorState,     -- parameter: original error state.
			@ErrorProcedure, -- parameter: original error procedure name.
			@ErrorLine       -- parameter: original error line number.
			); 
	END CATCH
END
GO

IF NOT EXISTS (SELECT NAME FROM sys.objects WHERE TYPE = 'P' AND NAME = 'dwh_history_aggregate')
BEGIN
	EXEC('CREATE PROCEDURE [dbo].dwh_history_aggregate AS RETURN')
END
GO

ALTER PROCEDURE [dbo].[dwh_history_aggregate]
AS
BEGIN
	SET NOCOUNT ON;
	BEGIN TRY

		-- aggregation logic is to aggregate by days, until the day before yesterday (if this is tuesday, we can aggregate sunday).
		-- we need monday at hourly level, so we can show last 24 hours until midnight of tuesday.
		-- same for hourly level - if it's 16:00, we can aggregate 14:00, but must keep 15:01... to show last 60 minutes

		declare @BASE_LEVEL tinyint; set @BASE_LEVEL=0
		declare @MINUTE_LEVEL tinyint; set @MINUTE_LEVEL=1
		declare @HOUR_LEVEL tinyint; set @HOUR_LEVEL=2
		declare @DAY_LEVEL tinyint; set @DAY_LEVEL=3
		declare @START_DATE datetime; set @START_DATE='1/1/2010'

		declare @GAP_DAY smallint; set @GAP_DAY = 24 -- 24 hours in a day 
		declare @GAP_HOUR smallint; set @GAP_HOUR = 1 -- 1 hour

		declare @Aggregation_level tinyint
		declare @Gap smallint 
		declare @AggrStart datetime		-- helper to use in loop when closing aggregation gaps

		declare @RoundDateTime datetime; set @RoundDateTime = cast(getdate() as smalldatetime)
		set @RoundDateTime = dateadd(mi, -datepart(mi, @RoundDateTime), @RoundDateTime) -- convert to hour level
		
		-- Force dirty-read to prevent locks on vdc database  
		SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED	

		---------------------- hourly aggregations -----------------------
		-- use hour level granularity, reduce 1 hour (at 13:30, we will only aggregate until 12:00)
		declare @thisHourAggr datetime	-- the current hour to aggragate to (or close gap to)
		select @thisHourAggr=Dateadd(hh, -@GAP_HOUR, @RoundDateTime)
		declare @lastHourAggr datetime	-- the last hour that was aggregated
		select @lastHourAggr=cast(var_value as datetime) from history_configuration where var_name='lastHourAggr'
		if @lastHourAggr is null begin
			set @lastHourAggr=@START_DATE
			insert history_configuration (var_name,var_value) values('lastHourAggr', cast(@thisHourAggr as varchar))
		end     
		
		print '@thisHourAggr: ' + cast(@thisHourAggr as varchar(50))
		print '@lastHourAggr: ' + cast (@lastHourAggr  as varchar(50))
	
		if @thisHourAggr>@lastHourAggr begin
			print 'performing hour aggregations'
			set @Aggregation_level = @HOUR_LEVEL
			set @Gap = @GAP_HOUR
			set @AggrStart = dateadd(hh, -@Gap, @lastHourAggr)
			exec dwh_history_aggregate_level @Aggregation_level, @AggrStart, @thisHourAggr, @Gap
		end	

		----------------------- daily aggregations ------------------------
		-- zero hours to get day granularity, then reduce 24 hours
		declare @thisDayAggr datetime	-- the current day to aggregate (or close gap to)
		select @thisDayAggr=Dateadd(hh, -@GAP_DAY, dateadd(hh,-datepart(hh,@RoundDateTime),@RoundDateTime))
		declare @lastDayAggr datetime	-- the last day that was aggregated
		select @lastDayAggr=cast(var_value as datetime) from history_configuration where var_name='lastDayAggr'
		if @lastDayAggr is null begin
			set @lastDayAggr=@START_DATE
			insert history_configuration (var_name,var_value) values('lastDayAggr', cast(@thisDayAggr as varchar))
		end
		
		print '@thisDayAggr: ' + cast(@thisDayAggr as varchar(50))
		print '@lastDayAggr: ' + cast(@lastDayAggr as varchar(50))
	
		if @thisDayAggr > @lastDayAggr begin
			print 'performing day aggregations'
			set @Aggregation_level = @DAY_LEVEL
			set @Gap = @GAP_DAY
			set @AggrStart = dateadd(hh, -@Gap, @lastDayAggr)
			exec dwh_history_aggregate_level @Aggregation_level, @AggrStart, @thisDayAggr, @Gap
		end

		----------------------- sampling delete ----------------------
		declare @lastSampleDel datetime   -- the last sample that was deleted
		select @lastSampleDel=cast(var_value as datetime) from history_configuration where var_name='lastSampleDel'
		if @lastSampleDel is null begin
			set @lastSampleDel=@START_DATE
			insert history_configuration (var_name,var_value) values('lastSampleDel', cast(@lastSampleDel as varchar))
		end

		-- read the time to keep history data from configuration table
		declare @samplesToKeep smallint; 
		select @samplesToKeep=cast(var_value as smallint) from history_configuration where var_name='samplesToKeep'
		if @samplesToKeep is null begin
			set @samplesToKeep=2 -- keep two hours of sampling
			insert history_configuration (var_name,var_value) values('samplesToKeep', cast(@samplesToKeep as varchar))
		end	

		declare @LastSampleToKeep datetime -- the oldest data to keep in hour level aggregation
		set @LastSampleToKeep = dateadd(hh, -@samplesToKeep, getdate())
		if @LastSampleToKeep > @lastSampleDel begin
			print 'performing hour detele'	
			set @Aggregation_level=@BASE_LEVEL
			set @Gap = @GAP_HOUR
			exec dwh_history_delete @Aggregation_level, @LastSampleToKeep, @lastSampleDel, @Gap
		end
		
		----------------------- hourly delete ----------------------
		declare @lastHourDel datetime   -- the last hour that was deleted
		select @lastHourDel=cast(var_value as datetime) from history_configuration where var_name='lastHourDel'
		if @lastHourDel is null begin
			set @lastHourDel=@START_DATE
			insert history_configuration (var_name,var_value) values('lastHourDel', cast(@lastHourDel as varchar))
		end

		-- read the time to keep history data from configuration table
		declare @hoursToKeep smallint; 
		select @hoursToKeep=cast(var_value as smallint) from history_configuration where var_name='hoursToKeep'
		if @hoursToKeep is null begin
			set @hoursToKeep=1440 -- keep two month of hour detail level
			insert history_configuration (var_name,var_value) values('hoursToKeep', cast(@hoursToKeep as varchar))
		end	

		declare @LastHourToKeep datetime -- the oldest data to keep in hour level aggregation
		set @LastHourToKeep = dateadd(hh, -@hoursToKeep, @RoundDateTime)
		if @LastHourToKeep > @lastHourDel begin
			print 'performing hour detele'	
			set @Aggregation_level=@HOUR_LEVEL
			set @Gap = @GAP_DAY
			exec dwh_history_delete @Aggregation_level, @LastHourToKeep, @lastHourDel, @Gap
		end
		
		----------------------- daily delete -----------------------	
        declare @lastDayDel datetime    -- the last day that was deteted
		select @lastDayDel=cast(var_value as datetime) from history_configuration where var_name='lastDayDel'
		if @lastDayDel is null begin
			set @lastDayDel=@START_DATE
			insert history_configuration (var_name,var_value) values('lastDayDel', cast(@lastDayDel as varchar))
		end

		-- read the time to keep history data from configuration table
		declare @daysToKeep smallint; 
		select @daysToKeep=cast(var_value as smallint) from history_configuration where var_name='daysToKeep'
		if @daysToKeep is null begin
			set @daysToKeep=1825 -- keep five years of day detail level
			insert history_configuration (var_name,var_value) values('daysToKeep', cast(@daysToKeep as varchar))
		end	

		declare @LastDayToKeep datetime  -- the oldest data to keep in day level aggregation
		set @LastDayToKeep = dateadd(dd, -@daysToKeep, dateadd(hh,-datepart(hh,@RoundDateTime),@RoundDateTime))
		if @LastDayToKeep > @lastDayDel begin
			print 'performing day detele'
			set @Aggregation_level=@DAY_LEVEL
			set @Gap = @GAP_DAY
			exec dwh_history_delete @Aggregation_level, @LastDayToKeep, @lastDayDel, @Gap
		end

	END TRY
	BEGIN CATCH
		EXEC $(db).dbo.RethrowError;
	END CATCH

END

GO




