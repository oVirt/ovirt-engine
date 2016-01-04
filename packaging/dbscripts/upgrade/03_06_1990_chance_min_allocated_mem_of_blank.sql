UPDATE vm_static
SET min_allocated_mem = mem_size_mb
WHERE vm_guid = '00000000-0000-0000-0000-000000000000'
    AND min_allocated_mem = 0;
