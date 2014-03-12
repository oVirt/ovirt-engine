-- Add install vds action group(130) and upgrade oVirt Node action group (131) to predefined roles
Create or replace FUNCTION __temp_add_install_vds_upgrade_node_action_group_03_05_0140()
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
PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ID_0001, 130);
PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ID_0001, 131);

--------------------------------
-- UPDATE DATA_CENTER_ADMIN role
--------------------------------
PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ID, 130);
PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ID, 131);

--------------------------------
-- UPDATE CLUSTER_ADMIN role
--------------------------------
PERFORM fn_db_add_action_group_to_role(v_CLUSTER_ADMIN_ID, 130);
PERFORM fn_db_add_action_group_to_role(v_CLUSTER_ADMIN_ID, 131);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_add_install_vds_upgrade_node_action_group_03_05_0140();
DROP function __temp_add_install_vds_upgrade_node_action_group_03_05_0140();

