
select fn_db_add_column('vds_statistics', 'ha_configured', 'BOOLEAN NOT NULL DEFAULT FALSE');
select fn_db_add_column('vds_statistics', 'ha_active', 'BOOLEAN NOT NULL DEFAULT FALSE');
select fn_db_add_column('vds_statistics', 'ha_global_maintenance', 'BOOLEAN NOT NULL DEFAULT FALSE');
select fn_db_add_column('vds_statistics', 'ha_local_maintenance', 'BOOLEAN NOT NULL DEFAULT FALSE');

