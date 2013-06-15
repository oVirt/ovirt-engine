Create or replace FUNCTION __temp_insert_predefined_owner_roles()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_TEMPLATE_OWNER_USER_ID UUID;
BEGIN
   v_TEMPLATE_OWNER_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000F';

-----------------------------
-- TEMPALTE_OWNER_USER role
-----------------------------
DELETE FROM roles_groups WHERE role_id = v_TEMPLATE_OWNER_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_TEMPLATE_OWNER_USER_ID, 'TemplateOwner', 'Template Owner', true, 2, true
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_TEMPLATE_OWNER_USER_ID
                  AND name='TemplateOwner'
                  AND description='Template Owner'
                  AND is_readonly=true
                  AND role_type=2);

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, 201);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, 202);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, 203);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, 204);
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_OWNER_USER_ID, 1300);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_predefined_owner_roles();
DROP function __temp_insert_predefined_owner_roles();
