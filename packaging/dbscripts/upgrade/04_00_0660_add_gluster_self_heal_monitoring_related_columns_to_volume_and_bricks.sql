-- Add gluster self-heal related columns to 'gluster_volume_bricks' table.
SELECT fn_db_add_column('gluster_volume_bricks', 'unsynced_entries', 'integer');
SELECT fn_db_add_column('gluster_volume_bricks', 'unsynced_entries_history', 'text');