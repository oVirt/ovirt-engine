

----------------------------------------------------------------
-- [all_disks] View
--






Create or replace FUNCTION GetAllFromDisks() RETURNS SETOF all_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   all_disks;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetDiskByDiskId(v_disk_id UUID)
RETURNS SETOF all_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   all_disks
    WHERE  image_group_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetDisksVmGuid(v_vm_guid UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF all_disks
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM all_disks
      WHERE
      vm_guid = v_vm_guid
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_vm_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = v_vm_guid));

END; $procedure$
LANGUAGE plpgsql;

-- Returns all the attachable disks in the storage pool.
-- If storage pool is ommited, all the attachable disks are retrurned.
Create or replace FUNCTION GetAllAttachableDisksByPoolId(v_storage_pool_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF all_disks
   AS $procedure$
BEGIN
    RETURN QUERY SELECT all_disks.*
    FROM all_disks
    WHERE (v_storage_pool_id IS NULL OR all_disks.storage_pool_id = v_storage_pool_id)
    AND   all_disks.vm_guid IS NULL
    AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_disk_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = disk_id));

END; $procedure$
LANGUAGE plpgsql;



