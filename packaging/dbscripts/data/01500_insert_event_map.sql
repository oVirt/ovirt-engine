--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6


--
-- Data for Name: event_map; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDC_STOP', 'VDC_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_VDS_MAINTENANCE', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_VDS_MAINTENANCE_MIGRATION_FAILED', 'USER_VDS_MAINTENANCE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_ACTIVATE_FAILED', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_RECOVER_FAILED', 'VDS_RECOVER');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SLOW_STORAGE_RESPONSE_TIME', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_APPROVE_FAILED', 'VDS_APPROVE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_INSTALL_FAILED', 'VDS_INSTALL');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_MIGRATION_START', 'VM_MIGRATION_DONE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('DWH_STOPPED', 'DWH_STARTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_START_FAILED', 'GLUSTER_VOLUME_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STOP', 'GLUSTER_VOLUME_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STOP_FAILED', 'GLUSTER_VOLUME_STOP');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_SET_FAILED', 'GLUSTER_VOLUME_OPTION_SET');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTIONS_RESET_FAILED', 'GLUSTER_VOLUME_OPTIONS_RESET');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_DELETE_FAILED', 'GLUSTER_VOLUME_DELETE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_START_FAILED', 'GLUSTER_VOLUME_REBALANCE_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REMOVE_BRICKS_FAILED', 'GLUSTER_VOLUME_REMOVE_BRICKS');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED', 'GLUSTER_VOLUME_REPLACE_BRICK_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_ADD_BRICK_FAILED', 'GLUSTER_VOLUME_ADD_BRICK');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_ENABLE', 'GLUSTER_HOOK_ENABLE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_DISABLE', 'GLUSTER_HOOK_DISABLE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_ADDED', 'GLUSTER_HOOK_ADD_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_REMOVED', 'GLUSTER_HOOK_REMOVE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_STARTED', 'GLUSTER_SERVICE_START_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_STOPPED', 'GLUSTER_SERVICE_STOP_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_RESTARTED', 'GLUSTER_SERVICE_RESTART_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVER_REMOVE', 'GLUSTER_SERVER_REMOVE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_PROFILE_START', 'GLUSTER_VOLUME_PROFILE_START_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_PROFILE_STOP', 'GLUSTER_VOLUME_PROFILE_STOP_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_STOP_FAILED', 'GLUSTER_VOLUME_REBALANCE_STOP');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED', 'START_REMOVING_GLUSTER_VOLUME_BRICKS');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL_DOMAIN', 'VDS_DETECTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL', 'VDS_DETECTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL_IFACE_DOWN', 'VDS_DETECTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_VDS_MAINTENANCE_MANUAL_HA', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_FAILURE', 'VDS_DETECTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('HOST_INTERFACE_STATE_DOWN', 'HOST_INTERFACE_STATE_UP');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('HOST_BOND_SLAVE_STATE_DOWN', 'HOST_BOND_SLAVE_STATE_UP');


--
-- PostgreSQL database dump complete
--

