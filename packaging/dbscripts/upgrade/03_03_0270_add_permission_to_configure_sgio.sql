-- Add CONFIGURE_SCSI_GENERIC_IO action_group to DataCenterAdmin role
select fn_db_add_action_group_to_role('def00002-0000-0000-0000-def000000002', 1105);
-- Add CONFIGURE_SCSI_GENERIC_IO action_group to SuperUser role
select fn_db_add_action_group_to_role('00000000-0000-0000-0000-000000000001', 1105);