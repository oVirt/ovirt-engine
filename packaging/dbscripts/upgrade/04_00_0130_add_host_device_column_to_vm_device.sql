SELECT fn_db_add_column('vm_device', 'host_device', 'VARCHAR(255)');

UPDATE vm_dynamic
SET hash = null
WHERE vm_guid IN (
        SELECT vm_id
        FROM vm_device
        WHERE type = 'interface'
            AND device = 'hostdev'
            AND is_plugged = true
        );
