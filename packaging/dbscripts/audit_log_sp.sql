

----------------------------------------------------------------
-- [audit_log] Table
--
CREATE OR REPLACE FUNCTION InsertAuditLog (
    INOUT v_audit_log_id BIGINT,
    v_log_time TIMESTAMP WITH TIME ZONE,
    v_log_type INT,
    v_log_type_name VARCHAR(100),
    v_severity INT,
    v_message TEXT,
    v_user_id UUID,
    v_user_name VARCHAR(255),
    v_vds_id UUID,
    v_vds_name VARCHAR(255),
    v_vm_id UUID,
    v_vm_name VARCHAR(255),
    v_vm_template_id UUID,
    v_vm_template_name VARCHAR(40),
    v_storage_pool_id UUID,
    v_storage_pool_name VARCHAR(40),
    v_storage_domain_id UUID,
    v_storage_domain_name VARCHAR(250),
    v_cluster_id UUID,
    v_cluster_name VARCHAR(255),
    v_quota_id UUID,
    v_quota_name VARCHAR(60),
    v_correlation_id VARCHAR(50),
    v_job_id UUID,
    v_gluster_volume_id UUID,
    v_gluster_volume_name VARCHAR(1000),
    v_call_stack TEXT,
    v_repeatable BOOLEAN,
    v_brick_id UUID,
    v_brick_path TEXT,
    v_origin VARCHAR(25),
    v_custom_event_id INT,
    v_event_flood_in_sec INT,
    v_custom_data TEXT
    ) AS $PROCEDURE$
DECLARE v_min_alert_severity INT;

BEGIN
    v_min_alert_severity := 10;

    -- insert regular log messages (non alerts)
    IF (v_severity < v_min_alert_severity) THEN
        INSERT INTO audit_log (
            LOG_TIME,
            log_type,
            log_type_name,
            severity,
            message,
            user_id,
            user_name,
            vds_id,
            vds_name,
            vm_id,
            vm_name,
            vm_template_id,
            vm_template_name,
            storage_pool_id,
            storage_pool_name,
            storage_domain_id,
            storage_domain_name,
            cluster_id,
            cluster_name,
            correlation_id,
            job_id,
            quota_id,
            quota_name,
            gluster_volume_id,
            gluster_volume_name,
            call_stack,
            brick_id,
            brick_path,
            origin,
            custom_event_id,
            event_flood_in_sec,
            custom_data
            )
        VALUES (
            v_log_time,
            v_log_type,
            v_log_type_name,
            v_severity,
            v_message,
            v_user_id,
            v_user_name,
            v_vds_id,
            v_vds_name,
            v_vm_id,
            v_vm_name,
            v_vm_template_id,
            v_vm_template_name,
            v_storage_pool_id,
            v_storage_pool_name,
            v_storage_domain_id,
            v_storage_domain_name,
            v_cluster_id,
            v_cluster_name,
            v_correlation_id,
            v_job_id,
            v_quota_id,
            v_quota_name,
            v_gluster_volume_id,
            v_gluster_volume_name,
            v_call_stack,
            v_brick_id,
            v_brick_path,
            v_origin,
            v_custom_event_id,
            v_event_flood_in_sec,
            v_custom_data
            );

        v_audit_log_id := CURRVAL('audit_log_seq');

        ELSE IF (
            v_repeatable
            OR NOT EXISTS (
                SELECT audit_log_id
                FROM audit_log
                WHERE vds_name = v_vds_name
                    AND log_type = v_log_type
                    AND NOT deleted
                )
            ) THEN
        INSERT INTO audit_log (
            LOG_TIME,
            log_type,
            log_type_name,
            severity,
            message,
            user_id,
            user_name,
            vds_id,
            vds_name,
            vm_id,
            vm_name,
            vm_template_id,
            vm_template_name,
            storage_pool_id,
            storage_pool_name,
            storage_domain_id,
            storage_domain_name,
            cluster_id,
            cluster_name,
            correlation_id,
            job_id,
            quota_id,
            quota_name,
            gluster_volume_id,
            gluster_volume_name,
            call_stack,
            brick_id,
            brick_path,
            origin,
            custom_event_id,
            event_flood_in_sec,
            custom_data
            )
        VALUES (
            v_log_time,
            v_log_type,
            v_log_type_name,
            v_severity,
            v_message,
            v_user_id,
            v_user_name,
            v_vds_id,
            v_vds_name,
            v_vm_id,
            v_vm_name,
            v_vm_template_id,
            v_vm_template_name,
            v_storage_pool_id,
            v_storage_pool_name,
            v_storage_domain_id,
            v_storage_domain_name,
            v_cluster_id,
            v_cluster_name,
            v_correlation_id,
            v_job_id,
            v_quota_id,
            v_quota_name,
            v_gluster_volume_id,
            v_gluster_volume_name,
            v_call_stack,
            v_brick_id,
            v_brick_path,
            v_origin,
            v_custom_event_id,
            v_event_flood_in_sec,
            v_custom_data
            );

            v_audit_log_id := CURRVAL('audit_log_seq');
         ELSE

            SELECT audit_log_id
            INTO v_audit_log_id
            FROM audit_log
            WHERE vds_name = v_vds_name
            AND log_type = v_log_type;
        END IF;
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAuditLog (v_audit_log_id BIGINT)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE audit_log
    SET deleted = true
    WHERE audit_log_id = v_audit_log_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ClearAllAuditLogEvents (v_severity INT)
RETURNS VOID AS $PROCEDURE$
BEGIN

    UPDATE audit_log
    SET deleted = true
    FROM ( SELECT * FROM audit_log
           WHERE severity != v_severity
               AND NOT deleted
           FOR UPDATE) AS s
    WHERE audit_log.audit_log_id = s.audit_log_id;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DisplayAllAuditLogEvents (v_severity INT)
RETURNS VOID AS $PROCEDURE$
BEGIN

    UPDATE audit_log
    SET deleted = false
    FROM ( SELECT * FROM audit_log
           WHERE severity != v_severity
               AND deleted
           FOR UPDATE) AS s
    WHERE audit_log.audit_log_id = s.audit_log_id;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SetAllAuditLogAlerts (
    v_severity INT,
    v_value BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN


    UPDATE audit_log
    SET deleted = v_value
    FROM ( SELECT * FROM audit_log
           WHERE severity = v_severity
               AND deleted != v_value
           FOR UPDATE) AS s
    WHERE audit_log.audit_log_id = s.audit_log_id;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns the events for which the user has direct permissions on
-- If the user has permissions only on a VM, the user will see only events for this VM
-- If the user has permissions on a cluster, he will see events from the cluster, the hosts and the VMS in the cluster
-- because each event has the cluster id of the entity that generates the event and we check to see if the user has
-- permissions on the cluster using the column cluster_id. Same holds true for data center
CREATE OR REPLACE FUNCTION GetAllFromAuditLog (
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF audit_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM audit_log a
    WHERE NOT deleted
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view pv,
                    user_object_permissions_view dpv
                WHERE pv.user_id = v_user_id
                    AND pv.entity_id = a.vm_id
                    AND pv.entity_id = dpv.entity_id
                )
            OR EXISTS (
                SELECT 1
                FROM user_vm_template_permissions_view pv,
                    user_object_permissions_view dpv
                WHERE pv.user_id = v_user_id
                    AND pv.entity_id = a.vm_template_id
                    AND pv.entity_id = dpv.entity_id
                )
            OR EXISTS (
                SELECT 1
                FROM user_vds_permissions_view pv,
                    user_object_permissions_view dpv
                WHERE pv.user_id = v_user_id
                    AND pv.entity_id = a.vds_id
                    AND pv.entity_id = dpv.entity_id
                )
            OR EXISTS (
                SELECT 1
                FROM user_storage_pool_permissions_view pv,
                    user_object_permissions_view dpv
                WHERE pv.user_id = v_user_id
                    AND pv.entity_id = a.storage_pool_id
                    AND pv.entity_id = dpv.entity_id
                )
            OR EXISTS (
                SELECT 1
                FROM user_storage_domain_permissions_view pv,
                    user_object_permissions_view dpv
                WHERE pv.user_id = v_user_id
                    AND pv.entity_id = a.storage_domain_id
                    AND pv.entity_id = dpv.entity_id
                )
            OR EXISTS (
                SELECT 1
                FROM user_cluster_permissions_view pv,
                    user_object_permissions_view dpv
                WHERE pv.user_id = v_user_id
                    AND pv.entity_id = a.cluster_id
                    AND pv.entity_id = dpv.entity_id
                )
            )
    ORDER BY audit_log_id DESC;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAuditLogByAuditLogId (v_audit_log_id BIGINT)
RETURNS SETOF audit_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM audit_log
    WHERE audit_log_id = v_audit_log_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAuditLogByVMId (
    v_vm_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF audit_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM audit_log
    WHERE NOT deleted
        AND vm_id = v_vm_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vm_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAuditLogByVMTemplateId (
    v_vm_template_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF audit_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM audit_log
    WHERE NOT deleted
        AND vm_template_id = v_vm_template_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_template_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vm_template_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveAuditLogByBrickIdLogType (
    v_brick_id UUID,
    v_audit_log_type INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE audit_log
    SET deleted = true
    WHERE brick_id = v_brick_id
        AND log_type = v_audit_log_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAuditLogLaterThenDate (v_date TIMESTAMP WITH TIME ZONE)
RETURNS SETOF audit_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM audit_log
    WHERE NOT deleted
        AND LOG_TIME >= v_date;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAuditLogOlderThenDate (v_date TIMESTAMP WITH TIME ZONE)
RETURNS VOID AS $PROCEDURE$
DECLARE v_id BIGINT;

SWV_RowCount INT;

BEGIN
    -- get first the id from which to remove in order to use index
    SELECT audit_log_id
    INTO v_id
    FROM audit_log
    WHERE LOG_TIME < v_date
    ORDER BY audit_log_id DESC LIMIT 1;

    -- check if there are candidates to remove
    GET DIAGNOSTICS SWV_RowCount = ROW_COUNT;

    IF (SWV_RowCount > 0) THEN
        DELETE
        FROM audit_log
        WHERE audit_log_id <= v_id;
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAuditAlertLogByVdsIDAndType (
    v_vds_id UUID,
    v_log_type INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE audit_log
    SET deleted = true
    WHERE vds_id = v_vds_id
        AND log_type = v_log_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteBackupRelatedAlerts ()
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE audit_log
    SET deleted = true
    WHERE origin = 'oVirt'
        AND log_type IN (
            9022,
            9023,
            9026
            );-- (ENGINE_NO_FULL_BACKUP, ENGINE_NO_WARM_BACKUP, ENGINE_BACKUP_FAILED)
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAuditAlertLogByVolumeIDAndType (
    v_gluster_volume_id UUID,
    v_log_type INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE audit_log
    SET deleted = true
    WHERE gluster_volume_id = v_gluster_volume_id
        AND log_type = v_log_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAuditLogAlertsByVdsID (
    v_vds_id UUID,
    v_delete_config_alerts BOOLEAN = true
    )
RETURNS VOID AS $PROCEDURE$
DECLARE v_min_alert_severity INT;

v_no_config_alret_type INT;

v_no_max_alret_type INT;

BEGIN
    v_min_alert_severity := 10;

    v_no_config_alret_type := 9000;

    v_no_max_alret_type := 9005;

    IF (v_delete_config_alerts = true) THEN
        UPDATE audit_log
        SET deleted = true
        WHERE vds_id = v_vds_id
            AND severity >= v_min_alert_severity
            AND log_type BETWEEN v_no_config_alret_type
                AND v_no_max_alret_type;
    ELSE
        UPDATE audit_log
        SET deleted = true
        WHERE vds_id = v_vds_id
            AND severity >= v_min_alert_severity
            AND log_type BETWEEN v_no_config_alret_type + 1
                AND v_no_max_alret_type;
END

IF ;END;$PROCEDURE$
    LANGUAGE plpgsql;

/*
Used to find out how many seconds to wait after Start/Stop/Restart PM operations
v_vds_name     - The host name
v_event        - The event [USER_VDS_STOP | USER_VDS_START | USER_VDS_RESTART]
v_wait_for_sec - Configurable time in seconds to wait from last operation.
Returns : The number of seconds we have to wait (negative value means we can do the operation immediately)
*/
CREATE OR REPLACE FUNCTION get_seconds_to_wait_before_pm_operation (
    v_vds_name VARCHAR(255),
    v_event VARCHAR(100),
    v_wait_for_sec INT
    )
RETURNS INT STABLE AS $PROCEDURE$
DECLARE v_last_event_dt TIMESTAMP
WITH TIME zone;
DECLARE v_now_dt TIMESTAMP
WITH TIME zone;

BEGIN
    IF EXISTS (
            SELECT 1
            FROM audit_log
            WHERE vds_name = v_vds_name
                AND log_type_name = v_event
            ) THEN
    BEGIN
        v_last_event_dt := log_time
        FROM audit_log
        WHERE vds_name = v_vds_name
            AND log_type_name = v_event
        ORDER BY audit_log_id DESC limit 1;

        v_now_dt := CURRENT_TIMESTAMP;

        RETURN cast((extract(epoch FROM v_last_event_dt) + v_wait_for_sec) - extract(epoch FROM v_now_dt) AS INT);
    END;
    ELSE
        RETURN 0;
END

IF ;END;$PROCEDURE$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAuditLogByOriginAndCustomEventId (
    v_origin VARCHAR(255),
    v_custom_event_id INT
    )
RETURNS SETOF audit_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM audit_log
    WHERE origin = v_origin
        AND custom_event_id = v_custom_event_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAuditLogByVolumeIdAndType (
    v_gluster_volume_id UUID,
    v_log_type INT
    )
RETURNS SETOF audit_log STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM audit_log
    WHERE gluster_volume_id = v_gluster_volume_id
        AND log_type = v_log_type;
END;$PROCEDURE$
LANGUAGE plpgsql;


