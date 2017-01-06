SELECT fn_db_add_column('vm_static', 'lease_sd_id', 'UUID NULL');
SELECT fn_db_create_constraint('vm_static',
                               'fk_vm_static_lease_sd_id_storage_domain_static_id',
                               'FOREIGN KEY (lease_sd_id) REFERENCES storage_domain_static(id) ON DELETE SET NULL');
