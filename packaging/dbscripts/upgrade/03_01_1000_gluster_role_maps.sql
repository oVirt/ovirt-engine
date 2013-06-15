Create or replace FUNCTION __temp_insert_predefined_gluster_roles()
RETURNS VOID
   AS $procedure$
   DECLARE
      v_super_user_id_0001 UUID;
      v_GLUSTER_ADMIN_ROLE_ID UUID;
BEGIN
      v_super_user_id_0001 := '00000000-0000-0000-0000-000000000001';
      v_GLUSTER_ADMIN_ROLE_ID := 'DEF0000b-0000-0000-0000-DEF00000000b';


--CREATE_GLUSTER_VOLUME
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_super_user_id_0001, 1000
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_super_user_id_0001 and action_group_id=1000);

--MANIPULATE_GLUSTER_VOLUME
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_super_user_id_0001, 1001
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_super_user_id_0001 and action_group_id=1001);

--DELETE_GLUSTER_VOLUME
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_super_user_id_0001, 1002
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_super_user_id_0001 and action_group_id=1002);

--------------
-- GLUSTER_ADMIN_USER role
--------------
INSERT INTO roles(id,name,description,is_readonly,role_type) SELECT v_GLUSTER_ADMIN_ROLE_ID, 'GlusterAdmin','Gluster Admin',true,1
WHERE not exists (SELECT id,name,description,is_readonly,role_type from roles
WHERE id= v_GLUSTER_ADMIN_ROLE_ID and name='GlusterAdmin' and description='Gluster Admin' and is_readonly=true and role_type=1);

-- Map all gluster action groups to the gluster admin role
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 1000
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=1000);

INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 1001
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=1001);

INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 1002
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=1002);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

select __temp_insert_predefined_gluster_roles();
drop function __temp_insert_predefined_gluster_roles();

