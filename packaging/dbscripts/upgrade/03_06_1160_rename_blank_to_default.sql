-- null after the decouple_blank_from_cluster ran, 00000000-0000-0000-0000-000000000000 before.
-- this way it is not dependent on the order
update vm_static set vm_name = 'Default', description = 'Default template' where vm_guid='00000000-0000-0000-0000-000000000000' or vm_guid is null;
