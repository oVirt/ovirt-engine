-- Update existing roles with action groups of network label actions

Create or replace FUNCTION __temp_add_action_groups_of_network_labels_03_04_0340()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_SUPER_USER_ID_0001 UUID;
   v_DATA_CENTER_ADMIN_ID UUID;
   v_NETWORK_ADMIN_ID UUID;
   v_HOST_ADMIN_ID UUID;

BEGIN
   v_SUPER_USER_ID_0001 := '00000000-0000-0000-0000-000000000001';
   v_DATA_CENTER_ADMIN_ID := 'DEF00002-0000-0000-0000-DEF000000002';
   v_NETWORK_ADMIN_ID := 'DEF00005-0000-0000-0000-DEF000000005';
   v_HOST_ADMIN_ID := 'DEF00004-0000-0000-0000-DEF000000004';

------------------------------------------------
--- Update existing roles with new Action Groups
------------------------------------------------

-- Action 163: LabelNetwork
-- Action 164: UnlabelNetwork
-- Action 165: LabelNic
-- Action 166: UnlabelNic

-------------------------
--- Update SuperUser role
-------------------------
PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ID_0001, 163);
PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ID_0001, 164);
PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ID_0001, 165);
PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ID_0001, 166);

--------------------------------
-- UPDATE DATA_CENTER_ADMIN role
--------------------------------
PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ID, 163);
PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ID, 164);
PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ID, 165);
PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ID, 166);

----------------------------
-- UPDATE NETWORK_ADMIN role
----------------------------
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 163);
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 164);
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 165);
PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 166);

----------------------------
-- UPDATE HOST_ADMIN role
----------------------------
PERFORM fn_db_add_action_group_to_role(v_HOST_ADMIN_ID, 165);
PERFORM fn_db_add_action_group_to_role(v_HOST_ADMIN_ID, 166);


 RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_add_action_groups_of_network_labels_03_04_0340();
DROP function __temp_add_action_groups_of_network_labels_03_04_0340();

