--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6


--
-- Data for Name: schema_version; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO schema_version(version,script,checksum,installed_by,ended_at,state,current)
  values ('04010000','upgrade/04_01_0000_set_version.sql','0','engine',now(),'INSTALLED',true);

