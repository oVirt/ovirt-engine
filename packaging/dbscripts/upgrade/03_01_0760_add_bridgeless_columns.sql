select fn_db_add_column('network', 'vm_network', 'boolean NOT NULL DEFAULT true');
select fn_db_add_column('network_cluster', 'required', 'boolean NOT NULL DEFAULT true');
select fn_db_add_column('vds_interface', 'bridged', 'boolean NOT NULL DEFAULT true');
