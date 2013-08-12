-- Policy units

-- Get All policy units
Create or replace FUNCTION GetAllFromPolicyUnits() RETURNS SETOF policy_units STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   policy_units;
END; $procedure$
LANGUAGE plpgsql;

-- get policy unit by id
Create or replace FUNCTION GetPolicyUnitByPolicyUnitId(v_id UUID) RETURNS SETOF policy_units STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   policy_units
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- CRUD procs:
Create or replace FUNCTION InsertPolicyUnit(
    v_id UUID,
    v_name VARCHAR(128),
    v_description text,
    v_is_internal BOOLEAN,
    v_type SMALLINT,
    v_custom_properties_regex text,
    v_enabled BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO policy_units(
        id,
        name,
        description,
        is_internal,
        type,
        custom_properties_regex,
        enabled)
    VALUES(
        v_id,
        v_name,
        v_description,
        v_is_internal,
        v_type,
        v_custom_properties_regex,
        v_enabled);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdatePolicyUnit(
    v_id UUID,
    v_enabled BOOLEAN,
    v_custom_properties_regex text,
    v_description text)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE policy_units
    SET    custom_properties_regex = v_custom_properties_regex,
           enabled = v_enabled,
           description = v_description
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;
