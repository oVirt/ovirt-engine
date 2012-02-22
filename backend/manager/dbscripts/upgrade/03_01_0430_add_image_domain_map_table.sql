CREATE OR REPLACE FUNCTION Upgrade_ImageDomainMapTable_03_01_0430()
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

   DROP TABLE image_group_storage_domain_map;
   
   ALTER TABLE images DROP CONSTRAINT Fk_images_storage_id; 
   ALTER TABLE images DROP COLUMN storage_id;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM Upgrade_ImageDomainMapTable_03_01_0430();

DROP FUNCTION Upgrade_ImageDomainMapTable_03_01_0430();

