Create or replace FUNCTION __temp_set_edit_profile_permissions()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_USER_PROFILE_EDITOR_ID UUID;

BEGIN
   v_USER_PROFILE_EDITOR_ID := 'DEF00021-0000-0000-0000-DEF000000015';

    -- Add edit_profile_user role
    INSERT INTO roles(id,name,description,is_readonly,role_type, app_mode)
    SELECT v_USER_PROFILE_EDITOR_ID, 'UserProfileEditor',
           'Role that allow users to edit the UserProfile', true, 2, 255
    WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                      FROM roles
                      WHERE id = v_USER_PROFILE_EDITOR_ID);

    PERFORM fn_db_add_action_group_to_role(v_USER_PROFILE_EDITOR_ID, 504);

    INSERT INTO permissions(id,
                         role_id,
                         ad_element_id,
                         object_id,
                         object_type_id)
    VALUES(uuid_generate_v1(),
           v_USER_PROFILE_EDITOR_ID,
           'EEE00000-0000-0000-0000-123456789EEE', -- Everyone
           'AAA00000-0000-0000-0000-123456789AAA',
           1);  -- System
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_set_edit_profile_permissions();
DROP function __temp_set_edit_profile_permissions();

