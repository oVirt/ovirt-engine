Create or replace FUNCTION __temp_set_disk_profiles_permissions()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_DISK_PROFILE_USER_ID UUID;

BEGIN
   v_DISK_PROFILE_USER_ID := 'DEF00020-0000-0000-0000-ABC000000010';

    -- Add disk_profile_user role
    INSERT INTO roles(id,name,description,is_readonly,role_type, app_mode) SELECT v_DISK_PROFILE_USER_ID, 'DiskProfileUser', 'Disk Profile User', true, 2, 1
    WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                      FROM roles
                      WHERE id = v_DISK_PROFILE_USER_ID
                      AND name='DiskProfileUser'
                      AND description='Disk Profile User'
                      AND is_readonly=true
                      AND role_type=2
                      AND app_mode=1);

    -- Add 'Attach disk profile' action group to roles:
    -- newly created disk profile user
    PERFORM fn_db_add_action_group_to_role(v_DISK_PROFILE_USER_ID, 1563);

    -- Add action group to each role that contains CRUD action groups on disk profile
    -- 1560-  CONFIGURE_STORAGE_DISK_PROFILE, 1561- CREATE_STORAGE_DISK_PROFILE, 1562- DELETE_STORAGE_DISK_PROFILE
    INSERT INTO roles_groups (role_id, action_group_id)
    SELECT DISTINCT role_id, 1563
    FROM roles_groups a
    WHERE NOT EXISTS (SELECT 1
                      FROM roles_groups b
                      WHERE b.role_id = a.role_id
                      AND b.action_group_id = 1563)
    AND EXISTS (SELECT 1
                FROM roles_groups b
                WHERE b.role_id = a.role_id
                AND (b.action_group_id = 1560 OR b.action_group_id = 1561 OR b.action_group_id = 1562));

    -- Add permission EVERYONE on DiskProfileUser role on each disk profile.
    INSERT INTO permissions(id,
                         role_id,
                         ad_element_id,
                         object_id,
                         object_type_id)
        SELECT uuid_generate_v1(),
        v_DISK_PROFILE_USER_ID,
        'EEE00000-0000-0000-0000-123456789EEE', -- Everyone
        disk_profiles.id,
        29 -- disk profile object id
        FROM disk_profiles;

END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_set_disk_profiles_permissions();
DROP function __temp_set_disk_profiles_permissions();

