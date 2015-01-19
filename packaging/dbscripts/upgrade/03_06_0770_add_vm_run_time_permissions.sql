Create or replace FUNCTION __temp_set_vm_run_time_permissions()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_VM_RUN_TIME_MANAGER_ID UUID;

BEGIN
   v_VM_RUN_TIME_MANAGER_ID := 'DEF00006-0000-0000-0000-DEF000000011';

    -- Add user role for vm run time manager
    INSERT INTO roles(id,name,description,is_readonly,role_type, app_mode) SELECT v_VM_RUN_TIME_MANAGER_ID, 'UserVmRunTimeManager', 'User Role, with permissions for any operations on VMs except snapshot manipulation', true, 2, 1;

    -- copy the properties of VM_OPERATOR role (role_id = def00006-0000-0000-0000-def000000006) without the vm snpashot manipulation (action group id = 12) to the new role of VM_RUN_TIME_MANAGER_ID.
    INSERT INTO roles_groups (role_id, action_group_id)
    SELECT v_VM_RUN_TIME_MANAGER_ID, action_group_id
    FROM roles_groups a
    WHERE role_id = 'def00006-0000-0000-0000-def000000006' AND action_group_id != 12;

END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_set_vm_run_time_permissions();
DROP function __temp_set_vm_run_time_permissions();

