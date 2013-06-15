INSERT INTO roles_groups(role_id,action_group_id)
        select 'DEF0000A-0000-0000-0000-DEF00000000D', -- VM Creator role id
                1100                                   -- CREATE_DISK ActionGroup
        where not exists (
                select * from roles_groups
                where role_id='DEF0000A-0000-0000-0000-DEF00000000D' and action_group_id=1100
        );
