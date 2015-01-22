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
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO users (user_id, name, surname, domain, username, groups, department, role, user_icon_path, desktop_device, email, note, status, session_count, last_admin_check_status, group_ids) VALUES ('fdfc627c-d875-11e0-90f0-83df133b58cc', 'admin', NULL, 'internal', 'admin@internal', '', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, false, NULL);


--
-- PostgreSQL database dump complete
--

