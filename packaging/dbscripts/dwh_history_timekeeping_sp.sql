

----------------------------------------------------------------
-- [dwh_history_timekeeping] Table
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION UpdateDwhHistoryTimekeeping (
    v_var_name VARCHAR(50),
    v_var_value VARCHAR(255),
    v_var_datetime TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE dwh_history_timekeeping
    SET var_value = v_var_value,
        var_datetime = v_var_datetime
    WHERE var_name = v_var_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDwhHistoryTimekeepingByVarName (v_var_name VARCHAR(50))
RETURNS SETOF dwh_history_timekeeping STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM dwh_history_timekeeping
    WHERE var_name = v_var_name;
END;$PROCEDURE$
LANGUAGE plpgsql;


