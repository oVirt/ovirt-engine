--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6


--
-- Data for Name: vm_device; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('58ca604e-02e8-001c-022c-000000000012', '00000000-0000-0000-0000-000000000000', 'controller', 'virtio-serial', '', 0, '', true, true, false, '2017-03-16 11:52:14.524893+02', NULL, '', NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('58ca6050-0134-00d6-0053-000000000388', '00000003-0003-0003-0003-0000000000be', 'graphics', 'spice', '', NULL, '', true, true, false, '2017-03-16 11:52:16.685305+02', NULL, NULL, NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('58ca6050-01db-019e-02d8-000000000342', '00000005-0005-0005-0005-0000000002e6', 'graphics', 'spice', '', NULL, '', true, true, false, '2017-03-16 11:52:16.685305+02', NULL, NULL, NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('58ca6050-0373-03bb-0259-00000000016a', '00000009-0009-0009-0009-0000000000f1', 'graphics', 'spice', '', NULL, '', true, true, false, '2017-03-16 11:52:16.685305+02', NULL, NULL, NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('58ca6050-00d6-0109-0077-0000000003b5', '0000000b-000b-000b-000b-00000000021f', 'graphics', 'spice', '', NULL, '', true, true, false, '2017-03-16 11:52:16.685305+02', NULL, NULL, NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('58ca6050-0219-02f8-01d3-0000000002e9', '00000000-0000-0000-0000-000000000000', 'graphics', 'spice', '', NULL, '', true, true, false, '2017-03-16 11:52:16.685305+02', NULL, NULL, NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('58ca6050-03d7-00e7-0062-00000000018f', '00000007-0007-0007-0007-00000000010a', 'graphics', 'spice', '', NULL, '', true, true, false, '2017-03-16 11:52:16.685305+02', NULL, NULL, NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('00000006-0006-0006-0006-000000000006', '00000000-0000-0000-0000-000000000000', 'video', 'qxl', '', NULL, '{ "vram" : "65536" }', true, NULL, false, '2013-12-25 22:54:23.416857+02', NULL, '58ca7b19-0071-00c0-01d6-000000000212', NULL, NULL, NULL, false, NULL);
INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias, custom_properties, snapshot_id, logical_name, is_using_scsi_reservation, host_device) VALUES ('1362db26-fedf-11e9-9bb0-8c1645ce738e', '00000000-0000-0000-0000-000000000000', 'balloon', 'memballoon', '', 0, '{"model" : "virtio"}', true, true, true, '2019-11-04 10:42:46.789428+02', NULL, NULL, NULL, NULL, NULL, false, NULL);


--
-- PostgreSQL database dump complete
--

