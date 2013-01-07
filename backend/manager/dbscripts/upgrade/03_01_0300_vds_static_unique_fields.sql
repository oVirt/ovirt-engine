select fn_db_create_constraint('vds_static', 'vds_static_vds_name_unique', 'UNIQUE(vds_name)');
select fn_db_create_constraint('vds_static', 'vds_static_host_name_unique', 'UNIQUE(host_name)');

