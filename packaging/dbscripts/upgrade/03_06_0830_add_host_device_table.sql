CREATE TABLE host_device (
    host_id UUID NOT NULL,
    device_name VARCHAR(255) NOT NULL,
    parent_device_name VARCHAR(255) NOT NULL,
    capability VARCHAR(32) NOT NULL,
    iommu_group INTEGER,
    product_name VARCHAR(255),
    product_id VARCHAR(255),
    vendor_name VARCHAR(255),
    vendor_id VARCHAR(255),
    physfn VARCHAR(255),
    total_vfs INTEGER,
    vm_id UUID
);

SELECT fn_db_create_constraint('host_device', 'host_device_pk', 'PRIMARY KEY (host_id, device_name)');
SELECT fn_db_create_constraint('host_device', 'fk_host_device_parent_name', 'FOREIGN KEY (host_id, parent_device_name) REFERENCES host_device(host_id, device_name) DEFERRABLE INITIALLY IMMEDIATE');
SELECT fn_db_create_constraint('host_device', 'fk_host_device_physfn', 'FOREIGN KEY (host_id, physfn) REFERENCES host_device(host_id, device_name) DEFERRABLE INITIALLY IMMEDIATE');
SELECT fn_db_create_constraint('host_device', 'fk_host_device_host_id', 'FOREIGN KEY (host_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE');
SELECT fn_db_create_constraint('host_device', 'fk_host_device_vm_id', 'FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE SET NULL');
