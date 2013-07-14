

----------------------------------------------------------------
-- [vds_groups] Table
--




Create or replace FUNCTION InsertVdsGroups(
	v_vds_group_id UUID,
	v_description VARCHAR(4000),
	v_free_text_comment text,
	v_name VARCHAR(40),
	v_cpu_name VARCHAR(255),
	v_storage_pool_id UUID ,
	v_max_vds_memory_over_commit INTEGER,
	v_count_threads_as_cores BOOLEAN,
	v_compatibility_version VARCHAR(40),
	v_transparent_hugepages BOOLEAN ,
	v_migrate_on_error INTEGER,
	v_virt_service BOOLEAN,
	v_gluster_service BOOLEAN,
	v_tunnel_migration BOOLEAN,
	v_emulated_machine VARCHAR(40),
	v_trusted_service BOOLEAN,
        v_cluster_policy_id UUID,
        v_cluster_policy_custom_properties text,
	v_enable_balloon BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
      INSERT INTO vds_groups(vds_group_id,description, name, free_text_comment, cpu_name, storage_pool_id,  max_vds_memory_over_commit, count_threads_as_cores, compatibility_version,
        transparent_hugepages, migrate_on_error, virt_service, gluster_service, tunnel_migration, emulated_machine, trusted_service, cluster_policy_id,
        cluster_policy_custom_properties, enable_balloon)
	VALUES(v_vds_group_id,v_description, v_name, v_free_text_comment, v_cpu_name, v_storage_pool_id,  v_max_vds_memory_over_commit, v_count_threads_as_cores, v_compatibility_version,
    v_transparent_hugepages, v_migrate_on_error, v_virt_service, v_gluster_service, v_tunnel_migration, v_emulated_machine, v_trusted_service, v_cluster_policy_id, v_cluster_policy_custom_properties, v_enable_balloon);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVdsGroup(v_description VARCHAR(4000) ,
	v_free_text_comment text,
	v_name VARCHAR(40),
	v_vds_group_id UUID,
	v_cpu_name VARCHAR(255) ,
	v_storage_pool_id UUID ,
	v_max_vds_memory_over_commit INTEGER,
	v_count_threads_as_cores BOOLEAN,
	v_compatibility_version VARCHAR(40),
	v_transparent_hugepages BOOLEAN ,
	v_migrate_on_error INTEGER,
	v_virt_service BOOLEAN,
	v_gluster_service BOOLEAN,
	v_tunnel_migration BOOLEAN,
	v_emulated_machine VARCHAR(40),
	v_trusted_service BOOLEAN,
        v_cluster_policy_id UUID,
        v_cluster_policy_custom_properties text,
	v_enable_balloon BOOLEAN)
RETURNS VOID

	--The [vds_groups] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vds_groups
      SET description = v_description, free_text_comment = v_free_text_comment, name = v_name,cpu_name = v_cpu_name,
      storage_pool_id = v_storage_pool_id,_update_date = LOCALTIMESTAMP,
      max_vds_memory_over_commit = v_max_vds_memory_over_commit,
      count_threads_as_cores = v_count_threads_as_cores,
      compatibility_version = v_compatibility_version,transparent_hugepages = v_transparent_hugepages,
      migrate_on_error = v_migrate_on_error,
      virt_service = v_virt_service, gluster_service = v_gluster_service, tunnel_migration = v_tunnel_migration,
      emulated_machine = v_emulated_machine, trusted_service = v_trusted_service, cluster_policy_id = v_cluster_policy_id,
      cluster_policy_custom_properties = v_cluster_policy_custom_properties, enable_balloon = v_enable_balloon
      WHERE vds_group_id = v_vds_group_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVdsGroup(v_vds_group_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
		-- Get (and keep) a shared lock with "right to upgrade to exclusive"
		-- in order to force locking parent before children
      select   vds_group_id INTO v_val FROM vds_groups  WHERE vds_group_id = v_vds_group_id     FOR UPDATE;
      DELETE FROM vds_groups
      WHERE vds_group_id = v_vds_group_id;
		-- delete VDS group permissions --
      DELETE FROM permissions where object_id = v_vds_group_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetAllFromVdsGroups(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                          FROM   user_vds_groups_permissions_view
                                          WHERE  user_id = v_user_id and entity_id = vds_group_id));

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVdsGroupByVdsGroupId(v_vds_group_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE vds_group_id = v_vds_group_id
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_vds_groups_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = v_vds_group_id));

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsGroupByVdsGroupName(v_vds_group_name VARCHAR(40)) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE name = v_vds_group_name;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdsGroupForUserByVdsGroupName(v_vds_group_name VARCHAR(40), v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE name = v_vds_group_name
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_vds_groups_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = vds_group_id));
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsGroupsByStoragePoolId(v_storage_pool_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE storage_pool_id = v_storage_pool_id
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_vds_groups_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = vds_group_id));
END; $procedure$
LANGUAGE plpgsql;






--This SP returns the VDS group if it has running vms
Create or replace FUNCTION GetVdsGroupWithRunningVms(v_vds_group_id UUID) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE vds_group_id = v_vds_group_id and vds_group_id in(select vds_group_id from vms where vms.status NOT IN(0,13,14));
END; $procedure$
LANGUAGE plpgsql;



--This SP returns all clusters with permissions to run the given action by user
Create or replace FUNCTION fn_perms_get_vds_groups_with_permitted_action(v_user_id UUID, v_action_group_id integer) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE (SELECT 1 from get_entity_permissions(v_user_id, v_action_group_id, vds_groups_view.vds_group_id, 9)) IS NOT NULL;
END; $procedure$
LANGUAGE plpgsql;

--This SP updates the vds_group emulated machine
Create or replace FUNCTION UpdateVdsGroupEmulatedMachine(v_vds_group_id UUID, v_emulated_machine varchar(40)) RETURNS VOID
   AS $procedure$
BEGIN
    UPDATE vds_groups set emulated_machine = v_emulated_machine where vds_group_id = v_vds_group_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetTrustedVdsGroups() RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE trusted_service;
END; $procedure$
LANGUAGE plpgsql;

-- returns all clusters attached to a specific cluster policy (given as a parameter to the SP)
Create or replace FUNCTION GetVdsGroupsByClusterPolicyId(v_cluster_policy_id UUID) RETURNS SETOF vds_groups_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vds_groups_view.*
      FROM vds_groups_view
      WHERE cluster_policy_id = v_cluster_policy_id;
END; $procedure$
LANGUAGE plpgsql;


