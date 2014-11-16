-- We change the VM permission to be more granular,
-- instead of grouping the reboot, stop, shut down, hibernate, run and run-once
-- into basic operation each is now a role group
CREATE OR REPLACE FUNCTION __temp_update_vm_basic_roles()
RETURNS void
AS $procedure$
DECLARE
    v_cur CURSOR FOR SELECT * FROM roles_groups WHERE action_group_id = 4; -- 4=VM_BASIC_OPERATIONS
    v_record roles_groups%ROWTYPE;
BEGIN
       OPEN v_cur;
       LOOP
           FETCH v_cur INTO v_record;
           EXIT WHEN NOT FOUND;
           insert into roles_groups (role_id, action_group_id) values (v_record.role_id, 17); -- 17=REBOOT_VM
           insert into roles_groups (role_id, action_group_id) values (v_record.role_id, 18); -- 18=STOP_VM
           insert into roles_groups (role_id, action_group_id) values (v_record.role_id, 19); -- 19=SHUT_DOWN_VM
           insert into roles_groups (role_id, action_group_id) values (v_record.role_id, 21); -- 21=HIBERNATE_VM
           insert into roles_groups (role_id, action_group_id) values (v_record.role_id, 22); -- 22=RUN_VM
       END LOOP;
       CLOSE v_cur;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_update_vm_basic_roles();
DROP FUNCTION __temp_update_vm_basic_roles();

-- delete VM_BASIC_OPERATIONS
DELETE FROM roles_groups WHERE action_group_id = 4;
