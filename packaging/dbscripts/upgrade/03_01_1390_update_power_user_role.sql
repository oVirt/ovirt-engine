DELETE
FROM roles_groups
WHERE role_id = '00000000-0000-0000-0001-000000000002' -- PowerUser role
AND action_group_id NOT IN (1300, -- LOGIN
                            1, -- CREATE_VM
                            200, -- CREATE_TEMPLATE
                            1100 -- CREATE_DISK
                           );

UPDATE roles
SET allows_viewing_children = false, description = 'User Role, allowed to create VMs, Templates and Disks'
WHERE id = '00000000-0000-0000-0001-000000000002';

