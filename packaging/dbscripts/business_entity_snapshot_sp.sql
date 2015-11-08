

CREATE OR REPLACE FUNCTION insert_entity_snapshot (
    v_id uuid,
    v_command_id uuid,
    v_command_type VARCHAR,
    v_entity_id VARCHAR,
    v_entity_type VARCHAR,
    v_entity_snapshot TEXT,
    v_snapshot_class VARCHAR,
    v_snapshot_type INT,
    v_insertion_order INT
    )
RETURNS void AS $PROCEDURE$
BEGIN
    BEGIN
        INSERT INTO business_entity_snapshot (
            id,
            command_id,
            command_type,
            entity_id,
            entity_type,
            entity_snapshot,
            snapshot_class,
            snapshot_type,
            insertion_order
            )
        VALUES (
            v_id,
            v_command_id,
            v_command_type,
            v_entity_id,
            v_entity_type,
            v_entity_snapshot,
            v_snapshot_class,
            v_snapshot_type,
            v_insertion_order
            );
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_entity_snapshot_by_id (v_id uuid)
RETURNS SETOF business_entity_snapshot STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT business_entity_snapshot.*
    FROM business_entity_snapshot
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_entity_snapshot_by_command_id (v_command_id uuid)
RETURNS SETOF business_entity_snapshot STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT business_entity_snapshot.*
    FROM business_entity_snapshot
    WHERE command_id = v_command_id
    ORDER BY insertion_order DESC;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS get_all_commands_rs CASCADE;
CREATE TYPE get_all_commands_rs AS (
        command_id UUID,
        command_type VARCHAR(256)
        );

CREATE OR REPLACE FUNCTION get_all_commands ()
RETURNS SETOF get_all_commands_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT business_entity_snapshot.command_id,
        business_entity_snapshot.command_type
    FROM business_entity_snapshot;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_entity_snapshot_by_command_id (v_command_id uuid)
RETURNS void AS $PROCEDURE$
BEGIN
    BEGIN
        DELETE
        FROM business_entity_snapshot
        WHERE command_id = v_command_id;
    END;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;


