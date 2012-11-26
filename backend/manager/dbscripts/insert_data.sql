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
   v_storage_pool_id := '5849B030-626E-47CB-AD90-3CE782D831B3';
   v_cluster_id := '99408929-82CF-4DC7-A532-9D998063FA95';
   v_id_0009 := '00000000-0000-0000-0000-000000000009';


--INSERTING DATA INTO TABLE storage_pool

INSERT INTO storage_pool (id,name,description,storage_pool_type,status,master_domain_version,compatibility_version) select v_storage_pool_id,'Default','The default Data Center',1,0,0,'3.1' where not exists (select * from storage_pool);
--INSERTING DATA INTO TABLE vds_groups

INSERT INTO vds_groups (vds_group_id,name,description,storage_pool_id,compatibility_version,transparent_hugepages) select v_cluster_id,'Default','The default server cluster',v_storage_pool_id,'3.1',true where not exists (select * from vds_groups);

--INSERTING DATA INTO NETWORK table

insert into network (id,name,description,storage_pool_id) select v_id_0009, 'engine', 'Management Network', v_storage_pool_id where not exists (select * from network);
insert into network_cluster (network_id, cluster_id, status) select v_id_0009,v_cluster_id,1 where not exists (select * from network_cluster);
insert into image_templates (it_guid,os,os_version,creation_date,size,description,bootable) select v_id_0000,'-','-','2008/04/01 00:00:00',85899345920,'Blanc Image Template',false where not exists (select it_guid from image_templates where it_guid = v_id_0000);
insert into vm_templates (vmt_guid,name,mem_size_mb,os,creation_date,child_count,num_of_sockets,cpu_per_socket,description,vds_group_id,domain,num_of_monitors,status) select v_id_0000,'Blank' ,512,0,'2008/04/01 00:00:00',0,1,1,'Blank template',v_cluster_id,'',1,0 where not exists (select vmt_guid from vm_templates where vmt_guid = v_id_0000);
insert into vm_template_image_map (it_guid,vmt_guid,internal_drive_mapping) select v_id_0000,v_id_0000,'1' where not exists (select * from vm_template_image_map where it_guid = v_id_0000  and vmt_guid = v_id_0000);

delete from event_map;


insert into event_map(event_up_name, event_down_name) values('VDC_STOP', 'VDC_START');

insert into event_map(event_up_name, event_down_name) values('IRS_FAILURE', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('IRS_DISK_SPACE_LOW', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('IRS_DISK_SPACE_LOW_ERROR', 'UNASSIGNED');

insert into event_map(event_up_name, event_down_name) values('VDS_FAILURE', 'VDS_ACTIVATE');
insert into event_map(event_up_name, event_down_name) values('USER_VDS_MAINTENANCE', 'VDS_ACTIVATE');
insert into event_map(event_up_name, event_down_name) values('USER_VDS_MAINTENANCE_MIGRATION_FAILED', 'USER_VDS_MAINTENANCE');
insert into event_map(event_up_name, event_down_name) values('VDS_ACTIVATE_FAILED', 'VDS_ACTIVATE');
insert into event_map(event_up_name, event_down_name) values('VDS_RECOVER_FAILED', 'VDS_RECOVER');
insert into event_map(event_up_name, event_down_name) values('VDS_SLOW_STORAGE_RESPONSE_TIME', 'VDS_ACTIVATE');
insert into event_map(event_up_name, event_down_name) values('VDS_APPROVE_FAILED', 'VDS_APPROVE');
insert into event_map(event_up_name, event_down_name) values('VDS_INSTALL_FAILED', 'VDS_INSTALL');

insert into event_map(event_up_name, event_down_name) values('VM_FAILURE', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('VM_MIGRATION_START', 'VM_MIGRATION_DONE');
insert into event_map(event_up_name, event_down_name) values('VM_MIGRATION_FAILED', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('VM_MIGRATION_FAILED_FROM_TO', 'UNASSIGNED');
insert into event_map(event_up_name, event_down_name) values('VM_NOT_RESPONDING', 'UNASSIGNED');

-- Insert notification methods for notification service
insert into event_notification_methods (method_id, method_type) values (0,'Email');

delete from action_version_map;
-- Inserting data to history timekeeping
Insert into dwh_history_timekeeping  VALUES('lastSync',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY'));
RETURN;
END; $procedure$
LANGUAGE plpgsql;
select insert_data();
drop function insert_data();

