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
-- Data for Name: storage_pool; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO storage_pool (id, name, description, storage_pool_type, storage_pool_format_type, status, master_domain_version, spm_vds_id, compatibility_version, _create_date, _update_date, quota_enforcement_type) VALUES ('00000002-0002-0002-0002-00000000021c', 'Default', 'The default Data Center', 1, NULL, 0, 0, NULL, '3.6', '2015-01-21 15:14:26.872109+02', NULL, NULL);


--
-- PostgreSQL database dump complete
--

