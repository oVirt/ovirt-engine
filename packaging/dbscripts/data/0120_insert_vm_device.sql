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
-- Data for Name: vm_device; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias) VALUES ('00000006-0006-0006-0006-000000000006', '00000000-0000-0000-0000-000000000000', 'video', 'cirrus', '', NULL, '{ "vram" : "65536" }', true, NULL, false, '2013-12-25 22:54:23.416857+02', NULL, '');


--
-- PostgreSQL database dump complete
--

