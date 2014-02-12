select fn_db_add_column('vm_static', 'serial_number_policy', 'smallint default null');
select fn_db_add_column('vm_static', 'custom_serial_number', 'varchar(255) default null');

select fn_db_add_column('vds_groups', 'serial_number_policy', 'smallint default null');
select fn_db_add_column('vds_groups', 'custom_serial_number', 'varchar(255) default null');
