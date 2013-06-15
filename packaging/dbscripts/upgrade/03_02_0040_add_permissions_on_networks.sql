-- Update existing roles with action groups and introduce new networks role

Create or replace FUNCTION __temp_insert_predefined_roles_03_02_0030()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_SUPER_USER_ID_0001 UUID;
   v_DATA_CENTER_ADMIN_ID UUID;
   v_NETWORK_ADMIN_ID UUID;
   v_NETWORK_USER_ID UUID;
   v_everyone_object_id  UUID;

BEGIN
   v_SUPER_USER_ID_0001 := '00000000-0000-0000-0000-000000000001';
   v_DATA_CENTER_ADMIN_ID := 'DEF00002-0000-0000-0000-DEF000000002';
   v_NETWORK_ADMIN_ID := 'DEF00005-0000-0000-0000-DEF000000005';
   v_NETWORK_USER_ID := 'DEF0000A-0000-0000-0000-DEF000000010';


------------------------------------------------
--- Update existing roles with new Action Groups
------------------------------------------------
-- Add ActionGroup 704 (CREATE_STORAGE_POOL_NETWORK) to any role which contains ActionGroup 703 (CONFIGURE_STORAGE_POOL_NETWORK)
INSERT INTO roles_groups (role_id, action_group_id)
SELECT DISTINCT role_id, 704
FROM roles_groups a
WHERE NOT EXISTS (SELECT 1
                  FROM roles_groups b
                  WHERE b.role_id = a.role_id
                  AND b.action_group_id = 704)
AND EXISTS (SELECT 1
            FROM roles_groups b
            WHERE b.role_id = a.role_id
            AND b.action_group_id = 703);

-- Add ActionGroup 705 (DELETE_STORAGE_POOL_NETWORK) to any role which contains ActionGroup 703 (CONFIGURE_STORAGE_POOL_NETWORK)
INSERT INTO roles_groups (role_id, action_group_id)
SELECT DISTINCT role_id, 705
FROM roles_groups a
WHERE NOT EXISTS (SELECT 1
                  FROM roles_groups b
                  WHERE b.role_id = a.role_id
                  AND b.action_group_id = 705)
AND EXISTS (SELECT 1
            FROM roles_groups b
            WHERE b.role_id = a.role_id
            AND b.action_group_id = 703);

-------------------------
--- Update SuperUser role
-------------------------

-- Add ASSIGN_CLUSTER_NETWORK
PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ID_0001, 404);

--------------------------------
-- UPDATE DATA_CENTER_ADMIN role
--------------------------------

-- Add ASSIGN_CLUSTER_NETWORK
PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ID, 404);

----------------------------
-- UPDATE NETWORK_ADMIN role
----------------------------

-- Add ASSIGN_CLUSTER_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 404);

-- Add PORT_MIRRORING
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 1200);

-- Add CONFIGURE_STORAGE_POOL_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID ,703);

-- Add CREATE_STORAGE_POOL_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID ,704);

-- Add DELETE_STORAGE_POOL_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID ,705);

-- Add CONFIGURE_VM_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID ,9);

-- Add CONFIGURE_TEMPLATE_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID ,204);

-- Delete MANIPUTLATE_HOST
DELETE FROM roles_groups WHERE role_id = v_NETWORK_ADMIN_ID AND action_group_id = 103;

------------------------
-- ADD NETWORK_USER role
------------------------
DELETE FROM roles_groups WHERE role_id = v_NETWORK_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type) SELECT v_NETWORK_USER_ID, 'NetworkUser', 'Network User', true, 2
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_NETWORK_USER_ID
                  AND name='NetworkUser'
                  AND description='Network User'
                  AND is_readonly=true
                  AND role_type=2);

-- Add CONFIGURE_VM_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_USER_ID, 9);

-- Add CONFIGURE_TEMPLATE_NETWORK
PERFORM fn_db_add_action_group_to_role(v_NETWORK_USER_ID, 204);

-------------------------------------------------------
-- Grant NetworkUser role to 'everyone' on all networks
-------------------------------------------------------
v_everyone_object_id := getGlobalIds('everyone');

INSERT INTO permissions (id,
                         role_id,
                         ad_element_id,
                         object_id,
                         object_type_id)
      (SELECT uuid_generate_v1(),
             v_NETWORK_USER_ID,
             v_everyone_object_id,
             id,
             20
       FROM network);

 RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_predefined_roles_03_02_0030();
DROP function __temp_insert_predefined_roles_03_02_0030();

