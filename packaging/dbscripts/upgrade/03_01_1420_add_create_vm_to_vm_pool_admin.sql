INSERT INTO roles_groups(role_id,action_group_id)
        select 'DEF00007-0000-0000-0000-DEF000000007', -- VMPoolAdmin
                1                                    -- CREATE_VM ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='DEF00007-0000-0000-0000-DEF000000007' and action_group_id=1
        );

