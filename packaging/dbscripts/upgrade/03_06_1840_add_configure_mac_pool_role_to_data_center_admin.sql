
Create or replace FUNCTION __temp_add_configure_mac_pool_role_to_data_center_admin()
RETURNS VOID
   AS $procedure$
DECLARE
  v_DATA_CENTER_ADMIN      UUID;

  v_CONFIGURE_MAC_POOL INTEGER;

BEGIN

  v_DATA_CENTER_ADMIN := 'DEF00002-0000-0000-0000-DEF000000002';
  v_CONFIGURE_MAC_POOL := 1663;

  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_DATA_CENTER_ADMIN, v_CONFIGURE_MAC_POOL);

  RETURN;
END; $procedure$
LANGUAGE plpgsql;


SELECT __temp_add_configure_mac_pool_role_to_data_center_admin();
DROP FUNCTION __temp_add_configure_mac_pool_role_to_data_center_admin();
