select fn_db_add_column('vds_interface_statistics', 'update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('vm_interface_statistics', 'update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('vm_statistics', 'update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('vds_statistics', 'update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('disk_image_dynamic', 'update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('storage_domain_dynamic', 'update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
