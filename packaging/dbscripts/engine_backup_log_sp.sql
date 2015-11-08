

-- The following SP is used by engine-backup to report engine-backup activity
-- v_scope is one of {db,dwhdb,reportsdb,files}
-- v_status can be :
-- -1 backup failed
-- 0 backup started
-- 1 backup completed successfully
CREATE OR REPLACE FUNCTION LogEngineBackupEvent (
    v_scope VARCHAR(64),
    v_done_at TIMESTAMP WITH TIME ZONE,
    v_status INT,
    v_output_message TEXT,
    v_fqdn VARCHAR(255),
    v_log_path TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    IF v_status = - 1 THEN
        INSERT INTO engine_backup_log (
            scope,
            done_at,
            is_passed,
            output_message,
            fqdn,
            log_path
            )
        VALUES (
            v_scope,
            v_done_at,
            false,
            v_output_message,
            v_fqdn,
            v_log_path
            );

    INSERT INTO audit_log (
        log_time,
        log_type_name,
        log_type,
        severity,
        message
        )
    VALUES (
        v_done_at,
        'ENGINE_BACKUP_FAILED',
        9026,
        2,
        v_output_message
        );

    ELSIF v_status = 0 THEN

    INSERT INTO audit_log (
        log_time,
        log_type_name,
        log_type,
        severity,
        message
        )
    VALUES (
        v_done_at,
        'ENGINE_BACKUP_STARTED',
        9024,
        0,
        v_output_message
        );

    ELSIF v_status = 1 THEN

    INSERT INTO engine_backup_log (
        scope,
        done_at,
        is_passed,
        output_message,
        fqdn,
        log_path
        )
    VALUES (
        v_scope,
        v_done_at,
        true,
        v_output_message,
        v_fqdn,
        v_log_path
        );

    -- Clean alerts
    PERFORM DeleteBackupRelatedAlerts();

    INSERT INTO audit_log (
        log_time,
        log_type_name,
        log_type,
        severity,
        message
        )
    VALUES (
        v_done_at,
        'ENGINE_BACKUP_COMPLETED',
        9025,
        0,
        v_output_message
        );
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLastSuccessfulEngineBackup (v_scope VARCHAR(64))
RETURNS SETOF engine_backup_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM engine_backup_log
    WHERE scope = v_scope
        AND is_passed
    ORDER BY scope,
        done_at DESC LIMIT 1;
END;$PROCEDURE$
LANGUAGE plpgsql;


