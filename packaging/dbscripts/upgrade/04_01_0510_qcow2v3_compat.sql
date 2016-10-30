SELECT fn_db_add_column('images', 'qcow_compat', 'integer default 0');

UPDATE images
SET qcow_compat = 1
WHERE volume_format = 4;
