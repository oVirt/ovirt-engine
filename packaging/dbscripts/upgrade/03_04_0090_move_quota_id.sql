CREATE OR REPLACE FUNCTION __temp_move_quota_id() RETURNS void
AS $FUNCTION$
BEGIN
  IF (NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name ILIKE 'image_storage_domain_map' AND column_name ILIKE 'quota_id')) THEN
    -- add quota id column to the image-storage map table
    PERFORM fn_db_add_column('image_storage_domain_map','quota_id', 'UUID NULL');
    -- copy old quota from images table
    UPDATE image_storage_domain_map SET quota_id = (SELECT quota_id FROM images WHERE image_id = image_guid);
    -- create a FK to quota table to that column
    ALTER TABLE image_storage_domain_map ADD CONSTRAINT fk_image_storage_domain_map_quota FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE SET NULL;
    -- remove quota_id column from images table
    PERFORM fn_db_drop_column ('images', 'quota_id');
END IF;
END; $FUNCTION$
LANGUAGE plpgsql;

SELECT  __temp_move_quota_id();

DROP FUNCTION __temp_move_quota_id();
