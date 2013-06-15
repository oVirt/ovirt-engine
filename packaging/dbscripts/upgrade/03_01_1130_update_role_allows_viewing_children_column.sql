UPDATE roles
SET    allows_viewing_children  = EXISTS (SELECT 1
                                          FROM   roles_groups
                                          WHERE  roles_groups.role_id = roles.id
                                          AND    action_group_id NOT IN (1,    -- CREATE_VM
                                                                         200,  -- CREATE_TEMPLATE
                                                                         300,  -- CREATE_VM_POOL
                                                                         1100  -- CREATE_DISK
                                                                        ));
