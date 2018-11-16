-- Add VNC device to Blank template and base Instance Types

INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly) VALUES (uuid_generate_v1(), '00000000-0000-0000-0000-000000000000', 'graphics', 'vnc', '', '', true, true, false);
INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly) VALUES (uuid_generate_v1(), '00000003-0003-0003-0003-0000000000be', 'graphics', 'vnc', '', '', true, true, false);
INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly) VALUES (uuid_generate_v1(), '00000005-0005-0005-0005-0000000002e6', 'graphics', 'vnc', '', '', true, true, false);
INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly) VALUES (uuid_generate_v1(), '00000007-0007-0007-0007-00000000010a', 'graphics', 'vnc', '', '', true, true, false);
INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly) VALUES (uuid_generate_v1(), '00000009-0009-0009-0009-0000000000f1', 'graphics', 'vnc', '', '', true, true, false);
INSERT INTO vm_device (device_id, vm_id, type, device, address, spec_params, is_managed, is_plugged, is_readonly) VALUES (uuid_generate_v1(), '0000000b-000b-000b-000b-00000000021f', 'graphics', 'vnc', '', '', true, true, false);

