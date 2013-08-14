ALTER TABLE vm_interface DROP CONSTRAINT fk_vm_interface_vnic_profile_id;
ALTER TABLE vm_interface ADD CONSTRAINT fk_vm_interface_vnic_profile_id FOREIGN KEY (vnic_profile_id) REFERENCES vnic_profiles(id) ON DELETE SET NULL;

