--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6


--
-- Data for Name: gluster_config_master; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('gluster_log_file', 'The path to the geo-replication glusterfs log file.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('gluster-log-level', 'The log level for glusterfs processes.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('log_file', 'The path to the geo-replication log file.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('log-level', 'The log level for geo-replication.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('ssh_command', 'The SSH command to connect to the remote machine', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('rsync-command', 'The rsync command to use for synchronizing the files', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('use-tarssh', 'The use-tarssh command allows tar over Secure Shell protocol. Use this option to handle workloads of files that have not undergone edits.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('timeout', 'The timeout period in seconds.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('sync-jobs', 'The number of simultaneous files/directories that can be synchronized.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('ignore_deletes', 'If this option is set to 1, a file deleted on the master will not trigger a delete operation on the slave. As a result, the slave will remain as a superset of the master and can be used to recover the master in the event of a crash and/or accidental delete.', '3.5', 'true;false', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('checkpoint', 'Sets a checkpoint with the given option LABEL. If the option is set as now, then the current time will be used as the label.', '3.5', '', 'geo_replication');
INSERT INTO gluster_config_master (config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature) VALUES ('use_meta_volume', 'Meta volume for the geo-replication session', '3.5', 'false;true', 'geo_replication');


--
-- PostgreSQL database dump complete
--

