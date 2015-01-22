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
-- Data for Name: event_map; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDC_STOP', 'VDC_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('IRS_FAILURE', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('IRS_DISK_SPACE_LOW', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('IRS_DISK_SPACE_LOW_ERROR', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_FAILURE', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_VDS_MAINTENANCE', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_VDS_MAINTENANCE_MIGRATION_FAILED', 'USER_VDS_MAINTENANCE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_ACTIVATE_FAILED', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_RECOVER_FAILED', 'VDS_RECOVER');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SLOW_STORAGE_RESPONSE_TIME', 'VDS_ACTIVATE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_APPROVE_FAILED', 'VDS_APPROVE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_INSTALL_FAILED', 'VDS_INSTALL');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_FAILURE', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_MIGRATION_START', 'VM_MIGRATION_DONE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_MIGRATION_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_MIGRATION_FAILED_FROM_TO', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_NOT_RESPONDING', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('DWH_STOPPED', 'DWH_STARTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('DWH_ERROR', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_TIME_DRIFT_ALERT', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_CREATE', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_CREATE_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_START', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_START_FAILED', 'GLUSTER_VOLUME_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STOP', 'GLUSTER_VOLUME_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STOP_FAILED', 'GLUSTER_VOLUME_STOP');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_SET', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_SET_FAILED', 'GLUSTER_VOLUME_OPTION_SET');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTIONS_RESET', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTIONS_RESET_FAILED', 'GLUSTER_VOLUME_OPTIONS_RESET');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_DELETE', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_DELETE_FAILED', 'GLUSTER_VOLUME_DELETE');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_START', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_START_FAILED', 'GLUSTER_VOLUME_REBALANCE_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REMOVE_BRICKS', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REMOVE_BRICKS_FAILED', 'GLUSTER_VOLUME_REMOVE_BRICKS');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REPLACE_BRICK_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REPLACE_BRICK_START', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED', 'GLUSTER_VOLUME_REPLACE_BRICK_START');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_ADD_BRICK', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_ADD_BRICK_FAILED', 'GLUSTER_VOLUME_ADD_BRICK');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVER_REMOVE_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVER_ADD_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_CREATED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_DELETED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_SET_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_RESET_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_PROPERTIES_CHANGED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_BRICK_ADDED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_BRICK_REMOVED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVER_REMOVED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('HA_VM_RESTART_FAILED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('HA_VM_FAILED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('SYSTEM_DEACTIVATED_STORAGE_DOMAIN', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL_IFACE_DOWN', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL_DOMAIN', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STARTED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STOPPED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_MEM_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_NETWORK_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_CPU_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_SWAP_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_LOW_SWAP', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_CHANGED_FROM_CLI', 'UNASSIGNED');


--
-- PostgreSQL database dump complete
--

