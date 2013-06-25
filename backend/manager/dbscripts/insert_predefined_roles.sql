Create or replace FUNCTION insert_predefined_roles()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_super_user_id_0001 UUID;
   v_power_user_id_0002 UUID;
   v_user_id_1001 UUID;
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
BEGIN
   v_super_user_id_0001 := '00000000-0000-0000-0000-000000000001';
   v_power_user_id_0002 := '00000000-0000-0000-0001-000000000002';
   v_user_id_1001 := '00000000-0000-0000-0001-000000000001';

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


--insert into vdc_options (option_name,option_value,version) select  'DomainName','example.org','general' where not exists (select option_name,version from vdc_options where option_name='DomainName' and version='general');

delete from roles_groups where role_id = v_super_user_id_0001;
INSERT INTO roles(id,name,description,is_readonly,role_type) select  v_super_user_id_0001,'SuperUser','Roles management administrator',true,1 where not exists (select * from roles where id=v_super_user_id_0001 and name='SuperUser' and description='Roles management administrator' and is_readonly=true and role_type=1);

---Vm Groups
--CREATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,1);
--DELETE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,2);
--EDIT_VM_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,3);
--VM_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,4);
--CHANGE_VM_CD
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,5);
--MIGRATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,6);
--CONNECT_TO_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,7);
--IMPORT_EXPORT_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,8);
--CONFIGURE_VM_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,9);
--CONFIGURE_VM_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,10);
--MOVE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,11);
--MANIPULATE_VM_SNAPSHOTS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,12);
-- host (vds) actions groups
--CREATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,100);
--EDIT_HOST_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,101);
--DELETE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,102);
--MANIPUTLATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,103);
--CONFIGURE_HOST_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,104);
-- templates actions groups
--CREATE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,200);
--EDIT_TEMPLATE_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,201);
--DELETE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,202);
--COPY_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,203);
--CONFIGURE_TEMPLATE_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,204);
-- vm pools actions groups
--CREATE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,300);
--EDIT_VM_POOL_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,301);
--DELETE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,302);
--VM_POOL_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,303);
-- clusters actions groups
--CREATE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,400);
--EDIT_CLUSTER_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,401);
--DELETE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,402);
--CONFIGURE_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,403);
-- users and MLA actions groups
--MANIPULATE_USERS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,500);
--MANIPULATE_ROLES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,501);
--MANIPULATE_PERMISSIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,502);
-- storage domains actions groups
--CREATE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,600);
--EDIT_STORAGE_DOMAIN_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,601);
--DELETE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,602);
--MANIPULATE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,603);
-- storage pool actions groups
--CREATE_STORAGE_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,700);
--DELETE_STORAGE_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,701);
--EDIT_STORAGE_POOL_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,702);
--CONFIGURE_STORAGE_POOL_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_super_user_id_0001,703);
delete from roles_groups where role_id = v_user_id_1001;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_user_id_1001,'UserRole','Standard User Role',true,2 where not exists (select id,name,description,is_readonly,role_type from roles where id=v_user_id_1001 and name='UserRole' and description='Standard User Role' and is_readonly=true and role_type=2);

--VM_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_user_id_1001,4);
--CHANGE_VM_CD
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_user_id_1001,5);
--CONNECT_TO_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_user_id_1001,7);
--VM_POOL_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_user_id_1001,303);

--PoewerUser role
---------------
delete from roles_groups where role_id = v_power_user_id_0002;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_power_user_id_0002,'PowerUserRole','User Role, allowed to create/manage Vms and Templates',true,2 where not exists (select id,name,description,is_readonly,role_type from roles where id=v_power_user_id_0002 and name='PowerUserRole' and description='User Role, allowed to create/manage Vms and Templates' and is_readonly=true and role_type=2);


---Vm Groups
--CREATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,1);
--DELETE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,2);
--EDIT_VM_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,3);
--VM_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,4);
--CHANGE_VM_CD
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,5);
--MIGRATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,6);
--CONNECT_TO_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,7);
--IMPORT_EXPORT_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,8);
--CONFIGURE_VM_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,9);
--CONFIGURE_VM_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,10);
--MOVE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,11);
--MANIPULATE_VM_SNAPSHOTS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,12);
-- templates actions groups
--CREATE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,200);
--EDIT_TEMPLATE_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,201);
--DELETE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,202);
--COPY_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,203);
--CONFIGURE_TEMPLATE_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,204);
-- vm pools actions groups
--CREATE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,300);
--EDIT_VM_POOL_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,301);
--DELETE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,302);
--VM_POOL_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_power_user_id_0002,303);

-------------
--CLUSTER_ADMIN role
---------------
delete from roles_groups where role_id = v_CLUSTER_ADMIN_ID;INSERT INTO roles(id,name,description,is_readonly,role_type) select v_CLUSTER_ADMIN_ID,'ClusterAdmin','Administrator Role, permission for all the objects underneath a specific Cluster',true,1 where not exists (select id,name,description,is_readonly,role_type from roles where id=v_CLUSTER_ADMIN_ID and name='ClusterAdmin' and description='Administrator Role, permission for all the objects underneath a specific Cluster' and is_readonly=true and role_type=1);


---Vm Groups
--CREATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,1);
--DELETE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,2);
--EDIT_VM_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,3);
--VM_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,4);
--CHANGE_VM_CD
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,5);
--MIGRATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,6);
--CONNECT_TO_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,7);
--IMPORT_EXPORT_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,8);
--CONFIGURE_VM_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,9);
--CONFIGURE_VM_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,10);
--MOVE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,11);
--MANIPULATE_VM_SNAPSHOTS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,12);
-- vm pools actions groups
--CREATE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,300);
--EDIT_VM_POOL_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,301);
--DELETE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,302);
--VM_POOL_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,303);
-- host (vds) actions groups
--CREATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,100);
--EDIT_HOST_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,101);
--DELETE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,102);
--MANIPUTLATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,103);
--CONFIGURE_HOST_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,104);
-- clusters actions groups
--CREATE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,400);
--EDIT_CLUSTER_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,401);
--DELETE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,402);
--CONFIGURE_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID,403);

-------------
--DATA_CENTER_ADMIN role
---------------
delete from roles_groups where role_id = v_DATA_CENTER_ADMIN_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_DATA_CENTER_ADMIN_ID,'DataCenterAdmin','Administrator Role, permission for all the objects underneath a specific Data Center, except Storage',true,1 where not exists (select id,name,description,is_readonly,role_type from roles where id=v_DATA_CENTER_ADMIN_ID and name='DataCenterAdmin' and description='Administrator Role, permission for all the objects underneath a specific Data Center, except Storage' and is_readonly=true and role_type=1);

---Vm Groups
--CREATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,1);
--DELETE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,2);
--EDIT_VM_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,3);
--VM_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,4);
--CHANGE_VM_CD
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,5);
--MIGRATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,6);
--CONNECT_TO_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,7);
--IMPORT_EXPORT_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,8);
--CONFIGURE_VM_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,9);
--CONFIGURE_VM_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,10);
--MOVE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,11);
--MANIPULATE_VM_SNAPSHOTS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,12);
-- templates actions groups
--CREATE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,200);
--EDIT_TEMPLATE_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,201);
--DELETE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,202);
--COPY_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,203);
--CONFIGURE_TEMPLATE_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,204);
-- vm pools actions groups
--CREATE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,300);
--EDIT_VM_POOL_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,301);
--DELETE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,302);
--VM_POOL_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,303);
-- host (vds) actions groups
--CREATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,100);
--EDIT_HOST_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,101);
--DELETE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,102);
--MANIPUTLATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,103);
--CONFIGURE_HOST_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,104);
-- clusters actions groups
--CREATE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,400);
--EDIT_CLUSTER_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,401);
--DELETE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,402);
--CONFIGURE_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,403);
-- storage pool actions groups
--CREATE_STORAGE_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,700);
--DELETE_STORAGE_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,701);
--EDIT_STORAGE_POOL_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,702);
--CONFIGURE_STORAGE_POOL_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID,703);

-------------
--STORAGE_ADMIN role
---------------
delete from roles_groups where role_id = v_STORAGE_ADMIN_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_STORAGE_ADMIN_ID,'StorageAdmin','Administrator Role, permission for all operations on a specific Storage Domain',true,1 where
not exists (select id,name,description,is_readonly,role_type from roles where id=v_STORAGE_ADMIN_ID and name='StorageAdmin' and description='Administrator Role, permission for all operations on a specific Storage Domain' and is_readonly=true and role_type=1);

-- storage domains actions groups
--CREATE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,600);
--EDIT_STORAGE_DOMAIN_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,601);
--DELETE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,602);
--MANIPULATE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID,603);

-------------
--HOST_ADMIN role
---------------

delete from roles_groups where role_id = v_HOST_ADMIN_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_HOST_ADMIN_ID,'HostAdmin','Administrator Role, permission for all operations on a specific Host',true,1 where
not exists (select id,name,description,is_readonly,role_type from roles where id=v_HOST_ADMIN_ID and name='HostAdmin' and description='Administrator Role, permission for all operations on a specific Host' and is_readonly=true and role_type=1);

-- host (vds) actions groups
--CREATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,100);
--EDIT_HOST_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,101);
--DELETE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,102);
--MANIPUTLATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,103);
--CONFIGURE_HOST_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,104);
-- storage domains actions groups
--CREATE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,600);
--EDIT_STORAGE_DOMAIN_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,601);
--DELETE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,602);
--MANIPULATE_STORAGE_DOMAIN
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_HOST_ADMIN_ID,603);


-------------
--NETWORK_ADMIN role
---------------

delete from roles_groups where role_id = v_NETWORK_ADMIN_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_NETWORK_ADMIN_ID,'NetworkAdmin','Administrator Role, permission for all operations on a specific Logical Network',true,1 where
not exists (select id,name,description,is_readonly,role_type from roles where id=v_NETWORK_ADMIN_ID and name='NetworkAdmin' and description='Administrator Role, permission for all operations on a specific Logical Network' and is_readonly=true and role_type=1);
--CONFIGURE_HOST_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_NETWORK_ADMIN_ID,104);
--MANIPUTLATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_NETWORK_ADMIN_ID,103);
--CONFIGURE_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_NETWORK_ADMIN_ID,403);

-------------
--VM_ADMIN role
---------------

delete from roles_groups where role_id = v_VM_ADMIN_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_VM_ADMIN_ID,'UserVmManager','User Role, with permission for any operation on Vms',true,2 where
not exists (select id,name,description,is_readonly,role_type from roles where id=v_VM_ADMIN_ID and name='UserVmManager' and description='User Role, with permission for any operation on Vms' and is_readonly=true and role_type=2);

-- insert local admin user to users table and assign superuser permissions
insert into users(user_id,name,domain,username,groups,status)
        select 'fdfc627c-d875-11e0-90f0-83df133b58cc', 'admin', 'internal', 'admin@internal','',1
        where not exists (select 1 from users where user_id = 'fdfc627c-d875-11e0-90f0-83df133b58cc');

insert into permissions(id,role_id,ad_element_id,object_id,object_type_id)
        select uuid_generate_v1(), '00000000-0000-0000-0000-000000000001', 'fdfc627c-d875-11e0-90f0-83df133b58cc', getGlobalIds('system'), 1
        where not exists
                (select 1 from permissions
                 where role_id = '00000000-0000-0000-0000-000000000001' and
                       ad_element_id = 'fdfc627c-d875-11e0-90f0-83df133b58cc' and
                       object_id= getGlobalIds('system') and
                       object_type_id = 1);

---Vm Groups
--CREATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,1);
--DELETE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,2);
--EDIT_VM_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,3);
--VM_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,4);
--CHANGE_VM_CD
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,5);
--MIGRATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,6);
--CONNECT_TO_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,7);
--IMPORT_EXPORT_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,8);
--CONFIGURE_VM_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,9);
--CONFIGURE_VM_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,10);
--MOVE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,11);
--MANIPULATE_VM_SNAPSHOTS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID,12);

-------------
--VM_POOL_ADMIN role
---------------
delete from roles_groups where role_id = v_VM_POOL_ADMIN_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_VM_POOL_ADMIN_ID,'VmPoolAdmin','Administrator Role, permission for all operations on a specific VM Pool',true,1 where
not exists (select id,name,description,is_readonly,role_type from roles where id= v_VM_POOL_ADMIN_ID and name='VmPoolAdmin' and description='Administrator Role, permission for all operations on a specific VM Pool' and is_readonly=true and role_type=1);

-- vm pools actions groups
--CREATE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,300);
--EDIT_VM_POOL_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,301);
--DELETE_VM_POOL
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,302);
--VM_POOL_BASIC_OPERATIONS
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_POOL_ADMIN_ID,303);
--TEMPLATE_ADMIN role
---------------
delete from roles_groups where role_id = v_TEMPLATE_ADMIN_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_TEMPLATE_ADMIN_ID,'TemplateAdmin','Administrator Role, permission for all operations on a specific Template',true,1 where
not exists (select id,name,description,is_readonly,role_type from roles where id= v_TEMPLATE_ADMIN_ID and name='TemplateAdmin' and description='Administrator Role, permission for all operations on a specific Template' and is_readonly=true and role_type=1);
-- templates actions groups
--CREATE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,200);
--EDIT_TEMPLATE_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,201);
--DELETE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,202);
--COPY_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,203);
--CONFIGURE_TEMPLATE_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_ADMIN_ID,204);

-------------
--TEMPLATE_USER role
---------------
delete from roles_groups where role_id = v_TEMPLATE_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) select v_TEMPLATE_USER_ID,'TemplateUser','Template User',true,2 where
not exists (select id,name,description,is_readonly,role_type from roles where id= v_TEMPLATE_USER_ID and name='TemplateUser' and description='Template User' and is_readonly=true and role_type=2);

-- MAKE BLANK TEMPLATE PUBLIC
insert into permissions (id,role_id,ad_element_id,object_id,object_type_id)
 select uuid_generate_v1(),
'DEF00009-0000-0000-0000-DEF000000009', -- TemplateUser
 getGlobalIds('everyone'),
 '00000000-0000-0000-0000-000000000000',    -- blank template id --
 4                                          -- template object type id --
 where not exists (
  select * from permissions where
  role_id = 'DEF00009-0000-0000-0000-DEF000000009'
  and
  ad_element_id = getGlobalIds('everyone')
  and
  object_id = '00000000-0000-0000-0000-000000000000'
  and
  object_type_id = 4);

---Vm Groups
--CREATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_USER_ID,1);

 RETURN;
END; $procedure$
LANGUAGE plpgsql;

select insert_predefined_roles();
drop function insert_predefined_roles();

