

----------------------------------------------------------------
-- [all_disks] View
--






Create or replace FUNCTION GetAllFromDisks(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF all_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   all_disks
    WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM user_disk_permissions_view
                                        WHERE user_id = v_user_id AND entity_id = disk_id));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetDiskByDiskId(v_disk_id UUID, v_user_id UUID, v_is_filtered boolean)
RETURNS SETOF all_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   all_disks
    WHERE  image_group_id = v_disk_id
    AND    (NOT v_is_filtered OR EXISTS (SELECT 1
                                         FROM user_disk_permissions_view
                                         WHERE user_id = v_user_id AND entity_id = v_disk_id));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetDisksVmGuid(v_vm_guid UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF all_disks
   AS $procedure$
BEGIN
      RETURN QUERY SELECT all_disks.*
      FROM all_disks
      LEFT JOIN vm_device on vm_device.device_id = all_disks.image_group_id
      WHERE vm_device.vm_id = v_vm_guid
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_disk_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = all_disks.disk_id));

END; $procedure$
LANGUAGE plpgsql;

-- Returns all the attachable disks in the storage pool
-- If storage pool is ommited, all the attachable disks are retrurned.
-- in case vm id is provided, returning all the disks in SP that are not attached to the vm
Create or replace FUNCTION GetAllAttachableDisksByPoolId(v_storage_pool_id UUID, v_vm_id uuid,  v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF all_disks
   AS $procedure$
BEGIN
    RETURN QUERY SELECT distinct all_disks.*
    FROM all_disks
    WHERE (v_storage_pool_id IS NULL OR all_disks.storage_pool_id = v_storage_pool_id)
    AND (all_disks.number_of_vms = 0 OR all_disks.shareable)
        -- ImageStatus.ILLEGAL=4 / imagestatus IS NULL -> LunDiski / ImageStatus.Locked=2
        AND (all_disks.imagestatus IS NULL OR (all_disks.imagestatus != 4 AND all_disks.imagestatus != 2))
        AND (v_vm_id IS NULL OR v_vm_id NOT IN (SELECT vm_id FROM vm_device WHERE vm_device.device_id = all_disks.image_group_id))
    AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_disk_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = disk_id));
END; $procedure$
LANGUAGE plpgsql;



