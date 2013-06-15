select fn_db_add_column('base_disks', 'boot', 'boolean NOT NULL DEFAULT false');

update base_disks set boot = images.boot from images where images.image_group_id = disk_id;

select fn_db_drop_column('images','boot');

