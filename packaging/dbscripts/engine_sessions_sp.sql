----------------------------------------------------------------
-- [engine_sessions] Table
--
Create or replace FUNCTION InsertEngineSession(INOUT v_id INTEGER,
        v_engine_session_id text,
        v_user_id UUID,
        v_user_name VARCHAR(255),
        v_group_ids VARCHAR(2048),
        v_role_ids VARCHAR(2048))
RETURNS INTEGER
   AS $procedure$
BEGIN
INSERT INTO engine_sessions(engine_session_id, user_id, user_name, group_ids, role_ids)
        VALUES(v_engine_session_id, v_user_id, v_user_name, v_group_ids, v_role_ids);
v_id := CURRVAL('engine_session_seq');
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetEngineSession(v_id INTEGER) RETURNS SETOF engine_sessions STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM engine_sessions
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetEngineSessionBySessionId(v_engine_session_id text) RETURNS SETOF engine_sessions STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM engine_sessions
   WHERE engine_session_id = v_engine_session_id;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteEngineSession(v_id INTEGER)
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM engine_sessions
   WHERE id = v_id;
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteAllFromEngineSessions()
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM engine_sessions;
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;
