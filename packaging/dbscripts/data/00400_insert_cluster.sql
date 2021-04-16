--
-- PostgreSQL database dump
--

--
-- Data for Name: cluster; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO cluster (cluster_id, name, description, cpu_name, _create_date, _update_date, storage_pool_id, max_vds_memory_over_commit, compatibility_version, transparent_hugepages, migrate_on_error, virt_service, gluster_service, count_threads_as_cores, emulated_machine, trusted_service, tunnel_migration, cluster_policy_id, cluster_policy_custom_properties, enable_balloon, free_text_comment, detect_emulated_machine, architecture, optimization_type, spice_proxy, ha_reservation, enable_ksm, serial_number_policy, custom_serial_number, optional_reason, required_rng_sources, skip_fencing_if_sd_active, skip_fencing_if_connectivity_broken, hosts_with_broken_connectivity_threshold, fencing_enabled, is_auto_converge, is_migrate_compressed, maintenance_reason_required, gluster_tuned_profile, gluster_cli_based_snapshot_scheduled, ksm_merge_across_nodes) VALUES ('58cfb470-03b9-01d0-03b9-0000000001e7', 'Default', 'The default server cluster', NULL, now(), NULL, '58cfb470-02f3-03d7-0386-0000000003bc', 100, '4.6', true, 1, true, false, false, NULL, false, false, 'b4ed2332-a7ac-4d5f-9596-99a439cb2812', NULL, true, NULL, true, 0, 0, NULL, false, true, NULL, NULL, false, 'RANDOM', false, false, 50, true, NULL, NULL, false, NULL, true, true);


--
-- PostgreSQL database dump complete
--

