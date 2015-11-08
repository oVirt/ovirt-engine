

-- Policy units
-- Get All policy units
CREATE OR REPLACE FUNCTION GetAllFromPolicyUnits ()
RETURNS SETOF policy_units STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM policy_units;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- get policy unit by id
CREATE OR REPLACE FUNCTION GetPolicyUnitByPolicyUnitId (v_id UUID)
RETURNS SETOF policy_units STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM policy_units
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- CRUD procs:
CREATE OR REPLACE FUNCTION InsertPolicyUnit (
    v_id UUID,
    v_name VARCHAR(128),
    v_description TEXT,
    v_is_internal BOOLEAN,
    v_type SMALLINT,
    v_custom_properties_regex TEXT,
    v_enabled BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO policy_units (
        id,
        name,
        description,
        is_internal,
        type,
        custom_properties_regex,
        enabled
        )
    VALUES (
        v_id,
        v_name,
        v_description,
        v_is_internal,
        v_type,
        v_custom_properties_regex,
        v_enabled
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdatePolicyUnit (
    v_id UUID,
    v_enabled BOOLEAN,
    v_custom_properties_regex TEXT,
    v_description TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE policy_units
    SET custom_properties_regex = v_custom_properties_regex,
        enabled = v_enabled,
        description = v_description
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeletePolicyUnit (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM policy_units
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


