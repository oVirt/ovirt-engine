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
    uuid_generate_v1(), -- new uuid
    vm_static.vm_guid,
    'controller',
    'virtio-scsi',
    '',
    0,
    '{}',
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
            AND vm_device.type = 'controller'
            AND vm_device.device = 'virtio-scsi')
    AND (vm_static.vm_guid = '00000000000000000000000000000000'   -- blank template
         OR (vm_static.entity_type = 'INSTANCE_TYPE'
             AND vm_static.vm_name IN ('Tiny', 'Small', 'Medium', 'Large', 'XLarge')));
