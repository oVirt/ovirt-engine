----------------------------------------------------------------
-- [audit_log] Table
--

Create or replace FUNCTION InsertAuditLog(INOUT v_audit_log_id INTEGER ,
 v_log_time TIMESTAMP WITH TIME ZONE,
 v_log_type INTEGER,
    v_log_type_name VARCHAR(100),
 v_severity INTEGER,
 v_message text,
 v_user_id UUID ,
 v_user_name VARCHAR(255) ,
 v_vds_id UUID ,
 v_vds_name VARCHAR(255) ,
 v_vm_id UUID ,
 v_vm_name VARCHAR(255) ,
 v_vm_template_id UUID ,
    v_vm_template_name VARCHAR(40) ,
    v_storage_pool_id UUID ,
    v_storage_pool_name VARCHAR(40) ,
    v_storage_domain_id UUID ,
    v_storage_domain_name VARCHAR(250) ,
    v_vds_group_id UUID ,
    v_vds_group_name VARCHAR(255),
    v_quota_id UUID,
    v_quota_name VARCHAR(60),
    v_correlation_id VARCHAR(50),
    v_job_id UUID,
    v_gluster_volume_id UUID,
    v_gluster_volume_name VARCHAR(1000),
    v_call_stack text,
    v_repeatable BOOLEAN)
   AS $procedure$
   DECLARE
   v_min_alret_severity  INTEGER;
BEGIN
      v_min_alret_severity := 10;
	-- insert regular log messages (non alerts)
      if (v_severity < v_min_alret_severity) then

INSERT INTO audit_log(LOG_TIME, log_type, log_type_name, severity,message, user_id, USER_NAME, vds_id, VDS_NAME, vm_id, VM_NAME,vm_template_id,VM_TEMPLATE_NAME,storage_pool_id,STORAGE_POOL_NAME,storage_domain_id,STORAGE_DOMAIN_NAME,vds_group_id,vds_group_name, correlation_id, job_id, quota_id, quota_name, gluster_volume_id, gluster_volume_name, call_stack)
		VALUES(v_log_time, v_log_type, v_log_type_name, v_severity, v_message, v_user_id, v_user_name, v_vds_id, v_vds_name, v_vm_id, v_vm_name,v_vm_template_id,v_vm_template_name,v_storage_pool_id,v_storage_pool_name,v_storage_domain_id,v_storage_domain_name,v_vds_group_id,v_vds_group_name, v_correlation_id, v_job_id, v_quota_id, v_quota_name, v_gluster_volume_id, v_gluster_volume_name, v_call_stack);

         v_audit_log_id := CURRVAL('audit_log_seq');
      else
         if (v_repeatable OR not exists(select audit_log_id from audit_log where vds_name = v_vds_name and log_type = v_log_type and not deleted)) then

INSERT INTO audit_log(LOG_TIME, log_type, log_type_name, severity,message, user_id, USER_NAME, vds_id, VDS_NAME, vm_id, VM_NAME,vm_template_id,VM_TEMPLATE_NAME,storage_pool_id,STORAGE_POOL_NAME,storage_domain_id,STORAGE_DOMAIN_NAME,vds_group_id,vds_group_name, correlation_id, job_id, quota_id, quota_name, gluster_volume_id, gluster_volume_name, call_stack)
			VALUES(v_log_time, v_log_type, v_log_type_name, v_severity, v_message, v_user_id, v_user_name, v_vds_id, v_vds_name, v_vm_id, v_vm_name,v_vm_template_id,v_vm_template_name,v_storage_pool_id,v_storage_pool_name,v_storage_domain_id,v_storage_domain_name,v_vds_group_id,v_vds_group_name, v_correlation_id, v_job_id, v_quota_id, v_quota_name, v_gluster_volume_id, v_gluster_volume_name, v_call_stack);

            v_audit_log_id := CURRVAL('audit_log_seq');
         else
            select   audit_log_id INTO v_audit_log_id from audit_log where vds_name = v_vds_name and log_type = v_log_type;
         end if;
      end if;
END; $procedure$
LANGUAGE plpgsql;

-- External Event/Alert
Create or replace FUNCTION InsertExternalAuditLog(INOUT v_audit_log_id INTEGER ,
    v_log_time TIMESTAMP WITH TIME ZONE,
    v_log_type INTEGER,
    v_log_type_name VARCHAR(100),
    v_severity INTEGER,
    v_message text,
    v_user_id UUID ,
    v_user_name VARCHAR(255) ,
    v_vds_id UUID ,
    v_vds_name VARCHAR(255) ,
    v_vm_id UUID ,
    v_vm_name VARCHAR(255) ,
    v_vm_template_id UUID ,
    v_vm_template_name VARCHAR(40) ,
    v_storage_pool_id UUID ,
    v_storage_pool_name VARCHAR(40) ,
    v_storage_domain_id UUID ,
    v_storage_domain_name VARCHAR(250) ,
    v_vds_group_id UUID ,
    v_vds_group_name VARCHAR(255),
    v_quota_id UUID,
    v_quota_name VARCHAR(60),
    v_correlation_id VARCHAR(50),
    v_job_id UUID,
    v_gluster_volume_id UUID,
    v_gluster_volume_name VARCHAR(1000),
    v_call_stack text,
    v_origin VARCHAR(25),
    v_custom_event_id INTEGER,
    v_event_flood_in_sec INTEGER,
    v_custom_data text)
AS $procedure$
DECLARE
    v_max_message_length INTEGER;
    v_truncated_message text;
BEGIN

   -- truncate message if exceeds configured max length. truncated messages will be ended
   -- with "..." to indicate that message is incomplete due to size limits.

   v_truncated_message := v_message;
   v_max_message_length := cast(option_value as int) FROM vdc_options WHERE option_name = 'MaxAuditLogMessageLength' and version = 'general';
   IF (v_max_message_length IS NOT NULL and length(v_message) > v_max_message_length) THEN
      v_truncated_message := substr(v_message, 1, v_max_message_length -3) || '...';
   END IF;
   INSERT INTO audit_log(LOG_TIME, log_type, log_type_name, severity,message, user_id, USER_NAME, vds_id, VDS_NAME, vm_id, VM_NAME,vm_template_id,VM_TEMPLATE_NAME,storage_pool_id,STORAGE_POOL_NAME,storage_domain_id,STORAGE_DOMAIN_NAME,vds_group_id,vds_group_name, correlation_id, job_id, quota_id, quota_name, gluster_volume_id, gluster_volume_name, call_stack, origin, custom_event_id, event_flood_in_sec, custom_data )
		VALUES(v_log_time, v_log_type, v_log_type_name, v_severity, v_truncated_message, v_user_id, v_user_name, v_vds_id, v_vds_name, v_vm_id, v_vm_name,v_vm_template_id,v_vm_template_name,v_storage_pool_id,v_storage_pool_name,v_storage_domain_id,v_storage_domain_name,v_vds_group_id,v_vds_group_name, v_correlation_id, v_job_id, v_quota_id, v_quota_name, v_gluster_volume_id, v_gluster_volume_name, v_call_stack, v_origin, v_custom_event_id, v_event_flood_in_sec, v_custom_data);

   v_audit_log_id := CURRVAL('audit_log_seq');
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAuditLog(v_audit_log_id INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE audit_log SET deleted = true
      WHERE audit_log_id = v_audit_log_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION ClearAllDismissedAuditLogs()
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE audit_log SET deleted = false;
END; $procedure$
LANGUAGE plpgsql;

-- Returns the events for which the user has direct permissions on
-- If the user has permissions only on a VM, the user will see only events for this VM
-- If the user has permissions on a cluster, he will see events from the cluster, the hosts and the VMS in the cluster
-- because each event has the cluster id of the entity that generates the event and we check to see if the user has
-- permissions on the cluster using the column vds_group_id. Same holds true for data center
Create or replace FUNCTION GetAllFromAuditLog(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF audit_log STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM audit_log a
      WHERE NOT deleted AND
     (NOT v_is_filtered OR EXISTS (SELECT 1
                                   FROM   user_vm_permissions_view pv, user_object_permissions_view dpv
                                   WHERE  pv.user_id = v_user_id AND pv.entity_id = a.vm_id AND pv.entity_id = dpv.entity_id)
                        OR EXISTS (SELECT 1
                                   FROM user_vm_template_permissions_view pv, user_object_permissions_view dpv
                                   WHERE pv.user_id = v_user_id AND pv.entity_id = a.vm_template_id AND pv.entity_id = dpv.entity_id)
                        OR EXISTS (SELECT 1
                                   FROM user_vds_permissions_view pv, user_object_permissions_view dpv
                                   WHERE pv.user_id = v_user_id AND pv.entity_id = a.vds_id AND pv.entity_id = dpv.entity_id)
                        OR EXISTS (SELECT 1
                                   FROM user_storage_pool_permissions_view pv, user_object_permissions_view dpv
                                   WHERE pv.user_id = v_user_id AND pv.entity_id = a.storage_pool_id AND pv.entity_id = dpv.entity_id)
                        OR EXISTS (SELECT 1
                                   FROM user_storage_domain_permissions_view pv, user_object_permissions_view dpv
                                   WHERE pv.user_id = v_user_id AND pv.entity_id = a.storage_domain_id AND pv.entity_id = dpv.entity_id)
                        OR EXISTS (SELECT 1
                                   FROM user_vds_groups_permissions_view pv, user_object_permissions_view dpv
                                   WHERE pv.user_id = v_user_id AND pv.entity_id = a.vds_group_id AND pv.entity_id = dpv.entity_id)
     );
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAuditLogByAuditLogId(v_audit_log_id INTEGER) RETURNS SETOF audit_log STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM audit_log
      WHERE audit_log_id = v_audit_log_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAuditLogByVMId(v_vm_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF audit_log STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM   audit_log
      WHERE  not deleted and vm_id = v_vm_id
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_vm_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = vm_id));


END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAuditLogByVMTemplateId(v_vm_template_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF audit_log STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM   audit_log
      WHERE  not deleted and vm_template_id = v_vm_template_id
      AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_vm_template_permissions_view
                                        WHERE  user_id = v_user_id AND entity_id = vm_template_id));


END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAuditLogLaterThenDate(v_date TIMESTAMP WITH TIME ZONE)
RETURNS SETOF audit_log STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM audit_log
      WHERE not deleted and LOG_TIME >= v_date;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAuditLogOlderThenDate(v_date TIMESTAMP WITH TIME ZONE)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_id  INTEGER;
   SWV_RowCount INTEGER;
BEGIN
        -- get first the id from which to remove in order to use index
      select   audit_log_id INTO v_id FROM audit_log WHERE LOG_TIME < v_date   order by audit_log_id desc LIMIT 1;
        -- check if there are candidates to remove
      GET DIAGNOSTICS SWV_RowCount = ROW_COUNT;
      if (SWV_RowCount > 0) then
         DELETE FROM audit_log
         WHERE audit_log_id <= v_id and
         audit_log_id not in(select audit_log_id from event_notification_hist);
      end if;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAuditAlertLogByVdsIDAndType(v_vds_id UUID,
    v_log_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE audit_log set deleted = true
      where vds_id = v_vds_id and log_type = v_log_type;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAuditLogAlertsByVdsID(v_vds_id UUID,
    v_delete_config_alerts BOOLEAN=true)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_min_alret_severity  INTEGER;
   v_no_config_alret_type  INTEGER;
   v_no_max_alret_type  INTEGER;
BEGIN
      v_min_alret_severity := 10;
      v_no_config_alret_type := 9000;
      v_no_max_alret_type := 9005;
      if (v_delete_config_alerts = true) then
         UPDATE audit_log set deleted = true
         where vds_id = v_vds_id and severity >= v_min_alret_severity and
         log_type  between v_no_config_alret_type and v_no_max_alret_type;
      else
         UPDATE audit_log set deleted = true
         where vds_id = v_vds_id and severity >= v_min_alret_severity and
         log_type  between v_no_config_alret_type + 1 and v_no_max_alret_type;
      end if;
END; $procedure$
LANGUAGE plpgsql;

/*
Used to find out how many seconds to wait after Start/Stop/Restart PM operations
v_vds_name     - The host name
v_event        - The event [USER_VDS_STOP | USER_VDS_START | USER_VDS_RESTART]
v_wait_for_sec - Configurable time in seconds to wait from last operation.
Returns : The number of seconds we have to wait (negative value means we can do the operation immediately)
*/
Create or replace FUNCTION get_seconds_to_wait_before_pm_operation(v_vds_name varchar(255), v_event varchar(100), v_wait_for_sec INTEGER ) RETURNS INTEGER STABLE
   AS $procedure$
declare v_last_event_dt timestamp with time zone;
declare v_now_dt timestamp with time zone;
BEGIN
      if exists(select 1 from audit_log where vds_name = v_vds_name and log_type_name = v_event) then
       begin
          v_last_event_dt := log_time
          from audit_log
          where vds_name = v_vds_name and log_type_name = v_event
          order by audit_log_id desc limit 1;
          v_now_dt :=  CURRENT_TIMESTAMP;
          RETURN cast((extract(epoch from v_last_event_dt) + v_wait_for_sec) - extract(epoch from v_now_dt) as int);
       end;
     else
          RETURN 0;
     end if;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAuditLogByOriginAndCustomEventId(v_origin varchar(255), v_custom_event_id INTEGER) RETURNS SETOF audit_log STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM audit_log
      WHERE origin = v_origin and custom_event_id = v_custom_event_id;
END; $procedure$
LANGUAGE plpgsql;

