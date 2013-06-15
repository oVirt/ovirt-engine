select fn_db_add_column('vds_groups', 'count_threads_as_cores', 'BOOLEAN NOT NULL DEFAULT FALSE');

select fn_db_add_column('vds_dynamic', 'cpu_threads', 'INTEGER');
