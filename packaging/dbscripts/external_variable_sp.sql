--
-- Table external_variable
--

CREATE OR REPLACE FUNCTION InsertExternalVariable(
        v_var_name VARCHAR(100),
        v_var_value VARCHAR(4000))
RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO external_variable(
            var_name,
            var_value
        ) VALUES (
            v_var_name,
            v_var_value
        );
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION UpdateExternalVariable(
        v_var_name VARCHAR(100),
        v_var_value VARCHAR(4000))
RETURNS BOOLEAN
    AS $procedure$
BEGIN
    UPDATE external_variable
        SET
            var_value = v_var_value,
            _update_date = LOCALTIMESTAMP
        WHERE var_name = v_var_name;
    RETURN found;
END; $procedure$
LANGUAGE plpgsql;


-- UpsertExternalVariable is used in fence_kdump listener
CREATE OR REPLACE FUNCTION UpsertExternalVariable(
        v_var_name VARCHAR(100),
        v_var_value VARCHAR(4000))
RETURNS VOID
    AS $procedure$
DECLARE
    record_found BOOLEAN;
BEGIN
    SELECT UpdateExternalVariable(
        v_var_name,
        v_var_value
    ) INTO record_found;

    IF NOT record_found THEN
        PERFORM InsertExternalVariable(
                v_var_name,
                v_var_value
        );
    END IF;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION DeleteExternalVariable(
        v_var_name VARCHAR(100))
RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM external_variable
        WHERE var_name = v_var_name;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetExternalVariableByName(
        v_var_name VARCHAR(100))
RETURNS SETOF external_variable STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT external_variable.*
        FROM external_variable
        WHERE var_name = v_var_name;
END; $procedure$
LANGUAGE plpgsql;
