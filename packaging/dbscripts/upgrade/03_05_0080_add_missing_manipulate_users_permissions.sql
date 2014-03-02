Create or replace FUNCTION _temp_add_missing_manipulate_users_permissions()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_ADD_USERS_AND_GROUPS_FROM_DIRECTORY INTEGER;
   v_MANIPULATE_PERMISSIONS INTEGER;
BEGIN
   v_ADD_USERS_AND_GROUPS_FROM_DIRECTORY = 503;
   v_MANIPULATE_PERMISSIONS = 502;
   INSERT INTO ROLES_GROUPS(role_id,action_group_id)
   SELECT rg.role_id, v_ADD_USERS_AND_GROUPS_FROM_DIRECTORY
   FROM ROLES_GROUPS rg
   WHERE
   action_group_id = v_MANIPULATE_PERMISSIONS
   AND NOT EXISTS (SELECT 1
                   FROM ROLES_GROUPS rg2
                   WHERE rg2.role_id = rg.role_id
                   AND action_group_id = v_ADD_USERS_AND_GROUPS_FROM_DIRECTORY);
   RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT  _temp_add_missing_manipulate_users_permissions();
drop function  _temp_add_missing_manipulate_users_permissions();
