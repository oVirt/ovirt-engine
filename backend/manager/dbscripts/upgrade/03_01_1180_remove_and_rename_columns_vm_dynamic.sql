-- remove unused column

select fn_db_drop_column('vm_dynamic', 'vm_last_up_time');

select fn_db_rename_column('vm_dynamic', 'vm_last_boot_time', 'last_start_time');

