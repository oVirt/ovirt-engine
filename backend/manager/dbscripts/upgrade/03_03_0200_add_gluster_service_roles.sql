Create or replace FUNCTION __temp_insert_predefined_gluster_service_roles()
RETURNS VOID
   AS $procedure$
   DECLARE
      v_super_user_id_0001 UUID;
      v_GLUSTER_ADMIN_ROLE_ID UUID;
      v_ACTION_GROUP_ID_GLUSTER_SERVICE INTEGER;
BEGIN
      v_super_user_id_0001 := '00000000-0000-0000-0000-000000000001';
      v_GLUSTER_ADMIN_ROLE_ID := 'DEF0000b-0000-0000-0000-DEF00000000b';
      v_ACTION_GROUP_ID_GLUSTER_SERVICE := 1004;

--MANIPULATE_GLUSTER_SERVER_SERVICES
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_super_user_id_0001, v_ACTION_GROUP_ID_GLUSTER_SERVICE
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_super_user_id_0001 and action_group_id=v_ACTION_GROUP_ID_GLUSTER_SERVICE);

-- Map all gluster service action groups to the gluster admin role
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, v_ACTION_GROUP_ID_GLUSTER_SERVICE
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=v_ACTION_GROUP_ID_GLUSTER_SERVICE);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

select __temp_insert_predefined_gluster_service_roles();
drop function __temp_insert_predefined_gluster_service_roles();

