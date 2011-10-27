

----------------------------------------------------------------
-- [vm_template_image_map] Table
--




Create or replace FUNCTION InsertVmTemplateImageMap(v_internal_drive_mapping VARCHAR(50),
	v_it_guid UUID,
	v_vmt_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_template_image_map(internal_drive_mapping, it_guid, vmt_guid)
	VALUES(v_internal_drive_mapping, v_it_guid, v_vmt_guid);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION UpdateVmTemplateImageMap(v_internal_drive_mapping VARCHAR(50),
	v_it_guid UUID,
	v_vmt_guid UUID)
RETURNS VOID

	--The [vm_template_image_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_template_image_map
      SET internal_drive_mapping = v_internal_drive_mapping
      WHERE it_guid = v_it_guid AND vmt_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVmTemplateImageMap(v_it_guid UUID,
	v_vmt_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM vm_template_image_map
      WHERE it_guid = v_it_guid AND vmt_guid = v_vmt_guid;
      DELETE FROM image_templates
      WHERE it_guid = v_it_guid;
      DELETE FROM images
      WHERE image_guid = v_it_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVmTemplateImageMap() RETURNS SETOF vm_template_image_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_template_image_map.*
      FROM vm_template_image_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmTemplateImageMapByItGuidAndByVmtGuid(v_it_guid UUID,
	v_vmt_guid UUID) RETURNS SETOF vm_template_image_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_template_image_map.*
      FROM vm_template_image_map
      WHERE it_guid = v_it_guid AND vmt_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;





