-- add new roles for instance types:
-- INSTANCE_CREATOR - "DEF00011-0000-0000-0000-DEF000000011" - allow to create instance, similar to VM_CREATOR
-- USER_INSTANCE_MANAGER - "DEF00012-0000-0000-0000-DEF000000012" - allow to manipulate instance, similar to VM_OPERATOR (UserVmManager)

Create or replace FUNCTION __temp_insert_predefined_roles_03_03_instance_types()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_INSTANCE_CREATOR_ID UUID;
   v_USER_INSTANCE_MANAGER_ID UUID;

BEGIN
   v_INSTANCE_CREATOR_ID := 'DEF00011-0000-0000-0000-DEF000000011';
   v_USER_INSTANCE_MANAGER_ID := 'DEF00012-0000-0000-0000-DEF000000012';

------------------------
-- ADD INSTANCE_CREATOR role
------------------------
DELETE FROM roles_groups WHERE role_id = v_INSTANCE_CREATOR_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children,app_mode) select v_INSTANCE_CREATOR_ID, 'InstanceCreator', 'User Role, permission to create Instances', true, 2, false, 1
WHERE NOT EXISTS (SELECT id
                  FROM roles
                  WHERE id = v_INSTANCE_CREATOR_ID);

-- Add CREATE_INSTANCE(16)
PERFORM fn_db_add_action_group_to_role(v_INSTANCE_CREATOR_ID,16);
-- Add CREATE_DISK(1100)
PERFORM fn_db_add_action_group_to_role(v_INSTANCE_CREATOR_ID,1100);
-- Add LOGIN(1300)
PERFORM fn_db_add_action_group_to_role(v_INSTANCE_CREATOR_ID,1300);


------------------------
-- ADD USER_INSTANCE_MANAGER role
------------------------
DELETE FROM roles_groups WHERE role_id = v_USER_INSTANCE_MANAGER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children,app_mode) select v_USER_INSTANCE_MANAGER_ID, 'UserInstanceManager', 'User Role, with permission for any operation on Instances', true, 2, false, 1
WHERE NOT EXISTS (SELECT id
                  FROM roles
                  WHERE id = v_USER_INSTANCE_MANAGER_ID);

-- Add DELETE_VM(2)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,2);
-- Add EDIT_VM_PROPERTIES(3)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,3);
-- Add VM_BASIC_OPERATIONS(4)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,4);
-- Add CHANGE_VM_CD(5)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,5);
-- Add CONNECT_TO_VM(7)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,7);
-- Add IMPORT_EXPORT_VM(8)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,8);
-- Add CONFIGURE_VM_STORAGE(10)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,10);
-- Add MANIPULATE_VM_SNAPSHOTS(12)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,12);
-- Add CREATE_DISK(1100)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,1100);
-- Add ATTACH_DISK(1101)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,1101);
-- Add EDIT_DISK_PROPERTIES(1102)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,1102);
-- Add DELETE_DISK(1104)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,1104);
-- Add LOGIN(1300)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,1300);
-- Add MANIPULATE_PERMISSIONS(502)
PERFORM fn_db_add_action_group_to_role(v_USER_INSTANCE_MANAGER_ID,502);

 RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_predefined_roles_03_03_instance_types();
DROP function __temp_insert_predefined_roles_03_03_instance_types();
