SELECT fn_db_add_column('cluster', 'migration_bandwidth_limit_type', 'VARCHAR(16) NOT NULL DEFAULT ''AUTO''');
SELECT fn_db_add_column('cluster', 'custom_migration_bandwidth_limit', 'integer NULL');
SELECT fn_db_create_constraint('cluster',
                               'check_cluster_custom_migration_bandwidth_set',
                               'CHECK (migration_bandwidth_limit_type != ''CUSTOM''
                                   OR custom_migration_bandwidth_limit IS NOT NULL)');
SELECT fn_db_create_constraint('cluster',
                               'check_cluster_migration_bandwidth_limit_type_enum',
                               'CHECK (migration_bandwidth_limit_type = ''AUTO''
                                   OR migration_bandwidth_limit_type = ''VDSM_CONFIG''
                                   OR migration_bandwidth_limit_type = ''CUSTOM'')');



