Create or replace FUNCTION __temp_insert_predefined_creator_roles()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_VM_CREATOR_USER_ID UUID;
   v_TEMPLATE_CREATOR_USER_ID UUID;
BEGIN
   v_VM_CREATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000D';
   v_TEMPLATE_CREATOR_USER_ID := 'DEF0000A-0000-0000-0000-DEF00000000E';

-------------------------
-- VM_CREATOR_USER role
-------------------------
DELETE FROM roles_groups WHERE role_id = v_VM_CREATOR_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_VM_CREATOR_USER_ID, 'VmCreator', 'VM Creator', true, 2, false
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_VM_CREATOR_USER_ID
                  AND name='VmCreator'
                  AND description='VM Creator'
                  AND is_readonly=true
                  AND role_type=2);

-- CREATE_VM
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_VM_CREATOR_USER_ID, 1);


-----------------------------
-- TEMPALTE_CREATOR_USER role
-----------------------------
DELETE FROM roles_groups WHERE role_id = v_TEMPLATE_CREATOR_USER_ID;
INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_TEMPLATE_CREATOR_USER_ID, 'TemplateCreator', 'Template Creator', true, 2, false
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_VM_CREATOR_USER_ID
                  AND name='TemplateCreator'
                  AND description='Template Creator'
                  AND is_readonly=true
                  AND role_type=2);

-- CREATE_TEMPLATE
INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_TEMPLATE_CREATOR_USER_ID, 200);


RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_predefined_creator_roles();
DROP function __temp_insert_predefined_creator_roles();
