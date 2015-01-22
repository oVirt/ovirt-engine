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
-- Data for Name: schema_version; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO schema_version(version,script,checksum,installed_by,ended_at,state,current)
  values ('03030000','upgrade/03_03_0000_set_version.sql','0','engine',now(),'INSTALLED',true);


--
-- PostgreSQL database dump complete
--

