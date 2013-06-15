ALTER TABLE vm_interface ALTER COLUMN network_name DROP NOT NULL;

UPDATE vm_interface
SET    network_name = NULL
WHERE  network_name = '';

