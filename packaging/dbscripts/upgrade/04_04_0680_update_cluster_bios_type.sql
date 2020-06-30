UPDATE cluster
SET bios_type = 1
WHERE bios_type = 0 AND compatibility_version <= '4.3';

