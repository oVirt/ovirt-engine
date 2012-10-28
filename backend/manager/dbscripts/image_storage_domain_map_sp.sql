

----------------------------------------------------------------
-- [image_group_storage_domain_map] Table
--




Create or replace FUNCTION Insertimage_storage_domain_map(v_image_id UUID,
    v_storage_domain_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO image_storage_domain_map(image_id, storage_domain_id)
	VALUES(v_image_id, v_storage_domain_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deleteimage_storage_domain_map(v_image_id UUID,
	v_storage_domain_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM image_storage_domain_map
   WHERE image_id = v_image_id AND storage_domain_id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Deleteimage_storage_domain_map_by_image_id(v_image_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM image_storage_domain_map
   WHERE image_id = v_image_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getimage_storage_domain_mapByimage_id(v_image_id UUID)
RETURNS SETOF image_storage_domain_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM image_storage_domain_map
   WHERE image_id = v_image_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getimage_storage_domain_mapBystorage_domain_id(v_storage_domain_id UUID) RETURNS SETOF image_storage_domain_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM image_storage_domain_map
   WHERE storage_domain_id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;


