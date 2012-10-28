

----------------------------------------------------------------
-- [images] Table
--







Create or replace FUNCTION GetImageByImageGuid(v_image_guid UUID)
RETURNS SETOF vm_images_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM vm_images_view
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAncestralImageByImageGuid(v_image_guid UUID)
RETURNS SETOF images_storage_domain_view
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
      AND ai.image_guid = i.image_guid;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetSnapshotByGuid(v_image_guid UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetSnapshotsByStorageDomainId(v_storage_domain_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE storage_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotByParentGuid(v_parent_guid UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE ParentId = v_parent_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmImageByImageGuid(v_image_guid UUID)
RETURNS SETOF vm_images_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM vm_images_view
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetImagesByQuotaId(v_quota_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view
      WHERE quota_id = v_quota_id;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetSnapshotsByVmSnapshotId(v_vm_snapshot_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE vm_snapshot_id = v_vm_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotsByImageGroupId(v_image_group_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE image_group_id = v_image_group_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetImageByStorageIdAndTemplateId(v_storage_id UUID, v_template_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
	  IF v_template_id IS NULL then
           RETURN QUERY SELECT images_storage_domain_view.*
           FROM images_storage_domain_view
           WHERE images_storage_domain_view.storage_id = v_storage_id AND images_storage_domain_view.entity_type::text = 'TEMPLATE'::text;
      ELSE
           RETURN QUERY SELECT images_storage_domain_view.*
           FROM images_storage_domain_view
           JOIN vm_device ON vm_device.device_id = images_storage_domain_view.disk_id
           WHERE images_storage_domain_view.storage_id = v_storage_id
             AND vm_device.vm_id = v_template_id
             AND images_storage_domain_view.entity_type::text = 'TEMPLATE'::text;
      END IF;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetImagesWhichHaveNoDisk(v_vm_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT i.*
      FROM   images_storage_domain_view i
      JOIN   snapshots s ON (i.vm_snapshot_id = s.snapshot_id)
      WHERE  s.vm_id = v_vm_id
      AND    NOT EXISTS (
          SELECT 1
          FROM   base_disks d
          WHERE  d.disk_id = i.image_group_id);
END; $procedure$
LANGUAGE plpgsql;
