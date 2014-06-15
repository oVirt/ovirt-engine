-- MANIPULATE_PERMISSIONS = 502
-- ADD_USERS_AND_GROUPS_FROM_DIRECTORY = 503
-- TEMPLATE_ADMIN_ID = 'DEF00008-0000-0000-0000-DEF000000008'
--
-- in order to template admin to be able to add permission for users we need
-- to give it manipulate-permissions and add-users-and-groups for manipulating active
-- directory data

INSERT INTO ROLES_GROUPS(role_id,action_group_id)
    VALUES('DEF00008-0000-0000-0000-DEF000000008', 502);
INSERT INTO ROLES_GROUPS(role_id,action_group_id)
    VALUES('DEF00008-0000-0000-0000-DEF000000008', 503);
