-- remove install vds action group(130) and upgrade oVirt Node action group (131) to predefined roles
Create or replace FUNCTION __temp_remove_install_vds_upgrade_node_action_group_03_06_0360()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_SUPER_USER_ID_0001 UUID;
   v_DATA_CENTER_ADMIN_ID UUID;
   v_CLUSTER_ADMIN_ID UUID;

BEGIN
   v_SUPER_USER_ID_0001 := '00000000-0000-0000-0000-000000000001';
   v_DATA_CENTER_ADMIN_ID := 'DEF00002-0000-0000-0000-DEF000000002';
   v_CLUSTER_ADMIN_ID := 'DEF00001-0000-0000-0000-DEF000000001';

-------------------------
--- Update SuperUser role
-------------------------
DELETE FROM roles_groups WHERE role_id = v_SUPER_USER_ID_0001 AND action_group_id=130;
DELETE FROM roles_groups WHERE role_id = v_SUPER_USER_ID_0001 AND action_group_id=131;

--------------------------------
-- UPDATE DATA_CENTER_ADMIN role
--------------------------------
DELETE FROM roles_groups WHERE role_id = v_DATA_CENTER_ADMIN_ID AND action_group_id=130;
DELETE FROM roles_groups WHERE role_id = v_DATA_CENTER_ADMIN_ID AND action_group_id=131;

--------------------------------
-- UPDATE CLUSTER_ADMIN role
--------------------------------
DELETE FROM roles_groups WHERE role_id = v_CLUSTER_ADMIN_ID AND action_group_id=130;
DELETE FROM roles_groups WHERE role_id = v_CLUSTER_ADMIN_ID AND action_group_id=131;

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_remove_install_vds_upgrade_node_action_group_03_06_0360();
DROP function __temp_remove_install_vds_upgrade_node_action_group_03_06_0360();
