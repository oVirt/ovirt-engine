INSERT INTO vdc_options(option_name, option_value, version) values ('AutoRegistrationDefaultVdsGroupID', uuid_generate_v1(),'general');

Create or replace FUNCTION insert_data()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_id_0000 UUID;
   v_storage_pool_id UUID;
   v_cluster_id UUID;
   v_id_0009 UUID;
BEGIN
   v_id_0000 := '00000000-0000-0000-0000-000000000000';
   v_storage_pool_id := uuid_generate_v1();
   v_cluster_id := option_value from vdc_options where option_name = 'AutoRegistrationDefaultVdsGroupID' and version = 'general';
   v_id_0009 := '00000000-0000-0000-0000-000000000009';


-- INSERT DATA to schema_version
INSERT INTO schema_version(version,script,checksum,installed_by,ended_at,state,current)
  values ('03030000','upgrade/03_03_0000_set_version.sql','0','engine',now(),'INSTALLED',true);

-- INSERT everyone TO ad_groups
INSERT into ad_groups (id,name,status,domain,distinguishedname)
 SELECT getGlobalIds('everyone'),
 'Everyone',
 1,
 '',
 ''
where not exists (
 SELECT id from ad_groups where id = getGlobalIds('everyone'));

--INSERTING DATA INTO TABLE storage_pool

INSERT INTO storage_pool (id,name,description,storage_pool_type,status,master_domain_version,compatibility_version) select v_storage_pool_id,'Default','The default Data Center',1,0,0,'3.6';
--INSERTING DATA INTO TABLE vds_groups

INSERT INTO vds_groups (vds_group_id,name,description,storage_pool_id,compatibility_version,transparent_hugepages) select v_cluster_id,'Default','The default server cluster',v_storage_pool_id,'3.6',true;

--INSERTING DATA INTO NETWORK table

INSERT INTO network (id,name,description,storage_pool_id) SELECT v_id_0009, 'ovirtmgmt', 'Management Network', v_storage_pool_id;
INSERT INTO network_cluster (network_id, cluster_id, status) SELECT v_id_0009,v_cluster_id,1;

INSERT INTO vm_static (vm_guid, vm_name, mem_size_mb, vmt_guid, os, description, vds_group_id, domain, creation_date, num_of_monitors, is_initialized, is_auto_suspend, num_of_sockets, cpu_per_socket, usb_policy, time_zone, is_stateless, fail_back, _create_date, _update_date, dedicated_vm_for_vds, auto_startup, vm_type, nice_level, default_boot_sequence, default_display_type, priority, iso_path, origin, initrd_url, kernel_url, kernel_params, migration_support, userdefined_properties, predefined_properties, min_allocated_mem, entity_type, child_count, template_status, quota_id, allow_console_reconnect, cpu_pinning, is_smartcard_enabled) VALUES ('00000000-0000-0000-0000-000000000000', 'Blank', 1024, '00000000-0000-0000-0000-000000000000', 0, 'Blank template', v_cluster_id , '', '2008-04-01 00:00:00+03', 1, NULL, false, 1, 1, 1, NULL, NULL, false, '2013-12-25 15:31:54.367179+02', '2013-12-25 15:31:53.239308+02', NULL, NULL, 0, 0, 0, 0, 0, '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, 'TEMPLATE', 0, 0, NULL, false, NULL, false);


INSERT INTO event_map(event_up_name, event_down_name) values('VDC_STOP', 'VDC_START');

INSERT INTO event_map(event_up_name, event_down_name) values('IRS_FAILURE', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('IRS_DISK_SPACE_LOW', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('IRS_DISK_SPACE_LOW_ERROR', 'UNASSIGNED');

INSERT INTO event_map(event_up_name, event_down_name) values('VDS_FAILURE', 'VDS_ACTIVATE');
INSERT INTO event_map(event_up_name, event_down_name) values('USER_VDS_MAINTENANCE', 'VDS_ACTIVATE');
INSERT INTO event_map(event_up_name, event_down_name) values('USER_VDS_MAINTENANCE_MIGRATION_FAILED', 'USER_VDS_MAINTENANCE');
INSERT INTO event_map(event_up_name, event_down_name) values('VDS_ACTIVATE_FAILED', 'VDS_ACTIVATE');
INSERT INTO event_map(event_up_name, event_down_name) values('VDS_RECOVER_FAILED', 'VDS_RECOVER');
INSERT INTO event_map(event_up_name, event_down_name) values('VDS_SLOW_STORAGE_RESPONSE_TIME', 'VDS_ACTIVATE');
INSERT INTO event_map(event_up_name, event_down_name) values('VDS_APPROVE_FAILED', 'VDS_APPROVE');
INSERT INTO event_map(event_up_name, event_down_name) values('VDS_INSTALL_FAILED', 'VDS_INSTALL');

INSERT INTO event_map(event_up_name, event_down_name) values('VM_FAILURE', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('VM_MIGRATION_START', 'VM_MIGRATION_DONE');
INSERT INTO event_map(event_up_name, event_down_name) values('VM_MIGRATION_FAILED', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('VM_MIGRATION_FAILED_FROM_TO', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('VM_NOT_RESPONDING', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('DWH_STOPPED', 'DWH_STARTED');
INSERT INTO event_map(event_up_name, event_down_name) values('DWH_ERROR', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('VDS_TIME_DRIFT_ALERT', '');


INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_CREATE', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_CREATE_FAILED', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_START', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_START_FAILED', 'GLUSTER_VOLUME_START');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_STOP', 'GLUSTER_VOLUME_START');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_STOP_FAILED', 'GLUSTER_VOLUME_STOP');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_OPTION_SET', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_OPTION_SET_FAILED', 'GLUSTER_VOLUME_OPTION_SET');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_OPTIONS_RESET', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_OPTIONS_RESET_FAILED', 'GLUSTER_VOLUME_OPTIONS_RESET');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_DELETE', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_DELETE_FAILED', 'GLUSTER_VOLUME_DELETE');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_REBALANCE_START', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_REBALANCE_START_FAILED', 'GLUSTER_VOLUME_REBALANCE_START');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_REMOVE_BRICKS', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_REMOVE_BRICKS_FAILED', 'GLUSTER_VOLUME_REMOVE_BRICKS');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_REPLACE_BRICK_FAILED', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_REPLACE_BRICK_START', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED', 'GLUSTER_VOLUME_REPLACE_BRICK_START');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_ADD_BRICK', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_ADD_BRICK_FAILED', 'GLUSTER_VOLUME_ADD_BRICK');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_SERVER_REMOVE_FAILED', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_SERVER_ADD_FAILED', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_CREATED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_DELETED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_OPTION_SET_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_OPTION_RESET_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_PROPERTIES_CHANGED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_BRICK_ADDED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_BRICK_REMOVED_FROM_CLI', 'UNASSIGNED');
INSERT INTO event_map(event_up_name, event_down_name) values('GLUSTER_SERVER_REMOVED_FROM_CLI', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('HA_VM_RESTART_FAILED', '');
insert into event_map(event_up_name, event_down_name) values('HA_VM_FAILED', '');
insert into event_map(event_up_name, event_down_name) values('SYSTEM_DEACTIVATED_STORAGE_DOMAIN', '');
insert into event_map(event_up_name, event_down_name) values('VDS_SET_NONOPERATIONAL', '');
insert into event_map(event_up_name, event_down_name) values('VDS_SET_NONOPERATIONAL_IFACE_DOWN', '');
insert into event_map(event_up_name, event_down_name) values('VDS_SET_NONOPERATIONAL_DOMAIN', '');
insert into event_map(event_up_name, event_down_name) values('SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM', '');
insert into event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_STARTED_FROM_CLI', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_STOPPED_FROM_CLI', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('VDS_HIGH_MEM_USE', '');
insert into event_map(event_up_name, event_down_name) values('VDS_HIGH_NETWORK_USE', '');
insert into event_map(event_up_name, event_down_name) values('VDS_HIGH_CPU_USE', '');
insert into event_map(event_up_name, event_down_name) values('VDS_HIGH_SWAP_USE', '');
insert into event_map(event_up_name, event_down_name) values('VDS_LOW_SWAP', '');
insert into event_map(event_up_name, event_down_name) values('GLUSTER_VOLUME_OPTION_CHANGED_FROM_CLI', 'UNASSIGNED');

INSERT INTO action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    SELECT 41, '3.1', '3.1';

INSERT INTO action_version_map values(158, '3.1','3.0');
INSERT INTO action_version_map (action_type,cluster_minimal_version,storage_pool_minimal_version) values (52,'3.1','3.1');
INSERT INTO action_version_map values(1009, '3.1','*');



INSERT INTO images (image_guid, creation_date, size, it_guid, parentid, imagestatus, lastmodified, vm_snapshot_id, volume_type, volume_format, image_group_id, _create_date, _update_date, quota_id, active) VALUES ('00000000-0000-0000-0000-000000000000', '2008-04-01 00:00:00+03', 85899345920, '00000000-0000-0000-0000-000000000000', NULL, 0, NULL, NULL, 2, 4, NULL, '2013-12-25 15:31:57.219114+02', NULL, NULL, true);

INSERT INTO vm_device (device_id, vm_id, type, device, address, boot_order, spec_params, is_managed, is_plugged, is_readonly, _create_date, _update_date, alias) VALUES ('00000006-0006-0006-0006-000000000006', '00000000-0000-0000-0000-000000000000', 'video', 'cirrus', '', NULL, '{ "vram" : "65536" }', true, NULL, false, '2013-12-25 22:54:23.416857+02', NULL, '');

-- Inserting data to history timekeeping
Insert into dwh_history_timekeeping  VALUES('lastSync',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY'));

--OVF
INSERT INTO vm_ovf_generations
      (SELECT vm.vm_guid, sp.id, 1
       FROM vm_static vm ,storage_pool sp, vds_groups vg
       WHERE vg.storage_pool_id = sp.id AND vm.vds_group_id = vg.vds_group_id);


RETURN;
END; $procedure$
LANGUAGE plpgsql;
SELECT insert_data();
drop function insert_data();


/******************************************************************************************************
                                         DWH DATA
******************************************************************************************************/

-- Inserting data to history timekeeping for host sync of slow changing data in dynamic configuration and statistical tables.

Insert into dwh_history_timekeeping  SELECT 'lastFullHostCheck',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY');


