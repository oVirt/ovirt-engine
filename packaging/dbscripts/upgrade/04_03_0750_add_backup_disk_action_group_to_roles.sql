-- Add BACKUP_DISK action group to SUPER_USER
select fn_db_add_action_group_to_role('00000000-0000-0000-0000-000000000001', 1110);
-- Add BACKUP_DISK action group to ENGINE_USER
select fn_db_add_action_group_to_role('def00001-0000-0000-0000-def000000001', 1110);
-- Add BACKUP_DISK action group to POWER_USER
select fn_db_add_action_group_to_role('def00002-0000-0000-0000-def000000002', 1110);
-- Add BACKUP_DISK action group to STORAGE_ADMIN
select fn_db_add_action_group_to_role('def00003-0000-0000-0000-def000000003', 1110);
-- Add BACKUP_DISK action group to VM_OPERATOR
select fn_db_add_action_group_to_role('def00006-0000-0000-0000-def000000006', 1110);
-- Add BACKUP_DISK action group to DISK_OPERATOR
select fn_db_add_action_group_to_role('def0000a-0000-0000-0000-def00000000b', 1110);
-- Add BACKUP_DISK action group to INSTANCE_OPERATOR
select fn_db_add_action_group_to_role('def00012-0000-0000-0000-def000000012', 1110);
-- Add BACKUP_DISK action group to INSTANCE_CREATOR
select fn_db_add_action_group_to_role('def00006-0000-0000-0000-def000000011', 1110);