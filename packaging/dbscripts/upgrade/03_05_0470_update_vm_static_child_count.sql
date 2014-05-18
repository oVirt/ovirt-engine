UPDATE vm_static as a
SET child_count =(SELECT COUNT(*) FROM vm_static as b WHERE a.vm_guid = b.vmt_guid and entity_type = 'VM');
