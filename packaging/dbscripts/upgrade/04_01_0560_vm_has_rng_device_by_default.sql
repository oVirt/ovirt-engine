-- It adds new random number generator (rng) device for all non-cluster-aware vm-like entities (Blank template and
-- instance types). It causes all new VMs to inherit such device unless explicitly specified otherwise.
-- The change is only performed for predefined entities. The assumption is that if user creates a custom
-- instance type without rng device it was an intentional decision.
INSERT INTO vm_device(
    device_id,
    vm_id,
    type,
    device,
    address,
    boot_order,
    spec_params,
    is_managed,
    is_plugged,
    is_readonly,
    _create_date,
    _update_date,
    alias,
    custom_properties,
    snapshot_id,
    logical_name,
    host_device)
SELECT
    uuid_generate_v1(),
    vm_static.vm_guid,
    'rng',
    'virtio',
    '',
    0,
    '{"source" : "urandom"}',
    TRUE,
    TRUE,
    FALSE,
    current_timestamp,
    NULL,
    '',
    NULL,
    NULL,
    NULL,
    NULL
FROM vm_static
WHERE vm_static.cluster_id IS NULL
    AND NOT exists(
        SELECT 1
        FROM vm_device
        WHERE vm_device.vm_id = vm_static.vm_guid
            AND vm_device.type = 'rng')
    AND (vm_static.vm_guid = '00000000-0000-0000-0000-000000000000' -- Blank template
         OR (vm_static.entity_type = 'INSTANCE_TYPE'
             AND vm_static.vm_name IN ('Tiny', 'Small', 'Medium', 'Large', 'XLarge')));
