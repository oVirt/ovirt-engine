
select fn_db_add_column('vm_dynamic', 'guest_timezone_offset', 'integer');
select fn_db_add_column('vm_dynamic', 'guest_timezone_name', 'varchar(255)');
select fn_db_add_column('vm_dynamic', 'guestos_arch', 'integer NOT NULL DEFAULT 0'); -- 0 stands for undefined
select fn_db_add_column('vm_dynamic', 'guestos_codename', 'varchar(255)');
select fn_db_add_column('vm_dynamic', 'guestos_distribution', 'varchar(255)');
select fn_db_add_column('vm_dynamic', 'guestos_kernel_version', 'varchar(255)');
select fn_db_add_column('vm_dynamic', 'guestos_type', 'varchar(255) NOT NULL DEFAULT ''Other''');
select fn_db_add_column('vm_dynamic', 'guestos_version', 'varchar(255)');
