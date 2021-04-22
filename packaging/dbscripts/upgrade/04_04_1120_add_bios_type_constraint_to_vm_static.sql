SELECT fn_db_create_constraint('vm_static',
                               'vm_static_bios_type_not_null_if_cluster_set',
                               'CHECK ((cluster_id IS NULL) = (bios_type IS NULL))');
