

CREATE OR REPLACE FUNCTION clear_osinfo ()
RETURNS VOID AS $PROCEDURE$
BEGIN
    TRUNCATE dwh_osinfo;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_osinfo (
    v_os_id INT,
    v_os_name VARCHAR(255)
    )
RETURNS VOID AS $PROCEDURE$
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
END;$PROCEDURE$
LANGUAGE plpgsql;


