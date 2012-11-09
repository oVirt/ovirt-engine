CREATE OR REPLACE FUNCTION insert_entity_snapshot(v_id uuid, v_command_id uuid,v_command_type character varying , v_entity_id character varying, v_entity_type character varying, v_entity_snapshot text, v_snapshot_class character varying, v_snapshot_type INTEGER,v_insertion_order INTEGER)
  RETURNS void AS
$procedure$
BEGIN
	BEGIN
		INSERT INTO business_entity_snapshot(id, command_id, command_type, entity_id,entity_type,entity_snapshot, snapshot_class, snapshot_type,insertion_order)
				VALUES(v_id, v_command_id, v_command_type, v_entity_id,v_entity_type,v_entity_snapshot, v_snapshot_class, v_snapshot_type,v_insertion_order);
	END;

   RETURN;
END; $procedure$
  LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_entity_snapshot_by_id(v_id uuid)
  RETURNS SETOF business_entity_snapshot AS
$procedure$
BEGIN
      RETURN QUERY SELECT business_entity_snapshot.*
      FROM business_entity_snapshot
      WHERE id = v_id;
END; $procedure$
  LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_entity_snapshot_by_command_id(v_command_id uuid)
  RETURNS SETOF business_entity_snapshot AS
$procedure$
BEGIN
      RETURN QUERY SELECT business_entity_snapshot.*
      FROM business_entity_snapshot
      WHERE command_id = v_command_id order by insertion_order desc;
END; $procedure$
  LANGUAGE plpgsql;



DROP TYPE IF EXISTS get_all_commands_rs CASCADE;
CREATE TYPE get_all_commands_rs  AS(command_id UUID, command_type varchar(256));

CREATE OR REPLACE FUNCTION get_all_commands()
  RETURNS SETOF get_all_commands_rs AS
$procedure$
BEGIN
      RETURN QUERY SELECT distinct business_entity_snapshot.command_id, business_entity_snapshot.command_type
      FROM business_entity_snapshot;

END; $procedure$
  LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION delete_entity_snapshot_by_command_id(v_command_id uuid)
  RETURNS void AS
$procedure$
BEGIN
	BEGIN
		delete from business_entity_snapshot where command_id = v_command_id;
	END;
	RETURN;
END; $procedure$
  LANGUAGE plpgsql;
