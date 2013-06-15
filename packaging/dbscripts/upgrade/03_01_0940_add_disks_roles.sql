Create or replace FUNCTION __temp_insert_predefined_roles()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_SUPER_USER_ID_0001 UUID;
   v_POWER_USER_ID_0002 UUID;
   v_CLUSTER_ADMIN_ID UUID;
   v_DATA_CENTER_ADMIN_ID UUID;
   v_STORAGE_ADMIN_ID UUID;
   v_VM_ADMIN_ID UUID;
   v_DISK_OPERATOR_USER_ID UUID;
   v_DISK_CREATOR_USER_ID UUID;
BEGIN
   v_SUPER_USER_ID_0001 := '00000000-0000-0000-0000-000000000001';
   v_POWER_USER_ID_0002 := '00000000-0000-0000-0001-000000000002';
   v_CLUSTER_ADMIN_ID := 'DEF00001-0000-0000-0000-DEF000000001';
   v_DATA_CENTER_ADMIN_ID := 'DEF00002-0000-0000-0000-DEF000000002';
   v_STORAGE_ADMIN_ID := 'DEF00003-0000-0000-0000-DEF000000003';
   v_VM_ADMIN_ID := 'DEF00006-0000-0000-0000-DEF000000006';
   v_DISK_OPERATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000B';
   v_DISK_CREATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000C';

-----------------
---SuperUser role
-----------------

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID_0001, 1100);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID_0001, 1101);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID_0001, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID_0001, 1103);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_SUPER_USER_ID_0001, 1104);


----------------
--PowerUser role
----------------

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID_0002, 1100);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID_0002, 1101);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID_0002, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID_0002, 1103);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_POWER_USER_ID_0002, 1104);


--------------------
--CLUSTER_ADMIN role
--------------------

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID, 1100);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID, 1101);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID, 1103);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_CLUSTER_ADMIN_ID, 1104);


------------------------
--DATA_CENTER_ADMIN role
------------------------

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID, 1100);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID, 1101);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID, 1103);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DATA_CENTER_ADMIN_ID, 1104);


--------------------
--STORAGE_ADMIN role
--------------------

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID, 1100);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID, 1101);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID, 1103);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_STORAGE_ADMIN_ID, 1104);


---------------
--VM_ADMIN role
---------------

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID, 1100);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID, 1101);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID, 1103);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_ADMIN_ID, 1104);


--------------------------
-- DISK_OPERATOR_USER role
--------------------------
DELETE FROM roles_groups WHERE role_id = v_DISK_OPERATOR_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) SELECT v_DISK_OPERATOR_USER_ID, 'DiskOperator', 'Disk Operator', true, 2
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_DISK_OPERATOR_USER_ID
                  AND name='DiskOperator'
                  AND description='Disk Operator'
                  AND is_readonly=true
                  AND role_type=2);

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID, 1100);
-- ATTACH_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID, 1101);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID, 1103);
-- DELETE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_OPERATOR_USER_ID, 1104);

-------------------------
-- DISK_CREATOR_USER role
-------------------------
DELETE FROM roles_groups WHERE role_id = v_DISK_CREATOR_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) SELECT v_DISK_CREATOR_USER_ID, 'DiskCreator', 'Disk Creator', true, 2
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_DISK_CREATOR_USER_ID
                  AND name='DiskCreator'
                  AND description='Disk Creator'
                  AND is_readonly=true
                  AND role_type=2);

-- CREATE_DISK
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_CREATOR_USER_ID, 1100);
-- EDIT_DISK_PROPERTIES
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_CREATOR_USER_ID, 1102);
-- CONFIGURE_DISK_STORAGE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_DISK_CREATOR_USER_ID, 1103);

 RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_predefined_roles();
DROP function __temp_insert_predefined_roles();




