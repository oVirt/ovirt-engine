select fn_db_add_column('vds_dynamic', 'cpu_topology', 'JSONB NULL');
select fn_db_add_column('vds_dynamic', 'vdsm_cpus_affinity', 'varchar(256)');