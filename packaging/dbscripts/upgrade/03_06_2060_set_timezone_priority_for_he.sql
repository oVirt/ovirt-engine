-- Fix time zone and priority fields for imported hosted engine VM
-- the old importer forgot to set those fields
UPDATE vm_static
SET priority = 1
WHERE priority < 1
    AND origin = 6;

UPDATE vm_static
SET time_zone = 'Etc/GMT'
WHERE time_zone is null
    AND origin = 6;
