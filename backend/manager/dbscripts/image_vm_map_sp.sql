-- TODO: Delete this function once the disks table is updated directly from code.
Create or replace FUNCTION UpdateActiveImageIdInDisk(v_image_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
    UPDATE disks
    SET    active_image_id = v_image_id
    WHERE  disk_id IN (SELECT image_group_id
                       FROM   images
                       WHERE  image_guid = v_image_id);
END; $procedure$
LANGUAGE plpgsql;




----------------------------------------------------------------
-- [image_vm_map] Table
--




Create or replace FUNCTION Insertimage_vm_map(v_active BOOLEAN ,
	v_image_id UUID,
	v_vm_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO image_vm_map(active, image_id, vm_id)
	VALUES(v_active, v_image_id, v_vm_id);
-- TODO: Delete this once the disks table is updated directly from code.
IF (v_active) THEN
   PERFORM UpdateActiveImageIdInDisk(v_image_id);
END IF;
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION Updateimage_vm_map(v_active BOOLEAN ,
	v_image_id UUID,
	v_vm_id UUID)
RETURNS VOID

	--The [image_vm_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE image_vm_map
      SET active = v_active
      WHERE image_id = v_image_id AND vm_id = v_vm_id;

      -- TODO: Delete this once the disks table is updated directly from code.
      IF (v_active) THEN
         PERFORM UpdateActiveImageIdInDisk(v_image_id);
      END IF;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deleteimage_vm_map(v_image_id UUID,
	v_vm_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM image_vm_map
      WHERE image_id = v_image_id AND vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromimage_vm_map() RETURNS SETOF image_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM image_vm_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getimage_vm_mapByimage_idAndByvm_id(v_image_id UUID,
	v_vm_id UUID) RETURNS SETOF image_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM image_vm_map
      WHERE image_id = v_image_id AND vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Getimage_vm_mapByimage_id(v_image_id UUID)
RETURNS SETOF image_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM image_vm_map
      WHERE image_id = v_image_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION Getimage_vm_mapByvm_id(v_vm_id UUID) RETURNS SETOF image_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM image_vm_map
      WHERE vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;



