Create or replace FUNCTION __temp_fix_user_vm_manager_role()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_VM_ADMIN_ID UUID;
BEGIN
   v_VM_ADMIN_ID := 'def00012-0000-0000-0000-def000000012';

-- Remove the import export vm action group from USER_VM_MANAGER role
DELETE FROM roles_groups
WHERE  role_id = v_VM_ADMIN_ID
AND    action_group_id = 8;

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_fix_user_vm_manager_role();
DROP function __temp_fix_user_vm_manager_role();