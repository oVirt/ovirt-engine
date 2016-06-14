SELECT fn_db_drop_constraint('vm_vds_numa_node_map','fk_vm_vds_numa_node_map_vds_numa_node');
SELECT fn_db_drop_column('vm_vds_numa_node_map','vds_numa_node_id');
SELECT fn_db_drop_column('vm_vds_numa_node_map','is_pinned');
