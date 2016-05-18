-- Create new table
CREATE TABLE disk_vm_element (
    disk_id UUID NOT NULL,
    vm_id UUID NOT NULL,
    is_boot BOOLEAN DEFAULT FALSE NOT NULL,
    disk_interface VARCHAR(32) NOT NULL
);


-- Create constraints
ALTER TABLE ONLY disk_vm_element ADD CONSTRAINT fk_disk_vm_element_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;
ALTER TABLE ONLY disk_vm_element ADD CONSTRAINT fk_disk_vm_element_base_disks FOREIGN KEY (disk_id) REFERENCES base_disks(disk_id) ON DELETE CASCADE;
ALTER TABLE ONLY disk_vm_element ADD CONSTRAINT pk_disk_vm_element PRIMARY KEY (vm_id, disk_id);


-- Copy boot and interface values for disks attached to VMs
INSERT INTO disk_vm_element (disk_id, vm_id, is_boot, disk_interface)
SELECT disk.disk_id, device.vm_id, disk.boot, disk.disk_interface
FROM vm_device device
JOIN base_disks disk ON disk.disk_id = device.device_id;


-- Drop original columns
SELECT fn_db_drop_column('base_disks', 'boot');
SELECT fn_db_drop_column('base_disks', 'disk_interface');
