select fn_db_add_column('job', 'is_external', 'boolean default false');
select fn_db_add_column('job', 'is_auto_cleared', 'boolean default true');
select fn_db_add_column('step', 'is_external', 'boolean default false');

-- Add External Task Injection priviledge to super user
INSERT INTO roles_groups(role_id,action_group_id) SELECT '00000000-0000-0000-0000-000000000001',1500
    WHERE NOT EXISTS (SELECT role_id,action_group_id
    from roles_groups
    WHERE role_id = '00000000-0000-0000-0000-000000000001' and
          action_group_id = 1500);

-- define a role for External Task injection
-----------------------------------
-- EXTERNAL_TASK_CREATOR_USER role
-----------------------------------
Create or replace FUNCTION __temp_insert_predefined_externa_tasks_creator_role()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_external_task_creator_user_id UUID;
BEGIN
   v_external_task_creator_user_id := 'DEF0000D-0000-0000-0000-DEF000000000';

INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_external_task_creator_user_id, 'ExternalTasksCreator', 'External Tasks Creator', true, 2, false
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_external_task_creator_user_id
                  AND name='ExternalTasksCreator'
                  AND description='External Tasks Creator'
                  AND is_readonly=true
                  AND role_type=2);

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_external_task_creator_user_id, 1500);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_predefined_externa_tasks_creator_role();
DROP function __temp_insert_predefined_externa_tasks_creator_role();


