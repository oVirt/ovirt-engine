Create or replace FUNCTION clear_osinfo()
  RETURNS VOID
AS $procedure$
BEGIN
      TRUNCATE dwh_osinfo;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION insert_osinfo(v_os_id INTEGER , v_os_name VARCHAR(255))
RETURNS VOID
   AS $procedure$
BEGIN
      INSERT into dwh_osinfo (os_id, os_name) VALUES (v_os_id, v_os_name);
      UPDATE dwh_history_timekeeping SET var_datetime = now() where var_name = 'lastOsinfoUpdate';
END; $procedure$
LANGUAGE plpgsql;
