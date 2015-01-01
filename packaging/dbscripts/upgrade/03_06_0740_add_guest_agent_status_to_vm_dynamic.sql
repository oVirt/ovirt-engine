-- add guest_agent_status Enum field to vm_dynamic
-- values can be:
--   0 DoesntExist
--   1 Exists
--   2 UpdateNeeded
SELECT fn_db_add_column('vm_dynamic', 'guest_agent_status', 'int default 0');
