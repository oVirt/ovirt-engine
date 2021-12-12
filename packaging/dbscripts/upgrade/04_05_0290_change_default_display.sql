-- Removing all graphics from blank template
DELETE FROM vm_device
WHERE type = 'graphics'
    AND vm_id = '00000000-0000-0000-0000-000000000000'
    AND EXISTS (select 1 from vm_static where vm_guid = '00000000-0000-0000-0000-000000000000' AND default_display_type = 1);

-- Add VNC graphics to blank template
INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly)
SELECT uuid_generate_v1(), '00000000-0000-0000-0000-000000000000', 'graphics', 'vnc', '', '', true, true, false
WHERE EXISTS (select 1 from vm_static where vm_guid = '00000000-0000-0000-0000-000000000000' AND default_display_type = 1);

-- Removing all video devices from blank template
DELETE FROM vm_device
WHERE type = 'video'
    AND vm_id = '00000000-0000-0000-0000-000000000000'
    AND EXISTS (select 1 from vm_static where vm_guid = '00000000-0000-0000-0000-000000000000' AND default_display_type = 1);

-- Add VGA video device to blank template
INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly)
SELECT uuid_generate_v1(), '00000000-0000-0000-0000-000000000000', 'video', 'vga', '', '{"vram" : "16384"}', true, true, false
WHERE EXISTS (select 1 from vm_static where vm_guid = '00000000-0000-0000-0000-000000000000' AND default_display_type = 1);

-- Change InstanceType's and Blank template default display type from QXL (1) to VGA (2)
UPDATE vm_static
SET default_display_type = 2
WHERE vm_guid in ('00000000-0000-0000-0000-000000000000',
    '00000003-0003-0003-0003-0000000000be',
    '00000005-0005-0005-0005-0000000002e6',
    '00000007-0007-0007-0007-00000000010a',
    '00000009-0009-0009-0009-0000000000f1',
    '0000000b-000b-000b-000b-00000000021f')
    AND default_display_type = 1;
