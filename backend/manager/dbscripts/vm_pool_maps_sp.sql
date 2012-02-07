

----------------------------------------------------------------
-- [vm_pool_map] Table
--




Create or replace FUNCTION InsertVm_pool_map(v_vm_guid UUID,  
 v_vm_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_pool_map(vm_guid, vm_pool_id)
	VALUES(v_vm_guid, v_vm_pool_id);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION UpdateVm_pool_map(v_vm_guid UUID,  
 v_vm_pool_id UUID)
RETURNS VOID

	--The [vm_pool_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_pool_map
      SET vm_pool_id = v_vm_pool_id
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVm_pool_map(v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  VARCHAR(50);
BEGIN
		-- Get (and keep) a shared lock with "right to upgrade to exclusive"
		-- in order to force locking parent before children 
      select   vm_guid INTO v_val FROM vm_pool_map  WHERE vm_guid = v_vm_guid     FOR UPDATE;
      DELETE FROM vm_pool_map
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVm_pool_map() RETURNS SETOF vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pool_map.*
      FROM vm_pool_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVm_pool_mapByvm_guid(v_vm_guid UUID) RETURNS SETOF vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pool_map.*
      FROM vm_pool_map
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVm_pool_mapByvm_pool_id(v_vm_pool_id UUID) RETURNS SETOF vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pool_map.*
      FROM vm_pool_map INNER JOIN vm_static
      ON vm_pool_map.vm_guid = vm_static.vm_guid
      WHERE vm_pool_id = v_vm_pool_id
      ORDER BY vm_static.VM_NAME;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION getVmMapsInVmPoolByVmPoolIdAndStatus(v_vm_pool_id UUID, v_status INTEGER) RETURNS SETOF vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pool_id, vm_pool_map.vm_guid
      FROM vm_pool_map, vm_dynamic
      WHERE vm_pool_map.vm_guid = vm_dynamic.vm_guid
      AND vm_pool_id = v_vm_pool_id
      AND vm_dynamic.status = v_status;
END; $procedure$
LANGUAGE plpgsql;





----------------------------------------------------------------
-- [image_vm_pool_map] Table
--


Create or replace FUNCTION Insertimage_vm_pool_map(v_image_guid UUID,
	v_internal_drive_mapping VARCHAR(50) ,
	v_vm_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO image_vm_pool_map(image_guid, internal_drive_mapping, vm_guid)
	VALUES(v_image_guid, v_internal_drive_mapping, v_vm_guid);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION Updateimage_vm_pool_map(v_image_guid UUID,
	v_internal_drive_mapping VARCHAR(50) ,
	v_vm_guid UUID)
RETURNS VOID

	--The [image_vm_pool_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE image_vm_pool_map
      SET internal_drive_mapping = v_internal_drive_mapping,vm_guid = v_vm_guid
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deleteimage_vm_pool_map(v_image_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM image_vm_pool_map
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromimage_vm_pool_map() RETURNS SETOF image_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT image_vm_pool_map.*
      FROM image_vm_pool_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getimage_vm_pool_mapByimage_guid(v_image_guid UUID) RETURNS SETOF image_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT image_vm_pool_map.*
      FROM image_vm_pool_map
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getimage_vm_pool_mapByvm_guid(v_vm_guid UUID) RETURNS SETOF image_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT image_vm_pool_map.*
      FROM image_vm_pool_map
      WHERE vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;





