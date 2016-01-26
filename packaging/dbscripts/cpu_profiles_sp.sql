

----------------------------------------------------------------------
--  Cpu Profiles
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetCpuProfileByCpuProfileId (v_id UUID)
RETURNS SETOF cpu_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cpu_profiles
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertCpuProfile (
    v_id UUID,
    v_name VARCHAR(50),
    v_cluster_id UUID,
    v_qos_id UUID,
    v_description TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO cpu_profiles (
        id,
        name,
        cluster_id,
        qos_id,
        description
        )
    VALUES (
        v_id,
        v_name,
        v_cluster_id,
        v_qos_id,
        v_description
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCpuProfile (
    v_id UUID,
    v_name VARCHAR(50),
    v_cluster_id UUID,
    v_qos_id UUID,
    v_description TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE cpu_profiles
    SET id = v_id,
        name = v_name,
        cluster_id = v_cluster_id,
        qos_id = v_qos_id,
        description = v_description,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteCpuProfile (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM cpu_profiles
    WHERE id = v_id;

    -- Delete the cpu profiles permissions
    DELETE
    FROM permissions
    WHERE object_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromCpuProfiles ()
RETURNS SETOF cpu_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cpu_profiles;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCpuProfilesByClusterId (
    v_cluster_id UUID,
    v_user_id UUID,
    v_is_filtered boolean,
    v_action_group_id INTEGER
    )
RETURNS SETOF cpu_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cpu_profiles
    WHERE cluster_id = v_cluster_id
        AND (
            NOT v_is_filtered
            OR (
                EXISTS (
                SELECT 1
                FROM user_cpu_profile_permissions_view
                NATURAL JOIN roles_groups
                WHERE user_id IN (v_user_id, getGlobalIds('everyone'))
                    AND entity_id = cpu_profiles.id
                    AND action_group_id = v_action_group_id
                )
            )
        )
    ORDER BY _create_date;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCpuProfilesByQosId (v_qos_id UUID)
RETURNS SETOF cpu_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cpu_profiles
    WHERE qos_id = v_qos_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


