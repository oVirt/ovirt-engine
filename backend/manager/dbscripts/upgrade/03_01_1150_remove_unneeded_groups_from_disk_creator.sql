Create or replace FUNCTION __temp_fix_disk_creator_role()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_DISK_CREATOR_USER_ID UUID;
BEGIN
   v_DISK_CREATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000C';

-- Remove the action groups other than CREATE DISK from DISK_CREATOR role
DELETE FROM roles_groups
WHERE  role_id = v_DISK_CREATOR_USER_ID
AND    action_group_id != 1100;

-- Make the role not inheritable
UPDATE roles
SET    allows_viewing_children = FALSE
WHERE  id = v_DISK_CREATOR_USER_ID;

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_fix_disk_creator_role();
DROP function __temp_fix_disk_creator_role();
