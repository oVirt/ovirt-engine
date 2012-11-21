-- Adding to audit_log fields used for External Events that may be invoked by plug-ins
select fn_db_add_column('audit_log', 'origin', 'VARCHAR(255)  DEFAULT ''oVirt''');
select fn_db_add_column('audit_log', 'custom_event_id', 'INTEGER  DEFAULT -1');
select fn_db_add_column('audit_log', 'event_flood_in_sec', 'INTEGER  DEFAULT 30');
select fn_db_add_column('audit_log', 'custom_data', 'TEXT DEFAULT ''''');
select fn_db_add_column('audit_log', 'deleted', 'BOOLEAN DEFAULT false');

-- Add an Index on origin,custom_event_id
CREATE UNIQUE INDEX audit_log_origin_custom_event_id_idx ON audit_log
(origin, custom_event_id)
where origin not ilike 'ovirt';

-- Add External Event Injection priviledge to super user
INSERT INTO roles_groups(role_id,action_group_id) VALUES('00000000-0000-0000-0000-000000000001',1400);

-- define a role for External Event injection
-----------------------------------
-- EXTERNAL_EVENT_CREATOR_USER role
-----------------------------------
Create or replace FUNCTION __temp_insert_predefined_externa_events_creator_role()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_external_event_creator_user_id UUID;
BEGIN
   v_external_event_creator_user_id := 'DEF0000C-0000-0000-0000-DEF000000000';

INSERT INTO roles(id,name,description,is_readonly,role_type,allows_viewing_children) SELECT v_external_event_creator_user_id, 'ExternalEventsCreator', 'External Events Creator', true, 2, false
WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                  FROM roles
                  WHERE id = v_external_event_creator_user_id
                  AND name='ExternalEventsCreator'
                  AND description='External Events Creator'
                  AND is_readonly=true
                  AND role_type=2);

INSERT INTO roles_groups(role_id,action_group_id) VALUES(v_external_event_creator_user_id, 1400);

RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_predefined_externa_events_creator_role();
DROP function __temp_insert_predefined_externa_events_creator_role();
