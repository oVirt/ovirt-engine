





Create or replace FUNCTION GetVmTemplateDiskByVmtGuidAndItGuid(v_vmt_guid UUID,
	v_it_guid  UUID) RETURNS SETOF vm_template_disk
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_template_disk.*
      FROM vm_template_disk
      WHERE vmt_guid = v_vmt_guid AND it_guid = v_it_guid;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetVmTemplateDisksByVmtGuid(v_vmt_guid UUID) RETURNS SETOF vm_template_disk
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_template_disk.*
      FROM vm_template_disk
      WHERE vmt_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteVmTemplateDisk(v_it_guid UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM vm_template_image_map
      WHERE it_guid = v_it_guid;
      DELETE FROM image_templates
      WHERE it_guid = v_it_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmTemplateDisksByImageTemplateGuid(v_it_guid UUID) RETURNS SETOF vm_template_disk
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_template_disk.*
      FROM vm_template_disk
      WHERE it_guid = v_it_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllVmTemplateDisks() RETURNS SETOF vm_template_disk
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_template_disk.*
      FROM vm_template_disk;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION InsertVmTemplateDisk(v_creation_date TIMESTAMP WITH TIME ZONE,
	v_description VARCHAR(4000) ,
	v_it_guid UUID,
	v_vtim_it_guid UUID,
	v_size BIGINT,
	v_os VARCHAR(40),
	v_os_version VARCHAR(40),
	v_bootable BOOLEAN ,
	v_vmt_guid UUID,
    v_internal_drive_mapping VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO image_templates(creation_date, description, it_guid, size, os, os_version, bootable)
	VALUES(v_creation_date, v_description, v_it_guid, v_size, v_os, v_os_version, v_bootable);
    
      INSERT INTO vm_template_image_map(it_guid, vmt_guid, internal_drive_mapping)
	VALUES(v_vtim_it_guid, v_vmt_guid, v_internal_drive_mapping);
END; $procedure$
LANGUAGE plpgsql;    






Create or replace FUNCTION UpdateVmTemplateDisk(v_creation_date TIMESTAMP WITH TIME ZONE,
	v_description VARCHAR(4000) ,
	v_it_guid UUID,
	v_vtim_it_guid UUID,
	v_size BIGINT,
	v_os VARCHAR(40),
	v_os_version VARCHAR(40),
	v_bootable BOOLEAN ,
	v_vmt_guid UUID,
    v_internal_drive_mapping VARCHAR(50))
RETURNS VOID

	--The [image_templates] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE image_templates
      SET creation_date = v_creation_date,description = v_description,size = v_size, 
      os = v_os,os_version = v_os_version,bootable = v_bootable
      WHERE it_guid = v_it_guid;
      UPDATE vm_template_image_map
      SET internal_drive_mapping = v_internal_drive_mapping
      WHERE it_guid = v_vtim_it_guid AND vmt_guid = v_vmt_guid;
END; $procedure$
LANGUAGE plpgsql;



