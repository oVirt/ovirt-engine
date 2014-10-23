CREATE OR REPLACE FUNCTION create_default_disk_profiles() RETURNS VOID AS $$
DECLARE
sd_id uuid;
BEGIN
RAISE NOTICE 'Assigning defaults disk profiles for all storage domains...';

INSERT INTO disk_profiles
    (SELECT uuid_generate_v1(), storage_name, id, NULL, 'Default unlimited disk profile'
     FROM storage_domain_static
     WHERE storage_domain_type IN (0,1) -- 0 and 1 are data domains type
     AND id NOT IN (select storage_domain_id from disk_profiles));

RAISE NOTICE 'Done Assigning default disk profiles.';
RETURN ;
END;
$$ LANGUAGE plpgsql;

SELECT create_default_disk_profiles();
DROP FUNCTION create_default_disk_profiles();

