--
-- PostgreSQL database dump
--


--
-- Data for Name: vds_groups; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO vds_groups (vds_group_id, name, description, cpu_name, _create_date, _update_date, storage_pool_id, max_vds_memory_over_commit, compatibility_version, transparent_hugepages, migrate_on_error, virt_service, gluster_service, count_threads_as_cores, emulated_machine, trusted_service, tunnel_migration, cluster_policy_id, cluster_policy_custom_properties, enable_balloon, free_text_comment, detect_emulated_machine, architecture, optimization_type, spice_proxy, ha_reservation, enable_ksm, serial_number_policy, custom_serial_number, optional_reason, required_rng_sources) VALUES ('00000002-0002-0002-0002-00000000017a', 'Default', 'The default server cluster', NULL, '2016-07-05 12:03:14.797477+03', NULL, '00000001-0001-0001-0001-000000000311', 100, '4.1', true, 1, true, false, false, NULL, false, false, 'b4ed2332-a7ac-4d5f-9596-99a439cb2812', NULL, false, NULL, true, 0, 0, NULL, false, true, NULL, NULL, false, 'RANDOM');


--
-- PostgreSQL database dump complete
--

