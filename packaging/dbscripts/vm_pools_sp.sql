----------------------------------------------------------------
-- [vm_pools] Table
--


Create or replace FUNCTION InsertVm_pools(v_vm_pool_description VARCHAR(4000),
 v_vm_pool_comment text,
 v_vm_pool_id UUID ,
 v_vm_pool_name VARCHAR(255),
 v_vm_pool_type INTEGER,
 v_parameters VARCHAR(200),
 v_prestarted_vms INTEGER,
 v_vds_group_id UUID,
 v_max_assigned_vms_per_user SMALLINT,
 v_spice_proxy VARCHAR(255))
RETURNS VOID
   AS $procedure$
BEGIN
      INSERT INTO vm_pools(vm_pool_id,vm_pool_description, vm_pool_comment, vm_pool_name, vm_pool_type,parameters, prestarted_vms, vds_group_id, max_assigned_vms_per_user, spice_proxy)
      VALUES(v_vm_pool_id,v_vm_pool_description, v_vm_pool_comment, v_vm_pool_name,v_vm_pool_type,v_parameters, v_prestarted_vms, v_vds_group_id, v_max_assigned_vms_per_user, v_spice_proxy);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVm_pools(v_vm_pool_description VARCHAR(4000),
 v_vm_pool_comment text,
 v_vm_pool_id UUID,
 v_vm_pool_name VARCHAR(255),
 v_vm_pool_type INTEGER,
 v_parameters VARCHAR(200),
 v_prestarted_vms INTEGER,
 v_vds_group_id UUID,
 v_max_assigned_vms_per_user SMALLINT,
 v_spice_proxy VARCHAR(255))
RETURNS VOID

	--The [vm_pools] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_pools
      SET vm_pool_description = v_vm_pool_description, vm_pool_comment = v_vm_pool_comment, vm_pool_name = v_vm_pool_name,
      vm_pool_type = v_vm_pool_type,parameters = v_parameters, prestarted_vms = v_prestarted_vms, vds_group_id = v_vds_group_id,
      max_assigned_vms_per_user = v_max_assigned_vms_per_user, spice_proxy = v_spice_proxy
      WHERE vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVm_pools(v_vm_pool_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
		-- Get (and keep) a shared lock with "right to upgrade to exclusive"
		-- in order to force locking parent before children
      select   vm_pool_id INTO v_val FROM vm_pools  WHERE vm_pool_id = v_vm_pool_id     FOR UPDATE;
      DELETE FROM vm_pools
      WHERE vm_pool_id = v_vm_pool_id;

		-- delete VmPool permissions --
      DELETE FROM permissions where object_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;




DROP TYPE IF EXISTS GetAllFromVm_pools_rs CASCADE;
Create type GetAllFromVm_pools_rs AS (vm_pool_id UUID, assigned_vm_count INTEGER, vm_running_count INTEGER, vm_pool_description VARCHAR(4000), vm_pool_comment text, vm_pool_name VARCHAR(255), vm_pool_type INTEGER, parameters VARCHAR(200), prestarted_vms INTEGER, vds_group_id UUID, vds_group_name VARCHAR(40), max_assigned_vms_per_user SMALLINT, spice_proxy VARCHAR(255));
Create or replace FUNCTION GetAllFromVm_pools() RETURNS SETOF GetAllFromVm_pools_rs
   AS $procedure$
BEGIN
      -- BEGIN TRAN
BEGIN
         CREATE TEMPORARY TABLE tt_VM_POOL_GROUP
         (
            vm_pool_id UUID,
            assigned_vm_count INTEGER
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_GROUP;
      END;
      insert INTO tt_VM_POOL_GROUP(vm_pool_id,
					assigned_vm_count)
      select
      vm_pools_view.vm_pool_id,
			  count(vm_pool_map.vm_pool_id)
      from vm_pools_view
      left join vm_pool_map on vm_pools_view.vm_pool_id = vm_pool_map.vm_pool_id
      group by vm_pools_view.vm_pool_id,vm_pool_map.vm_pool_id;
      BEGIN
         CREATE TEMPORARY TABLE tt_VM_POOL_RUNNING
         (
            vm_pool_id UUID,
            vm_running_count INTEGER
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_RUNNING;
      END;
      insert INTO tt_VM_POOL_RUNNING(vm_pool_id,
					vm_running_count)
      select vm_pools_view.vm_pool_id, count(vm_pools_view.vm_pool_id)
      from vm_pools_view
      left join vm_pool_map on vm_pools_view.vm_pool_id = vm_pool_map.vm_pool_id
      left join vm_dynamic on vm_pool_map.vm_guid = vm_dynamic.vm_guid
      where vm_dynamic.status > 0
      group by vm_pools_view.vm_pool_id;
      BEGIN
         CREATE TEMPORARY TABLE tt_VM_POOL_PRERESULT
         (
            vm_pool_id UUID,
            assigned_vm_count INTEGER,
            vm_running_count INTEGER
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_PRERESULT;
      END;
      insert INTO tt_VM_POOL_PRERESULT(vm_pool_id,
					assigned_vm_count,
					vm_running_count)
      select pg.vm_pool_id, pg.assigned_vm_count, pr.vm_running_count
      from tt_VM_POOL_GROUP pg
      left join tt_VM_POOL_RUNNING pr on pg.vm_pool_id = pr.vm_pool_id;
      update tt_VM_POOL_PRERESULT
      set vm_running_count = 0
      where vm_running_count is NULL;
      BEGIN
         CREATE TEMPORARY TABLE tt_VM_POOL_RESULT
         (
            vm_pool_id UUID,
            assigned_vm_count INTEGER,
            vm_running_count INTEGER,
            vm_pool_description VARCHAR(4000),
            vm_pool_comment text,
            vm_pool_name VARCHAR(255),
            vm_pool_type INTEGER,
            parameters VARCHAR(200),
            prestarted_vms INTEGER,
            vds_group_id UUID,
            vds_group_name VARCHAR(40),
            max_assigned_vms_per_user SMALLINT,
            spice_proxy VARCHAR(255)
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_RESULT;
      END;
      insert INTO tt_VM_POOL_RESULT(vm_pool_id,
            assigned_vm_count,
            vm_running_count,
            vm_pool_description,
            vm_pool_comment,
            vm_pool_name,
            vm_pool_type,
            parameters,
            prestarted_vms,
            vds_group_id,
            vds_group_name,
            max_assigned_vms_per_user,
            spice_proxy)
      select ppr.vm_pool_id, ppr.assigned_vm_count, ppr.vm_running_count,
  				 p.vm_pool_description, p.vm_pool_comment, p.vm_pool_name, p.vm_pool_type, p.parameters, p.prestarted_vms,
					 p.vds_group_id, p.vds_group_name, p.max_assigned_vms_per_user, p.spice_proxy
      from tt_VM_POOL_PRERESULT ppr
      inner join vm_pools_view p on ppr.vm_pool_id = p.vm_pool_id;
      RETURN QUERY select *
      from tt_VM_POOL_RESULT;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVm_poolsByvm_pool_id(v_vm_pool_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF vm_pools_full_view STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pools_full_view.*
      FROM vm_pools_full_view
      WHERE vm_pool_id = v_vm_pool_id
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_vm_pool_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = v_vm_pool_id));
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVm_poolsByvm_pool_name(v_vm_pool_name VARCHAR(255)) RETURNS SETOF vm_pools_view STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pools_view.*
      FROM vm_pools_view
      WHERE vm_pool_name = v_vm_pool_name;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetAllVm_poolsByUser_id(v_user_id UUID) RETURNS SETOF vm_pools_view STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT  DISTINCT vm_pools_view.*
      FROM         users_and_groups_to_vm_pool_map_view INNER JOIN
      vm_pools_view ON
      users_and_groups_to_vm_pool_map_view.vm_pool_id = vm_pools_view.vm_pool_id
      WHERE     (users_and_groups_to_vm_pool_map_view.user_id = v_user_id);
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVm_poolsByAdGroup_names(v_ad_group_names VARCHAR(4000)) RETURNS SETOF vm_pools_view STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vm_pools_view.*
      FROM         ad_groups INNER JOIN
      users_and_groups_to_vm_pool_map_view ON
      ad_groups.id = users_and_groups_to_vm_pool_map_view.user_id INNER JOIN
      vm_pools_view ON users_and_groups_to_vm_pool_map_view.vm_pool_id = vm_pools_view.vm_pool_id
      WHERE     (ad_groups.name in(select Id from fnSplitter(v_ad_group_names)));
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetVmDataFromPoolByPoolId(v_pool_id uuid, v_user_id uuid, v_is_filtered boolean)
  RETURNS SETOF vms STABLE AS $procedure$
BEGIN
     RETURN QUERY SELECT vms.*
     FROM vms WHERE vm_pool_id = v_pool_id
     AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_vm_pool_permissions_view
                                       WHERE  user_id = v_user_id AND entity_id = v_pool_id))
     -- Limiting results to 1 since we only need a single VM from the pool to retrieve the pool data
     LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDataFromPoolByPoolName(v_pool_name VARCHAR(4000), v_user_id uuid, v_is_filtered boolean)
  RETURNS SETOF vms STABLE AS $procedure$
BEGIN
     RETURN QUERY SELECT vms.*
     FROM vms WHERE vm_pool_name = v_pool_name
     AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_vm_pool_permissions_view
                                       WHERE  user_id = v_user_id AND entity_id = vms.vm_guid))
     -- Limiting results to 1 since we only need a single VM from the pool to retrieve the pool data
     LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllVm_poolsByUser_id_with_groups_and_UserRoles(v_user_id UUID)
RETURNS SETOF vm_pools_view STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT pools.*
      FROM vm_pools_view pools
      INNER JOIN user_vm_pool_permissions_view ON user_id = v_user_id AND entity_id = pools.vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;
