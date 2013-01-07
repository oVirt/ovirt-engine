SELECT fn_db_drop_constraint('disks','fk_disk_active_image');
SELECT fn_db_drop_column('disks', 'active_image_id');

SELECT fn_db_drop_column('images', 'internal_drive_mapping');
SELECT fn_db_drop_column('images', 'description');
SELECT fn_db_drop_column('images', 'app_list');
SELECT fn_db_drop_column('images', 'disk_type');
SELECT fn_db_drop_column('images', 'disk_interface');
SELECT fn_db_drop_column('images', 'wipe_after_delete');
SELECT fn_db_drop_column('images', 'propagate_errors');

