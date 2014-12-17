select fn_db_add_column('storage_domain_static', 'wipe_after_delete', 'boolean NOT NULL DEFAULT false');

UPDATE  storage_domain_static
SET     wipe_after_delete = true
FROM    vdc_options
WHERE   vdc_options.option_name = 'SANWipeAfterDelete' AND
        vdc_options.version = 'general' AND
        vdc_options.option_value = 'true' AND
        storage_domain_static.storage_type IN (2, 3); -- 2 and 3 are the only storage types which are block domains.
