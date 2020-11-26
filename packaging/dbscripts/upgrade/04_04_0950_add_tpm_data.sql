CREATE TABLE vm_external_data (
    device_id UUID NOT NULL,
    vm_id UUID NOT NULL,
    tpm_data TEXT,
    PRIMARY KEY (device_id, vm_id),
    FOREIGN KEY (device_id, vm_id) REFERENCES vm_device(device_id, vm_id) ON DELETE CASCADE
);
