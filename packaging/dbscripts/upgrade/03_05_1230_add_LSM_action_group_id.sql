Create or replace FUNCTION __temp_add_LSM_permissions()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_action_group_id INTEGER;

BEGIN
   v_action_group_id := 1107;

-- copy the action group id to all the roles which has permissions to CONFIGURE_DISK_STORAGE (1103)
INSERT INTO roles_groups (role_id, action_group_id)
SELECT role_id, v_action_group_id
FROM roles_groups a
WHERE action_group_id = 1103;

END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_add_LSM_permissions();
DROP function __temp_add_LSM_permissions();
