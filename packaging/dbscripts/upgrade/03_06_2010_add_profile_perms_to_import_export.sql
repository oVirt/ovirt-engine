-- Add attach cpu profile and attach disk profile permissions to all roles
-- that already have the import/export vm permissions

-- import/export ActionGroup 8
-- attach disk profile AG 1563
-- assign cpu profile AG 1668

SELECT fn_db_add_action_group_to_role(r.role_id, 1563)
FROM roles_groups r
WHERE action_group_id=8;

SELECT fn_db_add_action_group_to_role(r.role_id, 1668)
FROM roles_groups r
WHERE action_group_id=8;
