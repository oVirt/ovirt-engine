SELECT fn_db_add_column('vds_dynamic', 'cpu_topology', 'JSONB NULL');
SELECT fn_db_add_column('vds_dynamic', 'vdsm_cpus_affinity', 'VARCHAR(256)');
