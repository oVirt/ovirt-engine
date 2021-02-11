-- Add RESET_VM action group to SUPER_USER
select fn_db_add_action_group_to_role('00000000-0000-0000-0000-000000000001', 23);
-- Add RESET_VM action group to ENGINE_USER
select fn_db_add_action_group_to_role('00000000-0000-0000-0001-000000000001', 23);
-- Add RESET_VM action group to ClusterAdmin
select fn_db_add_action_group_to_role('def00001-0000-0000-0000-def000000001', 23);
-- Add RESET_VM action group to DataCenterAdmin
select fn_db_add_action_group_to_role('def00002-0000-0000-0000-def000000002', 23);
-- Add RESET_VM action group to UserVmManager
select fn_db_add_action_group_to_role('def00006-0000-0000-0000-def000000006', 23);
-- Add RESET_VM action group to UserVmRunTimeManager
select fn_db_add_action_group_to_role('def00006-0000-0000-0000-def000000011', 23);
-- Add RESET_VM action group to UserInstanceManager
select fn_db_add_action_group_to_role('def00012-0000-0000-0000-def000000012', 23);
