select fn_db_add_column('vm_static', 'balloon_enabled', 'BOOLEAN NOT NULL DEFAULT FALSE');

-- Updates existing instance types to true, setting each vm/template to true if the balloon device exists.
UPDATE vm_static
SET balloon_enabled = 'true'
WHERE entity_type = 'INSTANCE_TYPE'
    OR EXISTS (
        SELECT 1
        FROM vm_device
        WHERE vm_device.device = 'memballoon'
            AND vm_id = vm_guid
        );

-- Add balloon device to entities with false (without balloon device). This is since now the balloon device always
-- need to be presented. The new boolean on vm_static will indicate if we use MoM functionality of that device.
INSERT INTO vm_device (
    device_id,
    vm_id,
    device,
    type,
    address,
    spec_params,
    is_managed,
    is_plugged,
    is_readonly,
    alias,
    custom_properties,
    snapshot_id,
    logical_name,
    host_device
    )
SELECT
    uuid_generate_v1(), -- new uuid
    vm_static.vm_guid,
    'memballoon',
    'balloon',
    '',
    '{"model" : "virtio"}',
    TRUE,
    TRUE,
    TRUE,
    '',
    NULL,
    NULL,
    NULL,
    NULL
FROM vm_static
WHERE vm_static.balloon_enabled = FALSE;
