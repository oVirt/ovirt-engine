

CREATE OR REPLACE FUNCTION clear_osinfo ()
RETURNS VOID AS $FUNCTION$
BEGIN
    TRUNCATE dwh_osinfo;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_osinfo (
    v_os_id INT,
    v_os_name VARCHAR(255)
    )
RETURNS VOID AS $FUNCTION$
BEGIN
    INSERT INTO dwh_osinfo (
        os_id,
        os_name
        )
    VALUES (
        v_os_id,
        v_os_name
        );

    UPDATE dwh_history_timekeeping
    SET var_datetime = now()
    WHERE var_name = 'lastOsinfoUpdate';
END;$FUNCTION$
LANGUAGE plpgsql;


