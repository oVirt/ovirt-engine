UPDATE vm_device SET is_plugged = FALSE WHERE is_plugged IS NULL;

ALTER TABLE vm_device ALTER COLUMN is_plugged SET DEFAULT FALSE;

ALTER TABLE vm_device ALTER COLUMN is_plugged SET NOT NULL;
