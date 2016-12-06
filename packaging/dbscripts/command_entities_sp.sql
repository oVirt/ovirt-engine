CREATE OR REPLACE FUNCTION InsertCommandEntity (
    v_user_id uuid,
    v_engine_session_seq_id BIGINT,
    v_command_id uuid,
    v_command_type INT,
    v_parent_command_id uuid,
    v_root_command_id uuid,
    v_command_context TEXT,
    v_command_parameters TEXT,
    v_command_params_class VARCHAR(256),
    v_created_at TIMESTAMP WITH TIME ZONE,
    v_status VARCHAR(20),
    v_executed boolean,
    v_callback_enabled boolean,
    v_return_value TEXT,
    v_return_value_class VARCHAR(256),
    v_data TEXT
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    INSERT INTO command_entities (
        user_id,
        engine_session_seq_id,
        command_id,
        command_type,
        parent_command_id,
        root_command_id,
        command_context,
        command_parameters,
        command_params_class,
        created_at,
        status,
        executed,
        callback_enabled,
        return_value,
        return_value_class,
        data
        )
    VALUES (
        v_user_id,
        v_engine_session_seq_id,
        v_command_id,
        v_command_type,
        v_parent_command_id,
        v_root_command_id,
        v_command_context,
        v_command_parameters,
        v_command_params_class,
        NOW(),
        v_status,
        v_executed,
        v_callback_enabled,
        v_return_value,
        v_return_value_class,
        v_data
        );
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCommandEntity (
    v_user_id uuid,
    v_engine_session_seq_id BIGINT,
    v_command_id uuid,
    v_command_type INT,
    v_parent_command_id uuid,
    v_root_command_id uuid,
    v_command_context TEXT,
    v_command_parameters TEXT,
    v_command_params_class VARCHAR(256),
    v_status VARCHAR(20),
    v_executed boolean,
    v_callback_enabled boolean,
    v_return_value TEXT,
    v_return_value_class VARCHAR(256),
    v_data TEXT
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    UPDATE command_entities
    SET command_type = v_command_type,
        user_id = v_user_id,
        engine_session_seq_id = v_engine_session_seq_id,
        parent_command_id = v_parent_command_id,
        root_command_id = v_root_command_id,
        command_context = v_command_context,
        command_parameters = v_command_parameters,
        command_params_class = v_command_params_class,
        status = v_status,
        executed = v_executed,
        callback_enabled = v_callback_enabled,
        return_value = v_return_value,
        return_value_class = v_return_value_class,
        data = v_data
    WHERE command_id = v_command_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCommandEntityStatus (
    v_command_id uuid,
    v_status VARCHAR(20)
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    UPDATE command_entities
    SET status = v_status
    WHERE command_id = v_command_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCommandEntityExecuted (
    v_command_id uuid,
    v_executed boolean
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    UPDATE command_entities
    SET executed = v_executed
    WHERE command_id = v_command_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCommandEntityNotified (
    v_command_id uuid,
    v_callback_notified boolean
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    UPDATE command_entities
    SET callback_notified = v_callback_notified
    WHERE command_id = v_command_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertOrUpdateCommandEntity (
    v_user_id uuid,
    v_engine_session_seq_id BIGINT,
    v_command_id uuid,
    v_command_type INT,
    v_parent_command_id uuid,
    v_root_command_id uuid,
    v_command_context TEXT,
    v_command_parameters TEXT,
    v_command_params_class VARCHAR(256),
    v_created_at TIMESTAMP WITH TIME ZONE,
    v_status VARCHAR(20),
    v_executed boolean,
    v_callback_enabled boolean,
    v_return_value TEXT,
    v_return_value_class VARCHAR(256),
    v_data TEXT
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    IF NOT EXISTS (
            SELECT 1
            FROM command_entities
            WHERE command_id = v_command_id
            ) THEN
                  PERFORM InsertCommandEntity(
                      v_user_id,
                      v_engine_session_seq_id,
                      v_command_id,
                      v_command_type,
                      v_parent_command_id,
                      v_root_command_id,
                      v_command_context,
                      v_command_parameters,
                      v_command_params_class,
                      v_created_at,
                      v_status,
                      v_executed,
                      v_callback_enabled,
                      v_return_value,
                      v_return_value_class,
                      v_data);
             ELSE
                 PERFORM UpdateCommandEntity(
                     v_user_id,
                     v_engine_session_seq_id,
                     v_command_id,
                     v_command_type,
                     v_parent_command_id,
                     v_root_command_id,
                     v_command_context,
                     v_command_parameters,
                     v_command_params_class,
                     v_status,
                     v_executed,
                     v_callback_enabled,
                     v_return_value,
                     v_return_value_class,
                     v_data);
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCommandEntityByCommandEntityId (v_command_id uuid)
RETURNS SETOF command_entities AS $PROCEDURE$

BEGIN
    RETURN QUERY

    SELECT command_entities.*
    FROM command_entities
    WHERE command_id = v_command_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromCommandEntities ()
RETURNS SETOF command_entities AS $PROCEDURE$

BEGIN
    RETURN QUERY

    SELECT *
    FROM command_entities;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCommandEntitiesByParentCmdId (v_root_command_id uuid)
RETURNS SETOF command_entities STABLE AS $PROCEDURE$

BEGIN
    RETURN QUERY

    SELECT command_entities.*
    FROM command_entities
    WHERE root_command_id = v_root_command_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteCommandEntity (v_command_id uuid)
RETURNS VOID AS $PROCEDURE$

BEGIN
    BEGIN
        DELETE
        FROM command_entities
        WHERE command_id = v_command_id;
    END;

    RETURN;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteCommandEntitiesOlderThanDate (v_date TIMESTAMP WITH TIME ZONE)
RETURNS VOID AS $PROCEDURE$

DECLARE v_id INT;

SWV_RowCount INT;

BEGIN
    DELETE
    FROM command_entities
    WHERE CREATED_AT < v_date
        AND command_id NOT IN (
            SELECT command_id
            FROM async_tasks
            );
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertCommandAssociatedEntities (
    v_command_id UUID,
    v_entity_id UUID,
    v_entity_type VARCHAR(128)
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    INSERT INTO command_assoc_entities (
        command_id,
        entity_id,
        entity_type
        )
    VALUES (
        v_command_id,
        v_entity_id,
        v_entity_type
        );
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCommandIdsByEntityId (v_entity_id UUID)
RETURNS SETOF idUuidType STABLE AS $PROCEDURE$

BEGIN
    RETURN QUERY

    SELECT command_id
    FROM command_assoc_entities
    WHERE entity_id = v_entity_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCommandAssociatedEntities (v_command_id UUID)
RETURNS SETOF command_assoc_entities STABLE AS $PROCEDURE$

BEGIN
    RETURN QUERY

    SELECT *
    FROM command_assoc_entities
    WHERE command_id = v_command_id;
END;$PROCEDURE$

LANGUAGE plpgsql;


