SELECT fn_db_add_column('cluster', 'bios_type', 'integer not null default 0');
UPDATE vm_static SET bios_type = bios_type + 1 WHERE bios_type <> 0;
