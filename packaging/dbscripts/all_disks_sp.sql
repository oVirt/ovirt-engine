

----------------------------------------------------------------
-- [all_disks] View
--






Create or replace FUNCTION GetAllFromDisks(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF all_disks STABLE
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
RETURNS SETOF all_disks STABLE
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


Create or replace FUNCTION GetDisksVmGuid(v_vm_guid UUID, v_only_plugged BOOLEAN, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF all_disks_for_vms STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM all_disks_for_vms
      WHERE vm_id = v_vm_guid AND (NOT v_only_plugged OR is_plugged)
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_disk_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = disk_id));

END; $procedure$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS disks_basic_rs CASCADE;
CREATE TYPE disks_basic_rs AS (disk_id UUID,disk_alias varchar(255),size BIGINT);

Create or replace FUNCTION GetDisksVmGuidBasicView(v_vm_guid UUID, v_only_plugged BOOLEAN, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF disks_basic_rs STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT disk_id,disk_alias, size
      FROM images
      LEFT OUTER JOIN base_disks ON images.image_group_id = base_disks.disk_id
      LEFT JOIN vm_device ON vm_device.device_id = image_group_id AND (NOT v_only_plugged OR is_plugged)
      WHERE vm_device.vm_id = v_vm_guid
      AND images.active = true
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_disk_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = disk_id));

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetVmBootActiveDisk(v_vm_guid UUID) RETURNS SETOF all_disks STABLE AS $procedure$
BEGIN
      RETURN QUERY SELECT all_disks.*
      FROM all_disks
      JOIN vm_device ON vm_device.device_id = all_disks.image_group_id
      WHERE vm_device.vm_id = v_vm_guid AND boot = TRUE AND vm_device.snapshot_id IS NULL;
END; $procedure$
LANGUAGE plpgsql;

-- Returns all the attachable disks in the storage pool
-- If storage pool is ommited, all the attachable disks are retrurned.
-- in case vm id is provided, returning all the disks in SP that are not attached to the vm
Create or replace FUNCTION GetAllAttachableDisksByPoolId(v_storage_pool_id UUID, v_vm_id uuid,  v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF all_disks STABLE
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



