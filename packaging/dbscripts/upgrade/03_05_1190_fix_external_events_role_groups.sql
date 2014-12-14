CREATE OR REPLACE FUNCTION __temp_fix_external_events_role_groups() RETURNS VOID AS $$
BEGIN
    UPDATE roles_groups set action_group_id = 1400
        where role_id = 'def0000c-0000-0000-0000-def000000000' and action_group_id = 1500;
    IF not exists (select 1 from roles_groups where role_id = '00000000-0000-0000-0000-000000000001' and action_group_id = 1400) THEN
        insert into roles_groups(role_id, action_group_id) values ('00000000-0000-0000-0000-000000000001', 1400) ;
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT  __temp_fix_external_events_role_groups();
DROP FUNCTION __temp_fix_external_events_role_groups();

