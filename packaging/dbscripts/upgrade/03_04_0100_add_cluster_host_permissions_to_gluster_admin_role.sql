Create or replace FUNCTION __temp_add_host_permissions_to_gluster_admin_role()
RETURNS VOID
   AS $procedure$
   DECLARE
      v_GLUSTER_ADMIN_ROLE_ID UUID;
BEGIN
      v_GLUSTER_ADMIN_ROLE_ID := 'DEF0000b-0000-0000-0000-DEF00000000b';

--CREATE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 400
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=400);

--EDIT_CLUSTER_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 401
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=401);

--DELETE_CLUSTER
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 402
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=402);

--CONFIGURE_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 403
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=403);

--ASSIGN_CLUSTER_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 404
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=404);

--CREATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 100
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=100);

--EDIT_HOST_CONFIGURATION
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 101
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=101);

--DELETE_HOST
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 102
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=102);

--MANIPULATE_HOST
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 103
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=103);

--CONFIGURE_HOST_NETWORK
INSERT INTO roles_groups(role_id,action_group_id) SELECT v_GLUSTER_ADMIN_ROLE_ID, 104
WHERE not exists (SELECT role_id, action_group_id FROM roles_groups
        WHERE role_id=v_GLUSTER_ADMIN_ROLE_ID and action_group_id=104);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

select __temp_add_host_permissions_to_gluster_admin_role();
drop function __temp_add_host_permissions_to_gluster_admin_role();

