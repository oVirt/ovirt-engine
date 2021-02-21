

----------------------------------------------------------------
-- [images] Table
--







Create or replace FUNCTION GetImageByImageGuid(v_image_guid UUID)
RETURNS SETOF vm_images_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM vm_images_view
     WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAncestralImageByImageGuid(v_image_guid UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY WITH RECURSIVE ancestor_image(image_guid, parentid) AS (
         SELECT image_guid, parentid
         FROM images
         WHERE image_guid = v_image_guid
         UNION ALL
         SELECT i.image_guid, i.parentid
         FROM images i, ancestor_image ai
         WHERE i.image_guid = ai.parentid
     )
     SELECT i.*
     FROM ancestor_image ai, images_storage_domain_view i
     WHERE ai.parentid = '00000000-0000-0000-0000-000000000000'
         AND ai.image_guid = i.image_guid
         AND (NOT v_is_filtered OR EXISTS (
             SELECT 1
             FROM   user_disk_permissions_view
             WHERE  user_disk_permissions_view.user_id = v_user_id
             AND    user_disk_permissions_view.entity_id = i.image_group_id));
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetSnapshotByGuid(v_image_guid UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view images_storage_domain_view
     WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetSnapshotsByStorageDomainId(v_storage_domain_id UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view
     WHERE storage_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotByParentGuid(v_parent_guid UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view images_storage_domain_view
     WHERE ParentId = v_parent_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotByLeafGuid(v_image_guid UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY WITH RECURSIVE image_list AS (
          SELECT *
          FROM   images_storage_domain_view
          WHERE  image_guid = v_image_guid
          UNION ALL
          SELECT images_storage_domain_view.*
          FROM   images_storage_domain_view
          JOIN   image_list ON
                 image_list.parentid = images_storage_domain_view.image_guid AND
                 image_list.image_group_id = images_storage_domain_view.image_group_id
      )
      SELECT *
      FROM image_list;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmImageByImageGuid(v_image_guid UUID)
RETURNS SETOF vm_images_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM vm_images_view
     WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetSnapshotsByVmSnapshotId(v_vm_snapshot_id UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view images_storage_domain_view
     WHERE vm_snapshot_id = v_vm_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetAttachedDiskSnapshotsToVm(v_vm_guid UUID, v_is_plugged BOOLEAN)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
     RETURN QUERY SELECT images_storage_domain_view.*
     FROM images_storage_domain_view
     JOIN vm_device ON vm_device.device_id = images_storage_domain_view.disk_id
     WHERE vm_device.vm_id = v_vm_guid AND (v_is_plugged IS NULL OR vm_device.is_plugged = v_is_plugged)
          AND vm_device.snapshot_id IS NOT NULL
          AND vm_device.snapshot_id = images_storage_domain_view.vm_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotsByImageGroupId(v_image_group_id UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view images_storage_domain_view
     WHERE image_group_id = v_image_group_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetDiskSnapshotForVmSnapshot(v_image_group_id UUID, v_vm_snapshot_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view
     WHERE image_group_id = v_image_group_id
         AND vm_snapshot_id = v_vm_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllForStorageDomain(v_storage_domain_id UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT images_storage_domain_view.*
     FROM  images_storage_domain_view
     WHERE active AND images_storage_domain_view.storage_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetImagesWhichHaveNoDisk(v_vm_id UUID)
RETURNS SETOF images_storage_domain_view STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT i.*
     FROM   images_storage_domain_view i
     JOIN   snapshots s ON (i.vm_snapshot_id = s.snapshot_id)
     WHERE  s.vm_id = v_vm_id
     AND NOT EXISTS (
         SELECT 1
         FROM   base_disks d
         WHERE  d.disk_id = i.image_group_id);
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllForDiskProfiles(v_disk_profile_ids UUID[])
RETURNS SETOF images_storage_domain_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT images_storage_domain_view.*
        FROM images_storage_domain_view
        WHERE images_storage_domain_view.disk_profile_id = ANY(v_disk_profile_ids);
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetSnapshotsByParentsGuid(v_parent_guids uuid[])
RETURNS SETOF images_storage_domain_view STABLE
AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view
     WHERE parentid = ANY(v_parent_guids);
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetDiskImageByDiskAndImageIds(v_disk_id UUID, v_image_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF images_storage_domain_view STABLE
AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM images_storage_domain_view
     WHERE image_group_id = v_disk_id
         AND image_guid = v_image_id
         AND (NOT v_is_filtered OR EXISTS (
             SELECT 1
             FROM   user_disk_permissions_view
             WHERE  user_id = v_user_id
             AND    entity_id = images_storage_domain_view.image_group_id));
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllMetadataAndMemoryDisksForStorageDomain(v_storage_domain_id UUID)
RETURNS SETOF UUID
   AS $procedure$
BEGIN
    RETURN QUERY SELECT isdv.disk_id
    FROM images_storage_domain_view isdv
    INNER JOIN snapshots s
    ON (s.memory_dump_disk_id = isdv.disk_id OR s.memory_metadata_disk_id = isdv.disk_id)
    AND isdv.storage_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;
