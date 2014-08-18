SELECT fn_db_add_column('vds_groups', 'skip_fencing_if_connectivity_broken', 'boolean DEFAULT false');
SELECT fn_db_add_column('vds_groups', 'hosts_with_broken_connectivity_threshold', 'smallint DEFAULT 50');
