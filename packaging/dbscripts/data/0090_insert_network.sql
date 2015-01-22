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
-- Data for Name: network; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO network (id, name, description, type, addr, subnet, gateway, vlan_id, stp, storage_pool_id, mtu, vm_network) VALUES ('00000000-0000-0000-0000-000000000009', 'ovirtmgmt', 'Management Network', NULL, NULL, NULL, NULL, NULL, false, '00000002-0002-0002-0002-00000000021c', NULL, true);


--
-- PostgreSQL database dump complete
--

