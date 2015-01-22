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
-- Data for Name: dwh_history_timekeeping; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO dwh_history_timekeeping (var_name, var_value, var_datetime) VALUES ('lastSync', NULL, '2000-01-01 00:00:00+02');
INSERT INTO dwh_history_timekeeping (var_name, var_value, var_datetime) VALUES ('lastFullHostCheck', NULL, '2000-01-01 00:00:00+02');


--
-- PostgreSQL database dump complete
--

