

----------------------------------------------------------------
-- [action_version_map] Table
--
CREATE OR REPLACE FUNCTION Insertaction_version_map (
    v_action_type INT,
    v_cluster_minimal_version VARCHAR(40),
    v_storage_pool_minimal_version VARCHAR(40)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO action_version_map (
        action_type,
        cluster_minimal_version,
        storage_pool_minimal_version
        )
    VALUES (
        v_action_type,
        v_cluster_minimal_version,
        v_storage_pool_minimal_version
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deleteaction_version_map (v_action_type INT)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM action_version_map
    WHERE action_type = v_action_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromaction_version_map ()
RETURNS SETOF action_version_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM action_version_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getaction_version_mapByaction_type (v_action_type INT)
RETURNS SETOF action_version_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM action_version_map
    WHERE action_type = v_action_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Deletes keys from action_version_map for the given versions
CREATE OR REPLACE FUNCTION fn_db_delete_version_map (
    v_cluster_version VARCHAR(10),
    v_sp_version VARCHAR(40)
    )
RETURNS void AS $PROCEDURE$
BEGIN
    DELETE
    FROM action_version_map
    WHERE cluster_minimal_version = v_cluster_version
        AND storage_pool_minimal_version = v_sp_version;
END;$PROCEDURE$
LANGUAGE plpgsql;


