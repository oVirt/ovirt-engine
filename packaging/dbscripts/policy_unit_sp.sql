-- Policy units

-- Get All policy units
Create or replace FUNCTION GetAllFromPolicyUnits() RETURNS SETOF policy_units
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   policy_units;
END; $procedure$
LANGUAGE plpgsql;

-- get policy unit by id
Create or replace FUNCTION GetPolicyUnitByPolicyUnitId(v_id UUID) RETURNS SETOF policy_units
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   policy_units
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;
