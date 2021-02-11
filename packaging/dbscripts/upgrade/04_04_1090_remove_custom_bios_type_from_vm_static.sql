SELECT fn_db_rename_column('vm_static', 'effective_bios_type', 'bios_type');

ALTER TABLE vm_static
    ALTER COLUMN bios_type
        DROP DEFAULT;

ALTER TABLE vm_static
    ALTER COLUMN bios_type
        DROP NOT NULL;

UPDATE vm_static SET bios_type = NULL WHERE cluster_id IS NULL;

SELECT fn_db_drop_column('vm_static', 'custom_bios_type');

ALTER TABLE cluster
    ALTER COLUMN bios_type
        DROP DEFAULT;

ALTER TABLE cluster
    ALTER COLUMN bios_type
        DROP NOT NULL;
