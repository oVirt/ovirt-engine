SELECT fn_db_add_column('cluster',
                        'is_migrate_encrypted',
                        'boolean');

SELECT fn_db_add_column('vm_static',
                        'is_migrate_encrypted',
                        'boolean');
