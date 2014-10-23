INSERT INTO disk_profiles
    (SELECT uuid_generate_v1(), storage_name, id, NULL, 'Default unlimited disk profile'
     FROM storage_domain_static
     WHERE storage_domain_type IN (0,1) -- 0 and 1 are data domains type
     AND id NOT IN (select storage_domain_id from disk_profiles));
