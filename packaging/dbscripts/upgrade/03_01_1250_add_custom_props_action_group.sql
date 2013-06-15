Create or replace FUNCTION __temp_fn_db_grant_custom_props_action_group() returns void
AS $procedure$
BEGIN
-- super user
if (not exists (select 1 from roles_groups where role_id = '00000000-0000-0000-0000-000000000001' and action_group_id = 14)) then
	insert into roles_groups (role_id, action_group_id) values('00000000-0000-0000-0000-000000000001',14);
end if;

-- cluster admin
if (not exists (select 1 from roles_groups where role_id = 'DEF00001-0000-0000-0000-DEF000000001' and action_group_id = 14)) then
        insert into roles_groups (role_id, action_group_id) values('DEF00001-0000-0000-0000-DEF000000001',14);
end if;

-- dc admin
if (not exists (select 1 from roles_groups where role_id = 'DEF00002-0000-0000-0000-DEF000000002' and action_group_id = 14)) then
        insert into roles_groups (role_id, action_group_id) values('DEF00002-0000-0000-0000-DEF000000002',14);
end if;

END; $procedure$
LANGUAGE plpgsql;

select __temp_fn_db_grant_custom_props_action_group();
drop function __temp_fn_db_grant_custom_props_action_group();
