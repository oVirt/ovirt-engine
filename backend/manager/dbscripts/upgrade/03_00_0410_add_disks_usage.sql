select fn_db_add_column('vm_statistics', 'disks_usage', 'text');
select fn_db_drop_column('disk_image_dynamic', 'guest_disk_size_bytes');
select fn_db_drop_column('disk_image_dynamic', 'guest_used_disk_size_bytes');