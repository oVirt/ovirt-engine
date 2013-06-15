INSERT INTO roles_groups(role_id,action_group_id)
        select 'DEF00001-0000-0000-0000-DEF000000001', -- cluster admin role id
                502                                    -- MANIPULATE_PERMISSIONS ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='DEF00001-0000-0000-0000-DEF000000001' and action_group_id=502
        );
INSERT INTO roles_groups(role_id,action_group_id)
        select 'DEF00002-0000-0000-0000-DEF000000002', -- DC admin role id
                502                                    -- MANIPULATE_PERMISSIONS ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='DEF00002-0000-0000-0000-DEF000000002' and action_group_id=502
        );
INSERT INTO roles_groups(role_id,action_group_id)
        select 'DEF0000A-0000-0000-0000-DEF00000000F', -- Template Owner role id
                502                                    -- MANIPULATE_PERMISSIONS ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='DEF0000A-0000-0000-0000-DEF00000000F' and action_group_id=502
        );
INSERT INTO roles_groups(role_id,action_group_id)
        select 'DEF0000A-0000-0000-0000-DEF00000000B', -- Disk Operator role id
                502                                    -- MANIPULATE_PERMISSIONS ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='DEF0000A-0000-0000-0000-DEF00000000B' and action_group_id=502
        );
INSERT INTO roles_groups(role_id,action_group_id)
        select 'DEF00006-0000-0000-0000-DEF000000006', -- User VM Manager role id
                502                                    -- MANIPULATE_PERMISSIONS ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='DEF00006-0000-0000-0000-DEF000000006' and action_group_id=502
        );
