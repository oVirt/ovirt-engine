SELECT fn_db_change_table_string_columns_to_empty_string('storage_domain_static','{storage_description,storage_comment}');
SELECT fn_db_change_table_string_columns_to_empty_string('base_disks','{disk_description}');

ALTER TABLE storage_domain_static
    ALTER COLUMN storage_description SET NOT NULL,
    ALTER COLUMN storage_comment SET NOT NULL;

ALTER TABLE base_disks
    ALTER COLUMN disk_description SET NOT NULL;
