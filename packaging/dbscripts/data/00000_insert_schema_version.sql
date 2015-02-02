--
-- PostgreSQL database dump
--


--
-- Data for Name: schema_version; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO schema_version(version,script,checksum,installed_by,ended_at,state,current)
  values ('03040000','upgrade/03_04_0000_set_version.sql','0','engine',now(),'INSTALLED',true);


--
-- PostgreSQL database dump complete
--

