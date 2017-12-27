UPDATE vm_static
SET max_memory_size_mb = mem_size_mb
WHERE max_memory_size_mb < mem_size_mb;
