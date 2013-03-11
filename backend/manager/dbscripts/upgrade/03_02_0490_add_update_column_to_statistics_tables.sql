select fn_db_add_column('vds_interface_statistics', '_update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('vm_interface_statistics', '_update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('vm_statistics', '_update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('vds_statistics', '_update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('disk_image_dynamic', '_update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
select fn_db_add_column('storage_domain_dynamic', '_update_date', 'TIMESTAMP WITH TIME ZONE default NULL');
