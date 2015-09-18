-- Add CONNECT_TO_SERIAL_CONSOLE action group to SUPER_USER
select fn_db_add_action_group_to_role('00000000-0000-0000-0000-000000000001', 1664);
-- Add CONNECT_TO_SERIAL_CONSOLE action group to VM_OPERATOR
select fn_db_add_action_group_to_role('DEF00006-0000-0000-0000-DEF000000006', 1664);
-- Add CONNECT_TO_SERIAL_CONSOLE action group to INSTANCE_OPERATOR
select fn_db_add_action_group_to_role('DEF00012-0000-0000-0000-DEF000000012', 1664);
