

----------------------------------------------------------------
-- [image_group_storage_domain_map] Table
--




Create or replace FUNCTION Insertimage_group_storage_domain_map(v_image_group_id UUID,
    v_storage_domain_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO image_group_storage_domain_map(image_group_id, storage_domain_id)
	VALUES(v_image_group_id, v_storage_domain_id);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION Deleteimage_group_storage_domain_map(v_image_group_id UUID,
	v_storage_domain_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
	
   DELETE FROM image_group_storage_domain_map
   WHERE image_group_id = v_image_group_id AND storage_domain_id = v_storage_domain_id;
    
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromimage_group_storage_domain_map() 
RETURNS SETOF image_group_storage_domain_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM image_group_storage_domain_map;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getimage_grp_storage_domain_mapByimg_grp_idAndstorage_domain(v_image_group_id UUID,v_storage_domain_id UUID) RETURNS SETOF image_group_storage_domain_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM image_group_storage_domain_map
   WHERE image_group_id = v_image_group_id AND storage_domain_id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getimage_group_storage_domain_mapByimage_group_id(v_image_group_id UUID)
RETURNS SETOF image_group_storage_domain_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM image_group_storage_domain_map
   WHERE image_group_id = v_image_group_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getimage_group_storage_domain_mapBystorage_domain_id(v_storage_domain_id UUID) RETURNS SETOF image_group_storage_domain_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM image_group_storage_domain_map
   WHERE storage_domain_id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;


