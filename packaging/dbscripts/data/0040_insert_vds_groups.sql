--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Data for Name: vds_groups; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO vds_groups (vds_group_id, name, description, cpu_name, _create_date, _update_date, selection_algorithm, high_utilization, low_utilization, cpu_over_commit_duration_minutes, storage_pool_id, max_vds_memory_over_commit, compatibility_version, transparent_hugepages, migrate_on_error, virt_service, gluster_service, count_threads_as_cores) VALUES ('00000001-0001-0001-0001-0000000000d6', 'Default', 'The default server cluster', NULL, '2015-01-21 15:14:26.872109+02', NULL, 0, 75, 0, 2, '00000002-0002-0002-0002-00000000021c', 100, '3.6', true, 1, true, false, false);


--
-- PostgreSQL database dump complete
--

