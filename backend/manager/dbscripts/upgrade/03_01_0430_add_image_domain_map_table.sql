CREATE OR REPLACE FUNCTION __temp_Upgrade_ImageDomainMapTable()
RETURNS void
AS $function$
BEGIN
   IF EXISTS (SELECT * FROM information_schema.tables WHERE table_name ILIKE 'image_storage_domain_map') THEN
       RETURN;
   END IF;

   -- Add the image_storage_domain_map table.
   CREATE TABLE image_storage_domain_map
   (
      image_id UUID NOT NULL,
      storage_domain_id UUID NOT NULL,
      CONSTRAINT PK_image_storage_domain_map PRIMARY KEY(image_id,storage_domain_id)
   ) WITH OIDS;

   INSERT INTO image_storage_domain_map (image_id,storage_domain_id)
   SELECT images.image_guid AS image_id, images.storage_id AS storage_domain_id
   FROM images WHERE images.storage_id IS NOT NULL
   UNION
   SELECT images.image_guid AS image_id, image_group_storage_domain_map.storage_domain_id AS storage_domain_id
   FROM images INNER JOIN image_group_storage_domain_map ON images.image_group_id = image_group_storage_domain_map.image_group_id;

-- The following line removes grabage copied from dropped tables
delete from image_storage_domain_map where image_id not in (select image_guid from images) or
                                           storage_domain_id not in (select id from storage_domain_static);

perform fn_db_create_constraint('image_storage_domain_map', 'fk_image_storage_domain_map_images', 'FOREIGN KEY (image_id) REFERENCES images(image_guid) ON DELETE CASCADE');
perform fn_db_create_constraint('image_storage_domain_map', 'fk_image_storage_domain_map_storage_domain_static', 'FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE');

   DROP TABLE image_group_storage_domain_map;

   perform fn_db_drop_constraint('images','Fk_images_storage_id');
   ALTER TABLE images DROP COLUMN storage_id;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_ImageDomainMapTable();

DROP FUNCTION __temp_Upgrade_ImageDomainMapTable();

