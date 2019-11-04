--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6


--
-- Data for Name: gluster_services; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO gluster_services (id, service_type, service_name) VALUES ('00000003-0003-0003-0003-00000000004f', 'GLUSTER', 'glusterd');
INSERT INTO gluster_services (id, service_type, service_name) VALUES ('00000004-0004-0004-0004-00000000013e', 'GLUSTER_SWIFT', 'gluster-swift-proxy');
INSERT INTO gluster_services (id, service_type, service_name) VALUES ('00000005-0005-0005-0005-000000000180', 'GLUSTER_SWIFT', 'gluster-swift-container');
INSERT INTO gluster_services (id, service_type, service_name) VALUES ('00000006-0006-0006-0006-00000000017d', 'GLUSTER_SWIFT', 'gluster-swift-object');
INSERT INTO gluster_services (id, service_type, service_name) VALUES ('00000007-0007-0007-0007-000000000332', 'GLUSTER_SWIFT', 'gluster-swift-account');
INSERT INTO gluster_services (id, service_type, service_name) VALUES ('00000008-0008-0008-0008-0000000001b6', 'GLUSTER_SWIFT', 'memcached');
INSERT INTO gluster_services (id, service_type, service_name) VALUES ('00000009-0009-0009-0009-0000000002a2', 'SMB', 'smb');


--
-- PostgreSQL database dump complete
--

