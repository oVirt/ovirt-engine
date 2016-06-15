SELECT fn_db_add_column('cluster', 'skip_fencing_if_gluster_bricks_up', 'BOOLEAN DEFAULT false');
SELECT fn_db_add_column('cluster', 'skip_fencing_if_gluster_quorum_not_met', 'BOOLEAN DEFAULT false');