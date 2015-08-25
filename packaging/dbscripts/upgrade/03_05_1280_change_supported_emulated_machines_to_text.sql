-- change vds_dynamic.supported_emulated_machines to text
-- currently it has max of 255 chars, but this can be exceeded for example if using the virt-preview repo
SELECT fn_db_change_column_type('vds_dynamic', 'supported_emulated_machines', 'varchar', 'text');
