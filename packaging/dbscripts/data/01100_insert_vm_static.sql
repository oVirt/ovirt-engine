--
-- PostgreSQL database dump
--


--
-- Data for Name: vm_static; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO vm_static (vm_guid, vm_name, mem_size_mb, vmt_guid, os, description, vds_group_id, creation_date, num_of_monitors, is_initialized, is_auto_suspend, num_of_sockets, cpu_per_socket, usb_policy, time_zone, is_stateless, fail_back, _create_date, _update_date, dedicated_vm_for_vds, auto_startup, vm_type, nice_level, default_boot_sequence, default_display_type, priority, iso_path, origin, initrd_url, kernel_url, kernel_params, migration_support, userdefined_properties, predefined_properties, min_allocated_mem, entity_type, child_count, template_status, quota_id, allow_console_reconnect, cpu_pinning, is_smartcard_enabled, host_cpu_flags, db_generation, is_delete_protected, is_disabled, is_run_and_pause, created_by_user_id, tunnel_migration, free_text_comment, single_qxl_pci, cpu_shares, vnc_keyboard_layout) VALUES ('00000000-0000-0000-0000-000000000000', 'Blank', 1024, '00000000-0000-0000-0000-000000000000', 0, 'Blank template', '00000001-0001-0001-0001-0000000000d6', '2008-04-01 00:00:00+03', 1, NULL, false, 1, 1, 1, NULL, NULL, false, '2013-12-25 15:31:54.367179+02', '2013-12-25 15:31:53.239308+02', NULL, NULL, 0, 0, 0, 1, 0, '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, 'TEMPLATE', 0, 0, NULL, false, NULL, false, false, 1, false, false, false, NULL, NULL, NULL, false, 0, NULL);


--
-- PostgreSQL database dump complete
--

