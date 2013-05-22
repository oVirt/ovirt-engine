
update vm_static set mem_size_mb = 1024 where vm_guid = '00000000-0000-0000-0000-000000000000' and mem_size_mb < 1024;

