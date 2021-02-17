CREATE TABLE vm_nvram_data (
    vm_id UUID PRIMARY KEY REFERENCES vm_static(vm_guid) ON DELETE CASCADE,
    nvram_data TEXT
);
