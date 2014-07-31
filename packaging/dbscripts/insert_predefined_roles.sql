Create or replace FUNCTION insert_predefined_roles()
RETURNS VOID
   AS $procedure$
   DECLARE
   --Roles
   v_SUPER_USER_ID UUID;
   v_POWER_USER_ID UUID;
   v_USER_ID UUID;
   v_CLUSTER_ADMIN_ID UUID;
   v_DATA_CENTER_ADMIN_ID UUID;
   v_STORAGE_ADMIN_ID UUID;
   v_HOST_ADMIN_ID UUID;
   v_NETWORK_ADMIN_ID UUID;
   v_VM_ADMIN_ID UUID;
   v_VM_POOL_ADMIN_ID UUID;
   v_TEMPLATE_ADMIN_ID UUID;
   v_TEMPLATE_USER_ID UUID;
   v_QUOTA_CONSUMER_USER_ID UUID;
   v_DISK_OPERATOR_USER_ID UUID;
   v_DISK_CREATOR_USER_ID UUID;
   v_GLUSTER_ADMIN_ROLE_ID UUID;
   v_VM_CREATOR_USER_ID UUID;
   v_TEMPLATE_CREATOR_USER_ID UUID;
   v_TEMPLATE_OWNER_USER_ID UUID;
   v_LOCAL_ADMIN_ID UUID;
   v_NETWORK_USER_ID UUID;
   v_EXTERNAL_EVENT_CREATOR_ID UUID;
   --Permissions
   v_CREATE_VM INTEGER;
   v_DELETE_VM INTEGER;
   v_EDIT_VM_PROPERTIES INTEGER;
   v_VM_BASIC_OPERATIONS INTEGER;
   v_CHANGE_VM_CD INTEGER;
   v_MIGRATE_VM INTEGER;
   v_CONNECT_TO_VM INTEGER;
   v_IMPORT_EXPORT_VM INTEGER;
   v_CONFIGURE_VM_NETWORK INTEGER;
   v_CONFIGURE_VM_STORAGE INTEGER;
   v_MOVE_VM INTEGER;
   v_MANIPULATE_VM_SNAPSHOTS INTEGER;
   v_FORCE_CONNECT_VM INTEGER;
   v_CUSTOM_PROPERTIES INTEGER;
   v_CREATE_HOST INTEGER;
   v_EDIT_HOST_CONFIGURATION INTEGER;
   v_DELETE_HOST INTEGER;
   v_MANIPULATE_HOST INTEGER;
   v_CONFIGURE_HOST_NETWORK INTEGER;
   v_CREATE_TEMPLATE INTEGER;
   v_EDIT_TEMPLATE_PROPERTIES INTEGER;
   v_DELETE_TEMPLATE INTEGER;
   v_COPY_TEMPLATE INTEGER;
   v_CONFIGURE_TEMPLATE_NETWORK INTEGER;
   v_CREATE_VM_POOL INTEGER;
   v_EDIT_VM_POOL_CONFIGURATION INTEGER;
   v_DELETE_VM_POOL INTEGER;
   v_VM_POOL_BASIC_OPERATIONS INTEGER;
   v_CREATE_CLUSTER INTEGER;
   v_EDIT_CLUSTER_CONFIGURATION INTEGER;
   v_DELETE_CLUSTER INTEGER;
   v_ASSIGN_CLUSTER_NETWORK INTEGER;
   v_CONFIGURE_CLUSTER_NETWORK INTEGER;
   v_MANIPULATE_USERS INTEGER;
   v_MANIPULATE_ROLES INTEGER;
   v_MANIPULATE_PERMISSIONS INTEGER;
   v_EDIT_STORAGE_DOMAIN_CONFIGURATION INTEGER;
   v_DELETE_STORAGE_DOMAIN INTEGER;
   v_MANIPULATE_STORAGE_DOMAIN INTEGER;
   v_CREATE_STORAGE_POOL INTEGER;
   v_DELETE_STORAGE_POOL INTEGER;
   v_EDIT_STORAGE_POOL_CONFIGURATION INTEGER;
   v_CONFIGURE_STORAGE_POOL_NETWORK INTEGER;
   v_CREATE_STORAGE_POOL_NETWORK INTEGER;
   v_DELETE_STORAGE_POOL_NETWORK INTEGER;
   v_CONFIGURE_ENGINE INTEGER;
   v_MANIPULATE_QUOTA INTEGER;
   v_CONSUME_QUOTA INTEGER;
   v_CREATE_GLUSTER_VOLUME INTEGER;
   v_MANIPULATE_GLUSTER_VOLUME INTEGER;
   v_DELETE_GLUSTER_VOLUME INTEGER;
   v_CREATE_STORAGE_DOMAIN INTEGER;
   v_LIVE_MIGRATE_DISK INTEGER;
   v_LIVE_MIGRATE_DISKS INTEGER;
   v_CREATE_DISK INTEGER;
   v_ATTACH_DISK INTEGER;
   v_EDIT_DISK_PROPERTIES INTEGER;
   v_CONFIGURE_DISK_STORAGE INTEGER;
   v_DELETE_DISK INTEGER;
   v_CONFIGURE_STORAGE_POOL_VM_INTERFACE INTEGER;
   v_LOGIN INTEGER;
   v_EXTERNAL_EVENT_INJECTION INTEGER;
   -- Action Types
   v_CREATE_GLUSTER_VOLUME_BRICKS INTEGER;
   v_SET_GLUSTER_VOLUME INTEGER;
   v_START_GLUSTER_VOLUME INTEGER;
   v_STOP_GLUSTER_VOLUME INTEGER;
   v_RESET_GLUSTER_VOLUME INTEGER;
   v_DELETE_GLUSTER_VOLUME_BRICKS INTEGER;
   v_REMOVE_GLUTER_VOLUME_BRICKS INTEGER;
   v_REBALANCE_GLUSTER_VOLUME INTEGER;
   v_REPLACE_GLUSTER_VOLUME_BRICKS INTEGER;
   v_ADD_GLUSTER_VOLUME_BRICKS INTEGER;
   v_START_GLUSTER_VOLUME_PROFILE INTEGER;
   v_STOP_LUSTER_VOLUME_PROFILE INTEGER;
   v_REMOVE_GLUSTER_SERVER INTEGER;

   --Other
   v_ADMIN_USER_ID UUID;

BEGIN
   v_SUPER_USER_ID := '00000000-0000-0000-0000-000000000001';
   v_POWER_USER_ID := '00000000-0000-0000-0001-000000000002';
   v_USER_ID := '00000000-0000-0000-0001-000000000001';
   v_CLUSTER_ADMIN_ID := 'DEF00001-0000-0000-0000-DEF000000001';
   v_DATA_CENTER_ADMIN_ID := 'DEF00002-0000-0000-0000-DEF000000002';
   v_STORAGE_ADMIN_ID := 'DEF00003-0000-0000-0000-DEF000000003';
   v_HOST_ADMIN_ID := 'DEF00004-0000-0000-0000-DEF000000004';
   v_NETWORK_ADMIN_ID := 'DEF00005-0000-0000-0000-DEF000000005';
   v_VM_ADMIN_ID := 'DEF00006-0000-0000-0000-DEF000000006';
   v_VM_POOL_ADMIN_ID := 'DEF00007-0000-0000-0000-DEF000000007';
   v_TEMPLATE_ADMIN_ID := 'DEF00008-0000-0000-0000-DEF000000008';
   v_TEMPLATE_USER_ID := 'DEF00009-0000-0000-0000-DEF000000009';
   v_QUOTA_CONSUMER_USER_ID := 'DEF0000a-0000-0000-0000-DEF00000000a';
   v_DISK_OPERATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000B';
   v_DISK_CREATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000C';
   v_GLUSTER_ADMIN_ROLE_ID := 'DEF0000B-0000-0000-0000-DEF00000000B';
   v_VM_CREATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000D';
   v_TEMPLATE_CREATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000E';
   v_TEMPLATE_OWNER_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000F';
   v_LOCAL_ADMIN_ID := 'FDFC627C-D875-11E0-90F0-83DF133B58CC';
   v_NETWORK_USER_ID := 'DEF0000A-0000-0000-0000-DEF000000010';
   v_EXTERNAL_EVENT_CREATOR_ID := 'DEF0000C-0000-0000-0000-DEF000000000';
   v_CREATE_VM := 1;
   v_DELETE_VM := 2;
   v_EDIT_VM_PROPERTIES := 3;
   v_VM_BASIC_OPERATIONS := 4;
   v_CHANGE_VM_CD := 5;
   v_MIGRATE_VM := 6;
   v_CONNECT_TO_VM := 7;
   v_IMPORT_EXPORT_VM := 8;
   v_CONFIGURE_VM_NETWORK := 9;
   v_CONFIGURE_VM_STORAGE := 10;
   v_MOVE_VM := 11;
   v_MANIPULATE_VM_SNAPSHOTS := 12;
   v_FORCE_CONNECT_VM := 13;
   v_CUSTOM_PROPERTIES := 14;
   v_CREATE_HOST := 100;
   v_EDIT_HOST_CONFIGURATION := 101;
   v_DELETE_HOST := 102;
   v_MANIPULATE_HOST := 103;
   v_CONFIGURE_HOST_NETWORK := 104;
   v_CREATE_TEMPLATE := 200;
   v_EDIT_TEMPLATE_PROPERTIES := 201;
   v_DELETE_TEMPLATE := 202;
   v_COPY_TEMPLATE := 203;
   v_CONFIGURE_TEMPLATE_NETWORK := 204;
   v_CREATE_VM_POOL := 300;
   v_EDIT_VM_POOL_CONFIGURATION := 301;
   v_DELETE_VM_POOL := 302;
   v_VM_POOL_BASIC_OPERATIONS := 303;
   v_CREATE_CLUSTER := 400;
   v_EDIT_CLUSTER_CONFIGURATION := 401;
   v_DELETE_CLUSTER := 402;
   v_CONFIGURE_CLUSTER_NETWORK := 403;
   v_ASSIGN_CLUSTER_NETWORK := 404;
   v_MANIPULATE_USERS := 500;
   v_MANIPULATE_ROLES := 501;
   v_MANIPULATE_PERMISSIONS := 502;
   v_CREATE_STORAGE_DOMAIN := 600;
   v_EDIT_STORAGE_DOMAIN_CONFIGURATION := 601;
   v_DELETE_STORAGE_DOMAIN := 602;
   v_MANIPULATE_STORAGE_DOMAIN := 603;
   v_CREATE_STORAGE_POOL := 700;
   v_DELETE_STORAGE_POOL := 701;
   v_EDIT_STORAGE_POOL_CONFIGURATION := 702;
   v_CONFIGURE_STORAGE_POOL_NETWORK := 703;
   v_CREATE_STORAGE_POOL_NETWORK := 704;
   v_DELETE_STORAGE_POOL_NETWORK := 705;
   v_CONFIGURE_ENGINE := 800;
   v_MANIPULATE_QUOTA := 900;
   v_CONSUME_QUOTA := 901;
   v_CREATE_GLUSTER_VOLUME := 1000;
   v_MANIPULATE_GLUSTER_VOLUME := 1001;
   v_DELETE_GLUSTER_VOLUME := 1002;
   v_LIVE_MIGRATE_DISK := 1010;
   v_LIVE_MIGRATE_DISKS := 1011;
   v_CREATE_DISK := 1100;
   v_ATTACH_DISK := 1101;
   v_EDIT_DISK_PROPERTIES := 1102;
   v_CONFIGURE_DISK_STORAGE := 1103;
   v_DELETE_DISK := 1104;
   v_CONFIGURE_STORAGE_POOL_VM_INTERFACE := 1200;
   v_LOGIN := 1300;
   v_EXTERNAL_EVENT_INJECTION := 1500;
   -- Action Types
   v_CREATE_GLUSTER_VOLUME_BRICKS := 1400;
   v_SET_GLUSTER_VOLUME := 1401;
   v_START_GLUSTER_VOLUME := 1402;
   v_STOP_GLUSTER_VOLUME := 1403;
   v_RESET_GLUSTER_VOLUME := 1404;
   v_DELETE_GLUSTER_VOLUME_BRICKS := 1405;
   v_REMOVE_GLUTER_VOLUME_BRICKS := 1406;
   v_REBALANCE_GLUSTER_VOLUME := 1407;
   v_REPLACE_GLUSTER_VOLUME_BRICKS := 1408;
   v_ADD_GLUSTER_VOLUME_BRICKS := 1409;
   v_START_GLUSTER_VOLUME_PROFILE := 1410;
   v_STOP_LUSTER_VOLUME_PROFILE := 1411;
   v_REMOVE_GLUSTER_SERVER := 1412;



INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT  v_SUPER_USER_ID,'SuperUser','Roles management administrator',true,1,true;

---Vm Groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_VM_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_VM_BASIC_OPERATIONS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CHANGE_VM_CD);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MIGRATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONNECT_TO_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_IMPORT_EXPORT_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_VM_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_VM_STORAGE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MOVE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MANIPULATE_VM_SNAPSHOTS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_HOST_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MANIPULATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_HOST_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_TEMPLATE_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_COPY_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_TEMPLATE_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_VM_POOL_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_VM_POOL_BASIC_OPERATIONS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_CLUSTER);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_CLUSTER_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_CLUSTER);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_CLUSTER_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MANIPULATE_USERS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MANIPULATE_ROLES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MANIPULATE_PERMISSIONS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_STORAGE_DOMAIN);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_STORAGE_DOMAIN_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_STORAGE_DOMAIN);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_MANIPULATE_STORAGE_DOMAIN);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_STORAGE_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_STORAGE_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_STORAGE_POOL_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_STORAGE_POOL_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_STORAGE_POOL_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_STORAGE_POOL_NETWORK);
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_USER_ID,'UserRole','Standard User Role',true,2,true;
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_ENGINE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_USER_ID,v_VM_BASIC_OPERATIONS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_USER_ID,v_CHANGE_VM_CD);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_USER_ID,v_CONNECT_TO_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_USER_ID,v_VM_POOL_BASIC_OPERATIONS);

--PoewerUser role
---------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_POWER_USER_ID,'PowerUserRole','User Role, allowed to create VMs, Templates and Disks',true,2,false;


---Vm Groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID,v_CREATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID,v_CREATE_TEMPLATE);

-------------
--CLUSTER_ADMIN role
---------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_CLUSTER_ADMIN_ID,'ClusterAdmin','Administrator Role, permission for all the objects underneath a specific Cluster',true,1,true;


---Vm Groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CREATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_DELETE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_EDIT_VM_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_VM_BASIC_OPERATIONS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CHANGE_VM_CD);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_MIGRATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CONNECT_TO_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_IMPORT_EXPORT_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CONFIGURE_VM_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CONFIGURE_VM_STORAGE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_MOVE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_MANIPULATE_VM_SNAPSHOTS);
-- vm pools actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CREATE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_EDIT_VM_POOL_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_DELETE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_VM_POOL_BASIC_OPERATIONS);
-- host (vds) actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CREATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_EDIT_HOST_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_DELETE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_MANIPULATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CONFIGURE_HOST_NETWORK);
-- clusters actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CREATE_CLUSTER);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_EDIT_CLUSTER_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_DELETE_CLUSTER);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CONFIGURE_CLUSTER_NETWORK);

-------------
--DATA_CENTER_ADMIN role
---------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_DATA_CENTER_ADMIN_ID,'DataCenterAdmin','Administrator Role, permission for all the objects underneath a specific Data Center, except Storage',true,1,true;

---Vm Groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_EDIT_VM_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_VM_BASIC_OPERATIONS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CHANGE_VM_CD);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_MIGRATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONNECT_TO_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_IMPORT_EXPORT_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONFIGURE_VM_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONFIGURE_VM_STORAGE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_MOVE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_MANIPULATE_VM_SNAPSHOTS);
-- templates actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_EDIT_TEMPLATE_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_COPY_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONFIGURE_TEMPLATE_NETWORK);
-- vm pools actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_EDIT_VM_POOL_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_VM_POOL_BASIC_OPERATIONS);
-- host (vds) actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_EDIT_HOST_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_MANIPULATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONFIGURE_HOST_NETWORK);
-- clusters actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_CLUSTER);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_EDIT_CLUSTER_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_CLUSTER);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONFIGURE_CLUSTER_NETWORK);
-- storage pool actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_STORAGE_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_STORAGE_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_EDIT_STORAGE_POOL_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONFIGURE_STORAGE_POOL_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_STORAGE_POOL_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_STORAGE_POOL_NETWORK);

-------------
--STORAGE_ADMIN role
---------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_STORAGE_ADMIN_ID,'StorageAdmin','Administrator Role, permission for all operations on a specific Storage Domain',true,1,true;

-- storage domains actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_CREATE_STORAGE_DOMAIN);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_EDIT_STORAGE_DOMAIN_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_DELETE_STORAGE_DOMAIN);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_MANIPULATE_STORAGE_DOMAIN);

-------------
--HOST_ADMIN role
---------------

INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_HOST_ADMIN_ID,'HostAdmin','Administrator Role, permission for all operations on a specific Host',true,1,true;

-- host (vds) actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_CREATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_EDIT_HOST_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_DELETE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_MANIPULATE_HOST);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_CONFIGURE_HOST_NETWORK);
-- storage domains actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_CREATE_STORAGE_DOMAIN);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_EDIT_STORAGE_DOMAIN_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_DELETE_STORAGE_DOMAIN);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,v_MANIPULATE_STORAGE_DOMAIN);


-------------
--NETWORK_ADMIN role
---------------

INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_NETWORK_ADMIN_ID,'NetworkAdmin','Administrator Role, permission for all operations on a specific Logical Network',true,1,true;
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_NETWORK_ADMIN_ID,v_CONFIGURE_HOST_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_NETWORK_ADMIN_ID,v_CONFIGURE_CLUSTER_NETWORK);

-------------
--VM_ADMIN role
---------------

INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_VM_ADMIN_ID,'UserVmManager','User Role, with permission for any operation on Vms',true,2,true;

-- insert local admin user to users table and assign superuser permissions
INSERT INTO users(user_id,name,domain,username,groups,status)
        SELECT v_LOCAL_ADMIN_ID, 'admin', 'internal', 'admin@internal','',1;

INSERT INTO permissions(id,role_id,ad_element_id,object_id,object_type_id)
        SELECT uuid_generate_v1(), v_SUPER_USER_ID, v_LOCAL_ADMIN_ID, getGlobalIds('system'), 1;

---Vm Groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_CREATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_DELETE_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_EDIT_VM_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_VM_BASIC_OPERATIONS);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_CHANGE_VM_CD);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_CONNECT_TO_VM);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_CONFIGURE_VM_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_CONFIGURE_VM_STORAGE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_MANIPULATE_VM_SNAPSHOTS);

-------------
--VM_POOL_ADMIN role
---------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_VM_POOL_ADMIN_ID,'VmPoolAdmin','Administrator Role, permission for all operations on a specific VM Pool',true,1,true;

-- vm pools actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,v_CREATE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,v_EDIT_VM_POOL_CONFIGURATION);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,v_DELETE_VM_POOL);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,v_VM_POOL_BASIC_OPERATIONS);
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_TEMPLATE_ADMIN_ID,'TemplateAdmin','Administrator Role, permission for all operations on a specific Template',true,1,true;
-- templates actions groups
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,v_CREATE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,v_EDIT_TEMPLATE_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,v_DELETE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,v_COPY_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,v_CONFIGURE_TEMPLATE_NETWORK);

-------------
--TEMPLATE_USER role
---------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_TEMPLATE_USER_ID,'TemplateUser','Template User',true,2,false;

INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_QUOTA_CONSUMER_USER_ID, 'QuotaConsumer','User Role, permissions to consume the Quota resources',true,2,false;

-- MAKE BLANK TEMPLATE PUBLIC
INSERT INTO permissions (id,role_id,ad_element_id,object_id,object_type_id)
 SELECT uuid_generate_v1(),
 v_TEMPLATE_USER_ID, -- TemplateUser
 getGlobalIds('everyone'),
 '00000000-0000-0000-0000-000000000000',    -- blank template id --
 4;                                          -- template object type id --

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_USER_ID,v_CREATE_VM);
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_DATA_CENTER_ADMIN_ID, v_MANIPULATE_QUOTA;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_SUPER_USER_ID, v_MANIPULATE_QUOTA;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_DATA_CENTER_ADMIN_ID, v_CONSUME_QUOTA;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_SUPER_USER_ID, v_CONSUME_QUOTA;
INSERT INTO roles_groups (role_id,action_group_id) SELECT v_SUPER_USER_ID, v_FORCE_CONNECT_VM;

-- Disks

-----------------
---SuperUser role
-----------------

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CREATE_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_ATTACH_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_EDIT_DISK_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_CONFIGURE_DISK_STORAGE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID,v_DELETE_DISK);


----------------
--PowerUser role
----------------

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID,v_CREATE_DISK);

--------------------
--CLUSTER_ADMIN role
--------------------

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CREATE_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_ATTACH_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_EDIT_DISK_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_CONFIGURE_DISK_STORAGE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,v_DELETE_DISK);


------------------------
--DATA_CENTER_ADMIN role
------------------------

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CREATE_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_ATTACH_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_EDIT_DISK_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_CONFIGURE_DISK_STORAGE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,v_DELETE_DISK);

--------------------
--STORAGE_ADMIN role
--------------------

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_CREATE_DISK);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_ATTACH_DISK);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_EDIT_DISK_PROPERTIES);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_CONFIGURE_DISK_STORAGE);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,v_DELETE_DISK);


---------------
--VM_ADMIN role
---------------

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_CREATE_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_ATTACH_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_EDIT_DISK_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,v_DELETE_DISK);

--------------------------
-- DISK_OPERATOR_USER role
--------------------------
DELETE FROM roles_groups WHERE role_id = v_DISK_OPERATOR_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_DISK_OPERATOR_USER_ID, 'DiskOperator', 'User Role, permissions for all operations on a specific disk', true, 2, true;

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID,v_CREATE_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID,v_ATTACH_DISK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID,v_EDIT_DISK_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID,v_DELETE_DISK);

-------------------------
-- DISK_CREATOR_USER role
-------------------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_DISK_CREATOR_USER_ID, 'DiskCreator', 'User Role, permission to create Disks', true, 2, false;

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_CREATOR_USER_ID,v_CREATE_DISK);

-- Gluster

INSERT INTO roles_groups(role_id,action_group_id) SELECT v_SUPER_USER_ID, v_CREATE_GLUSTER_VOLUME;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_SUPER_USER_ID, v_MANIPULATE_GLUSTER_VOLUME;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_SUPER_USER_ID, v_DELETE_GLUSTER_VOLUME;

--------------
-- GLUSTER_ADMIN_USER role
--------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_GLUSTER_ADMIN_ROLE_ID, 'GlusterAdmin','Administrator Role, permissions for operations on Gluster objects',true,1,true;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, v_CREATE_GLUSTER_VOLUME;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, v_MANIPULATE_GLUSTER_VOLUME;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, v_DELETE_GLUSTER_VOLUME;


-------------------------
-- VM_CREATOR_USER role
-------------------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_VM_CREATOR_USER_ID, 'VmCreator', 'User Role, permission to create VMs', true, 2, false;
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_CREATOR_USER_ID, v_CREATE_VM);


-----------------------------
-- TEMPALTE_CREATOR_USER role
-----------------------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_TEMPLATE_CREATOR_USER_ID, 'TemplateCreator', 'User Role, permission to create Templates', true, 2, false;

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_CREATOR_USER_ID, v_CREATE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES( v_DATA_CENTER_ADMIN_ID, v_CONFIGURE_STORAGE_POOL_VM_INTERFACE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES( v_SUPER_USER_ID, v_CONFIGURE_STORAGE_POOL_VM_INTERFACE);

-----------------------------
-- TEMPALTE_OWNER_USER role
-----------------------------
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_TEMPLATE_OWNER_USER_ID, 'TemplateOwner', 'User Role, permissions for all operations on Templates', true, 2, true;

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, v_EDIT_TEMPLATE_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, v_DELETE_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, v_COPY_TEMPLATE);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, v_CONFIGURE_TEMPLATE_NETWORK);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, v_LOGIN);

-- Custom properties

INSERT INTO roles_groups (role_id, action_group_id) values(v_SUPER_USER_ID,v_CUSTOM_PROPERTIES);
INSERT INTO roles_groups (role_id, action_group_id) values(v_CLUSTER_ADMIN_ID, v_CUSTOM_PROPERTIES);
INSERT INTO roles_groups (role_id, action_group_id) values(v_DATA_CENTER_ADMIN_ID,v_CUSTOM_PROPERTIES);
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_VM_CREATOR_USER_ID, v_CREATE_DISK;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_CLUSTER_ADMIN_ID, v_MANIPULATE_PERMISSIONS;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_DATA_CENTER_ADMIN_ID, v_MANIPULATE_PERMISSIONS;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_TEMPLATE_OWNER_USER_ID, v_MANIPULATE_PERMISSIONS;
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_DISK_OPERATOR_USER_ID, v_MANIPULATE_PERMISSIONS;
INSERT INTO roles_groups(role_id,action_group_id)SELECT v_VM_ADMIN_ID, v_MANIPULATE_PERMISSIONS;
INSERT INTO roles_groups(role_id,action_group_id)SELECT v_VM_POOL_ADMIN_ID, v_CREATE_VM;

-- Login

INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_DATA_CENTER_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_POWER_USER_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_DISK_CREATOR_USER_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_TEMPLATE_CREATOR_USER_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_TEMPLATE_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_STORAGE_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_VM_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_SUPER_USER_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_VM_CREATOR_USER_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_DISK_OPERATOR_USER_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_CLUSTER_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_USER_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_HOST_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_VM_POOL_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_NETWORK_ADMIN_ID, v_LOGIN);
INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_GLUSTER_ADMIN_ROLE_ID, v_LOGIN);

-- Networking
-- Add ASSIGN_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_SUPER_USER_ID, v_ASSIGN_CLUSTER_NETWORK);

-- Add ASSIGN_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID, v_ASSIGN_CLUSTER_NETWORK);

-- Add ASSIGN_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_ADMIN_ID, v_ASSIGN_CLUSTER_NETWORK);

-- Add PORT_MIRRORING
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_ADMIN_ID, v_CONFIGURE_STORAGE_POOL_VM_INTERFACE);

-- Add CONFIGURE_STORAGE_POOL_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_ADMIN_ID ,v_CONFIGURE_STORAGE_POOL_NETWORK);

-- Add CREATE_STORAGE_POOL_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_ADMIN_ID ,v_CREATE_STORAGE_POOL_NETWORK);

-- Add DELETE_STORAGE_POOL_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_ADMIN_ID ,v_DELETE_STORAGE_POOL_NETWORK);

-- Add CONFIGURE_VM_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_ADMIN_ID ,v_CONFIGURE_VM_NETWORK);

-- Add CONFIGURE_TEMPLATE_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_ADMIN_ID ,v_CONFIGURE_TEMPLATE_NETWORK);

-- ADD NetworkUser Role
INSERT INTO roles(id,name,description,is_readonly,role_type) values(v_NETWORK_USER_ID, 'NetworkUser', 'Network User', true, 2);

-- Add CONFIGURE_VM_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_USER_ID, v_CONFIGURE_VM_NETWORK);

-- Add CONFIGURE_TEMPLATE_NETWORK
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_USER_ID, v_CONFIGURE_TEMPLATE_NETWORK);


-------------------------------------------------------
-- Grant NetworkUser role to 'everyone' on all networks
-------------------------------------------------------

INSERT INTO permissions (id,role_id, ad_element_id, object_id, object_type_id)
    (SELECT uuid_generate_v1(), v_NETWORK_USER_ID, getGlobalIds('everyone'), id, 20 FROM network);

-- grant admin poweruser role on system
v_ADMIN_USER_ID := user_id from users where username = 'admin@internal';
insert into permissions(id,role_id,ad_element_id,object_id,object_type_id)
    values (uuid_generate_v1(), v_POWER_USER_ID, v_ADMIN_USER_ID, getGlobalIds('system'), 1);

-- Add External Event Injection priviledge to super user
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID, v_EXTERNAL_EVENT_INJECTION);

-----------------------------------
-- EXTERNAL_EVENT_CREATOR_USER role
-----------------------------------

INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children)
    values(v_EXTERNAL_EVENT_CREATOR_ID, 'ExternalEventsCreator', 'External Events Creator', true, 2, false);

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_EXTERNAL_EVENT_CREATOR_ID, v_EXTERNAL_EVENT_INJECTION);

-----------------------------------
-- GLUSTER VOLUME
-----------------------------------

-- Create gluster volume
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_CREATE_GLUSTER_VOLUME_BRICKS, '3.1', '*');

-- Set gluster volume option
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_SET_GLUSTER_VOLUME, '3.1', '*');

-- Start gluster volume
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_START_GLUSTER_VOLUME, '3.1', '*');

-- Stop gluster volume
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_STOP_GLUSTER_VOLUME, '3.1', '*');

-- Reset gluster volume options
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_RESET_GLUSTER_VOLUME, '3.1', '*');

-- Delete gluster volume
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_DELETE_GLUSTER_VOLUME_BRICKS, '3.1', '*');

-- Gluster volume remove bricks
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_REMOVE_GLUTER_VOLUME_BRICKS, '3.1', '*');

-- Start gluster volume rebalance
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_REBALANCE_GLUSTER_VOLUME, '3.1', '*');

-- Replace gluster volume bricks
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_REPLACE_GLUSTER_VOLUME_BRICKS, '3.1', '*');

-- Add bricks to Gluster volume
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_ADD_GLUSTER_VOLUME_BRICKS, '3.1', '*');

-- Start Gluster volume profile
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_START_GLUSTER_VOLUME_PROFILE, '3.2', '*');

-- Stop gluster volume profile
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_STOP_LUSTER_VOLUME_PROFILE, '3.2', '*');

-- Remove gluster server
insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    values(v_REMOVE_GLUSTER_SERVER, '3.2', '*');

-- QUOTA
INSERT INTO roles_groups(role_id,action_group_id)
    values(v_QUOTA_CONSUMER_USER_ID, v_CONSUME_QUOTA);

-- Add Login Permission
INSERT INTO roles_groups(role_id, action_group_id) VALUES(v_NETWORK_USER_ID, v_LOGIN);

-- lsm_version_support
insert into action_version_map values (v_LIVE_MIGRATE_DISK, '3.2', '3.2');
insert into action_version_map values (v_LIVE_MIGRATE_DISKS, '3.2', '3.2');


 RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT insert_predefined_roles();
drop function insert_predefined_roles();

