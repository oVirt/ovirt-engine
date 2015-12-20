--
-- PostgreSQL database dump
--



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
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STARTED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_STOPPED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_MEM_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_NETWORK_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_CPU_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_HIGH_SWAP_USE', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_LOW_SWAP', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_CHANGED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_ENABLE', 'GLUSTER_HOOK_ENABLE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_ENABLE_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_DISABLE', 'GLUSTER_HOOK_DISABLE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_DISABLE_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_DETECTED_NEW', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_CONFLICT_DETECTED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_DETECTED_DELETE', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_ADDED', 'GLUSTER_HOOK_ADD_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_ADD_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_REMOVED', 'GLUSTER_HOOK_REMOVE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_HOOK_REMOVE_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_STARTED', 'GLUSTER_SERVICE_START_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_START_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_STOPPED', 'GLUSTER_SERVICE_STOP_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_STOP_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_RESTARTED', 'GLUSTER_SERVICE_RESTART_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVICE_RESTART_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_UNTRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_STATUS_RESTORED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_SERVER_REMOVE', 'GLUSTER_SERVER_REMOVE_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_ADDED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTION_MODIFIED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_OPTIONS_RESET_ALL', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_PROFILE_START', 'GLUSTER_VOLUME_PROFILE_START_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_PROFILE_START_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_PROFILE_STOP', 'GLUSTER_VOLUME_PROFILE_STOP_FAILED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_PROFILE_STOP_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_STOP', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_STOP_FAILED', 'GLUSTER_VOLUME_REBALANCE_STOP');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('START_REMOVING_GLUSTER_VOLUME_BRICKS', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED', 'START_REMOVING_GLUSTER_VOLUME_BRICKS');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_BRICK_STATUS_CHANGED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_FINISHED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL_DOMAIN', 'VDS_DETECTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REMOVE_BRICKS_STOP', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_CONSOLE_CONNECTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_CONSOLE_DISCONNECTED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_SET_TICKET', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VM_DOWN_ERROR', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_INITIATED_RUN_VM_FAIL', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('CLUSTER_ALERT_HA_RESERVATION', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_INITIATED_RUN_VM_FAILED', '');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL', 'VDS_DETECTED');
INSERT INTO event_map (event_up_name, event_down_name) VALUES ('VDS_SET_NONOPERATIONAL_IFACE_DOWN', 'VDS_DETECTED');


--
-- PostgreSQL database dump complete
--

