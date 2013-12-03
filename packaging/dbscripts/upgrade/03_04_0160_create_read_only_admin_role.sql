Create or replace FUNCTION __temp_insert_read_only_admin_role()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_ROLE_ID UUID;
BEGIN
   v_ROLE_ID := 'DEF0000C-0000-0000-0000-DEF00000000C';

DELETE FROM roles_groups WHERE role_id = v_ROLE_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children,app_mode) SELECT v_ROLE_ID, 'ReadOnlyAdmin', 'Read Only Administrator Role', true, 1, true, 1
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_ROLE_ID
                  AND name='ReadOnlyAdmin'
                  AND description='Read Only Administrator Role'
                  AND is_readonly=true
                  AND role_type=1);

-- Allowing this role to login
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_ROLE_ID, 1300);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_read_only_admin_role();
DROP function __temp_insert_read_only_admin_role();
