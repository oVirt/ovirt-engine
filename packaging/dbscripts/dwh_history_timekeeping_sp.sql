----------------------------------------------------------------
-- [dwh_history_timekeeping] Table
----------------------------------------------------------------
Create or replace FUNCTION UpdateDwhHistoryTimekeeping(v_var_name VARCHAR(50),
    v_var_value VARCHAR(255),
    v_var_datetime TIMESTAMP WITH TIME ZONE)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE dwh_history_timekeeping
      SET var_value = v_var_value,
          var_datetime = v_var_datetime
      WHERE var_name = v_var_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetDwhHistoryTimekeepingByVarName(v_var_name VARCHAR(50))
RETURNS SETOF dwh_history_timekeeping STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM dwh_history_timekeeping
   WHERE var_name = v_var_name;
END; $procedure$
LANGUAGE plpgsql;
