-- Add gluster peer status to 'gluster_server' table.
SELECT fn_db_add_column('gluster_server', 'peer_status', 'VARCHAR(50)');
UPDATE gluster_server SET peer_status = 'CONNECTED';
ALTER TABLE gluster_server ALTER COLUMN peer_status SET NOT NULL;
