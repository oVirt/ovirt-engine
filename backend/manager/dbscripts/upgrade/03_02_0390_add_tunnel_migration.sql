select fn_db_add_column('vds_groups', 'tunnel_migration', 'boolean NOT NULL default false');
select fn_db_add_column('vm_static', 'tunnel_migration', 'boolean');
