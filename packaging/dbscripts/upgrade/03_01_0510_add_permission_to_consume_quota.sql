-- Add quota consume group command to DataCenterAdmin
INSERT INTO roles_groups(role_id,action_group_id)
        select 'def00002-0000-0000-0000-def000000002', -- Data center admin role id
                901                                    -- Manipulate quota ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='def00002-0000-0000-0000-def000000002' and action_group_id=901
        );

---- Add quota consume group command to SuperUser
INSERT INTO roles_groups(role_id,action_group_id)
        select '00000000-0000-0000-0000-000000000001', -- Super user role id
                901                                    -- Manipulate quota ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='00000000-0000-0000-0000-000000000001' and action_group_id=901
        );
