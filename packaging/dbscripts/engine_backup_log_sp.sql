CREATE OR REPLACE FUNCTION InsertEngineBackupHistory(v_db_name VARCHAR(64),
     v_done_at TIMESTAMP WITH TIME ZONE ,
     v_is_passed BOOLEAN,
     v_output_message TEXT)

RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO engine_backup_log(db_name, done_at,is_passed, output_message)
       VALUES(v_db_name, v_done_at, v_is_passed, v_output_message);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLastSuccessfulEngineBackup(v_db_name VARCHAR(64))
RETURNS SETOF engine_backup_log STABLE
   AS $procedure$
BEGIN
     RETURN QUERY SELECT *
     FROM engine_backup_log
     WHERE db_name = v_db_name and is_passed
     ORDER BY db_name, done_at DESC
     LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

-- The following SP is used by engine-backup to report engine-backup activity
-- v_status can be :
-- -1 backup failed
-- 0 backup started
-- 1 backup completed successfully

CREATE OR REPLACE FUNCTION LogEngineBackupEvent(v_db_name VARCHAR(64),
     v_done_at TIMESTAMP WITH TIME ZONE ,
     v_status INTEGER,
     v_output_message TEXT)
RETURNS VOID
     AS $procedure$
BEGIN
    IF v_status = -1 THEN
        PERFORM InsertEngineBackupHistory(v_db_name, v_done_at, false, v_output_message);

        INSERT INTO audit_log(log_time, log_type_name, log_type, severity, message)
        VALUES(v_done_at, 'ENGINE_BACKUP_FAILED', 9026, 2, v_output_message);

    ELSIF v_status = 0 THEN
        INSERT INTO audit_log(log_time, log_type_name, log_type, severity, message)
        VALUES(v_done_at, 'ENGINE_BACKUP_STARTED', 9024, 0, v_output_message);
    ELSIF v_status = 1 THEN
        PERFORM InsertEngineBackupHistory(v_db_name, v_done_at, true, v_output_message);

        -- Clean alerts
        PERFORM DeleteBackupRelatedAlerts();

        INSERT INTO audit_log(log_time, log_type_name, log_type, severity, message)
        VALUES(v_done_at, 'ENGINE_BACKUP_COMPLETED', 9025, 0, v_output_message);
    END IF;
END; $procedure$
LANGUAGE plpgsql;

