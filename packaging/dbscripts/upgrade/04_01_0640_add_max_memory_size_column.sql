SELECT fn_db_add_column('vm_static', 'max_memory_size_mb', 'INTEGER NULL');

-- 1048576 is an old default from *MaxMemorySizeInMB config values
UPDATE vm_static
    SET max_memory_size_mb = LEAST(4 * mem_size_mb::bigint, 1048576);

ALTER table vm_static
    ALTER COLUMN max_memory_size_mb SET NOT NULL;
