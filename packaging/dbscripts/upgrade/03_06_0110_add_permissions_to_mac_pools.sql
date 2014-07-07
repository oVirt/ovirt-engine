
Create or replace FUNCTION __temp_insert_mac_pool_roles_and_permissions()
RETURNS VOID
   AS $procedure$
DECLARE
  v_EVERYONE           UUID;
  v_MAC_POOL_ADMIN     UUID;
  v_MAC_POOL_USER      UUID;
  v_SUPER_USER_ID      UUID;

  v_CREATE_MAC_POOL    INTEGER;
  v_EDIT_MAC_POOL      INTEGER;
  v_DELETE_MAC_POOL    INTEGER;
  v_CONFIGURE_MAC_POOL INTEGER;
  v_LOGIN              INTEGER;

  v_APP_MODE           INTEGER;

BEGIN
  v_EVERYONE := 'EEE00000-0000-0000-0000-123456789EEE';
  v_MAC_POOL_ADMIN := 'DEF00013-0000-0000-0000-DEF000000013';
  v_MAC_POOL_USER := 'DEF00014-0000-0000-0000-DEF000000014';
  v_SUPER_USER_ID := '00000000-0000-0000-0000-000000000001';

  v_CREATE_MAC_POOL := 1660;
  v_EDIT_MAC_POOL := 1661;
  v_DELETE_MAC_POOL := 1662;
  v_CONFIGURE_MAC_POOL := 1663;
  v_LOGIN := 1300;

  v_APP_MODE := 1;

  INSERT INTO roles (id,
                     name,
                     description,
                     is_readonly,
                     role_type,
                     allows_viewing_children,
                     app_mode)
    SELECT
      v_MAC_POOL_ADMIN,
      'MacPoolAdmin',
      'MAC Pool Administrator Role, permission for manipulation with MAC pools',
      true,
      1,
      true,
      v_APP_MODE;

  INSERT INTO roles (id,
                     name,
                     description,
                     is_readonly,
                     role_type,
                     allows_viewing_children,
                     app_mode)
    SELECT
      v_MAC_POOL_USER,
      'MacPoolUser',
      'MAC Pool User Role, permission allowing using MAC pools',
      true,
      1,
      true,
      v_APP_MODE;

  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_MAC_POOL_ADMIN, v_CREATE_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_MAC_POOL_ADMIN, v_EDIT_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_MAC_POOL_ADMIN, v_DELETE_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_MAC_POOL_ADMIN, v_CONFIGURE_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_MAC_POOL_ADMIN, v_LOGIN);

  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_MAC_POOL_USER, v_CONFIGURE_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_MAC_POOL_USER, v_LOGIN);

  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_SUPER_USER_ID, v_CREATE_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_SUPER_USER_ID, v_EDIT_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_SUPER_USER_ID, v_DELETE_MAC_POOL);
  INSERT INTO roles_groups (role_id, action_group_id) VALUES (v_SUPER_USER_ID, v_CONFIGURE_MAC_POOL);

  RETURN;
END; $procedure$
LANGUAGE plpgsql;


SELECT __temp_insert_mac_pool_roles_and_permissions();
DROP FUNCTION __temp_insert_mac_pool_roles_and_permissions();
