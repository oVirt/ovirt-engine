SELECT fn_db_add_column('gluster_volumes', 'disperse_count', 'INTEGER NOT NULL DEFAULT 0');
SELECT fn_db_add_column('gluster_volumes', 'redundancy_count', 'INTEGER NOT NULL DEFAULT 0');
