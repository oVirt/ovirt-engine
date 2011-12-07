INSERT INTO roles_groups(role_id,action_group_id)
        select '00000000-0000-0000-0001-000000000002', -- power user role id
                502                                    -- MANIPULATE_PERMISSIONS ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='00000000-0000-0000-0001-000000000002' and action_group_id=502
        );
