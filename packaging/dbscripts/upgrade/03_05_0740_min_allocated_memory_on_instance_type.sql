update vm_static s set min_allocated_mem = (select mem_size_mb from vm_static where vm_guid = s.vm_guid) where s.min_allocated_mem = 0 and entity_type = 'INSTANCE_TYPE';
