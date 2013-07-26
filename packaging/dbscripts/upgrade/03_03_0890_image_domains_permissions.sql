-- Adding the ACCESS_IMAGE_STORAGE action to the relevant roles
INSERT INTO roles_groups(role_id,action_group_id)
    SELECT '00000000-0000-0000-0001-000000000001', -- UserRole
            1106                                   -- ACCESS_IMAGE_STORAGE
    WHERE NOT EXISTS (
        SELECT * FROM roles_groups
            WHERE role_id='00000000-0000-0000-0001-000000000001' and
                  action_group_id=1106
    );
INSERT INTO roles_groups(role_id,action_group_id)
    SELECT 'def00008-0000-0000-0000-def000000008', -- TemplateAdmin
            1106                                   -- ACCESS_IMAGE_STORAGE
    WHERE NOT EXISTS (
        SELECT * FROM roles_groups
            WHERE role_id='def00008-0000-0000-0000-def000000008' and
                  action_group_id=1106
    );
INSERT INTO roles_groups(role_id,action_group_id)
    SELECT '00000000-0000-0000-0000-000000000001', -- SuperUser
            1106                                   -- ACCESS_IMAGE_STORAGE
    WHERE NOT EXISTS (
        SELECT * FROM roles_groups
            WHERE role_id='00000000-0000-0000-0000-000000000001' and
                  action_group_id=1106
    );
INSERT INTO roles_groups(role_id,action_group_id)
    SELECT 'def00003-0000-0000-0000-def000000003', -- StorageAdmin
            1106                                   -- ACCESS_IMAGE_STORAGE
    WHERE NOT EXISTS (
        SELECT * FROM roles_groups
            WHERE role_id='def00003-0000-0000-0000-def000000003' and
                  action_group_id=1106
    );
INSERT INTO roles_groups(role_id,action_group_id)
    SELECT '00000000-0000-0000-0001-000000000002', -- PowerUserRole
            1106                                   -- ACCESS_IMAGE_STORAGE
    WHERE NOT EXISTS (
        SELECT * FROM roles_groups
            WHERE role_id='00000000-0000-0000-0001-000000000002' and
                  action_group_id=1106
    );
