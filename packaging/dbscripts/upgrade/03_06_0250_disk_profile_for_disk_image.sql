-- Add disk_profile id into image_storage_domain_map
SELECT fn_db_add_column('image_storage_domain_map', 'disk_profile_id', 'UUID NULL');

-- Create index for disk profile
DROP INDEX IF EXISTS IDX_image_storage_domain_map_profile_id;
CREATE INDEX IDX_image_storage_domain_map_profile_id ON image_storage_domain_map(disk_profile_id);

-- Add FK an handle cascade
ALTER TABLE image_storage_domain_map ADD CONSTRAINT FK_image_storage_domain_map_disk_profile_id FOREIGN KEY(disk_profile_id)
REFERENCES disk_profiles(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE SET NULL;

-- Add disk profile for each SD, if compat version >= 3.5
INSERT INTO disk_profiles(id, name, storage_domain_id)
  SELECT uuid_generate_v1(),
    storage_domain_static.storage_name,
    storage_domain_static.id
  FROM storage_pool LEFT OUTER JOIN storage_pool_iso_map ON storage_pool.id = storage_pool_iso_map.storage_pool_id
  LEFT OUTER JOIN storage_domain_static ON storage_pool_iso_map.storage_id = storage_domain_static.id
  WHERE (storage_domain_static.storage_domain_type = 0 OR
    storage_domain_static.storage_domain_type = 1) AND
    cast(storage_pool.compatibility_version as float) >= 3.5;

--- Add correct profile id foreach disk.
UPDATE image_storage_domain_map
  SET disk_profile_id = disk_profiles.id
  FROM disk_profiles
  WHERE image_storage_domain_map.storage_domain_id = disk_profiles.storage_domain_id;

