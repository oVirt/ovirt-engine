ALTER TABLE vm_interface ADD  CONSTRAINT FK_vm_interface_vm_static_template FOREIGN KEY(vmt_guid)
REFERENCES vm_static(vm_guid) ON DELETE CASCADE;
