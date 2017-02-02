ALTER TABLE vm_static
ADD CONSTRAINT vm_static_max_memory_size_lower_bound CHECK (mem_size_mb <= max_memory_size_mb);
