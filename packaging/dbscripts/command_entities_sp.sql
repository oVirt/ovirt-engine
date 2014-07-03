CREATE OR REPLACE FUNCTION InsertCommandEntity (v_command_id uuid,
       v_command_type int,
       v_root_command_id uuid,
       v_job_id uuid,
       v_step_id uuid,
       v_action_parameters text,
       v_action_parameters_class varchar(256),
       v_status varchar(20),
       v_callback_enabled boolean,
       v_return_value text,
       v_return_value_class varchar(256))
RETURNS VOID
   AS $procedure$
BEGIN
       INSERT INTO command_entities(command_id, command_type, root_command_id, job_id, step_id, action_parameters, action_parameters_class, created_at, status, callback_enabled, return_value, return_value_class)
              VALUES(v_command_id, v_command_type, v_root_command_id, v_job_id, v_step_id, v_action_parameters, v_action_parameters_class, NOW(), v_status, v_callback_enabled, v_return_value, v_return_value_class);
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateCommandEntity (v_command_id uuid,
       v_command_type int,
       v_root_command_id uuid,
       v_job_id uuid,
       v_step_id uuid,
       v_action_parameters text,
       v_action_parameters_class varchar(256),
       v_status varchar(20),
       v_callback_enabled boolean,
       v_return_value text,
       v_return_value_class varchar(256))
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE command_entities
      SET command_type = v_command_type ,
          root_command_id = v_root_command_id,
          job_id = v_job_id,
          step_id = v_step_id,
          action_parameters = v_action_parameters,
          action_parameters_class = v_action_parameters_class,
          status = v_status,
          callback_enabled = v_callback_enabled,
          return_value = v_return_value,
          return_value_class = v_return_value_class
      WHERE command_id = v_command_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateCommandEntityStatus (v_command_id uuid,
       v_status varchar(20))
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE command_entities
      SET status = v_status
      WHERE command_id = v_command_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateCommandEntityNotified(v_command_id uuid,
       v_callback_notified boolean)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE command_entities
      SET callback_notified = v_callback_notified
      WHERE command_id = v_command_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION InsertOrUpdateCommandEntity (v_command_id uuid,
       v_command_type int,
       v_root_command_id uuid,
       v_job_id uuid,
       v_step_id uuid,
       v_action_parameters text,
       v_action_parameters_class varchar(256),
       v_status varchar(20),
       v_callback_enabled boolean,
       v_return_value text,
       v_return_value_class varchar(256))
RETURNS VOID
   AS $procedure$
BEGIN
      IF NOT EXISTS (SELECT 1 from command_entities where command_id = v_command_id) THEN
            PERFORM InsertCommandEntity (v_command_id, v_command_type, v_root_command_id, v_job_id, v_step_id, v_action_parameters, v_action_parameters_class, v_status, v_callback_enabled, v_return_value, v_return_value_class);
      ELSE
            PERFORM UpdateCommandEntity (v_command_id, v_command_type, v_root_command_id, v_job_id, v_step_id, v_action_parameters, v_action_parameters_class, v_status, v_callback_enabled, v_return_value, v_return_value_class);
      END IF;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetCommandEntityByCommandEntityId (v_command_id uuid)
RETURNS SETOF command_entities
   AS $procedure$
BEGIN
      RETURN QUERY SELECT command_entities.*
      FROM command_entities
      WHERE command_id = v_command_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromCommandEntities ()
RETURNS SETOF command_entities
   AS $procedure$
BEGIN
      RETURN QUERY SELECT * from command_entities;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteCommandEntity(v_command_id uuid)
RETURNS VOID
   AS $procedure$
BEGIN
      BEGIN
              delete from command_entities where command_id = v_command_id;
      END;
      RETURN;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteCommandEntitiesOlderThanDate(v_date TIMESTAMP WITH TIME ZONE)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_id  INTEGER;
   SWV_RowCount INTEGER;
BEGIN
      DELETE FROM command_entities
      WHERE CREATED_AT < v_date and
      command_id NOT IN(SELECT command_id FROM async_tasks);
END; $procedure$
LANGUAGE plpgsql;
