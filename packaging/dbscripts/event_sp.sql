






----------------------------------------------------------------
-- [event_notification_hist] Table
--


Create or replace FUNCTION insertevent_notification_hist(v_audit_log_id BIGINT,
	v_event_name VARCHAR(100),
	v_method_type  CHAR(10),
	v_reason CHAR(255) ,
	v_sent_at TIMESTAMP WITH TIME ZONE,
	v_status BOOLEAN,
	v_subscriber_id VARCHAR(100))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO event_notification_hist(audit_log_id, event_name, method_type, reason, sent_at, status, subscriber_id)
	VALUES(v_audit_log_id, v_event_name, v_method_type, v_reason, v_sent_at, v_status, v_subscriber_id::uuid);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromevent_notification_hist()
RETURNS SETOF event_notification_hist STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM event_notification_hist;

END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [event_subscriber] Table
--


Create or replace FUNCTION Insertevent_subscriber(v_event_up_name VARCHAR(100),
	v_method_id INTEGER,
    v_method_address VARCHAR(255),
	v_subscriber_id UUID,
	v_tag_name VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
      if not exists(select * from  event_subscriber where
      subscriber_id = v_subscriber_id and
      event_up_name = v_event_up_name and
      method_id = v_method_id and
      tag_name = v_tag_name) then

INSERT INTO event_subscriber(event_up_name, method_id, method_address, subscriber_id, tag_name)
			VALUES(v_event_up_name, v_method_id, v_method_address, v_subscriber_id,v_tag_name);
      end if;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION Getevent_subscriberBysubscriber_id(v_subscriber_id UUID)
RETURNS SETOF event_subscriber STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM event_subscriber
   WHERE subscriber_id = v_subscriber_id;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllFromevent_audit_log_subscriber()
RETURNS SETOF event_audit_log_subscriber_view STABLE
   AS $procedure$
   DECLARE
   v_last  BIGINT;
BEGIN
      -- begin tran


			-- get last event
			select   audit_log_id INTO v_last from audit_log    order by audit_log_id desc LIMIT 1;
			-- mark processed events
      update audit_log set processed = TRUE where audit_log_id <= v_last;
			-- get from view all events with id <= @last
      RETURN QUERY SELECT *
      from event_audit_log_subscriber_view  event_audit_log_subscriber_view
      where audit_log_id <= v_last;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Deleteevent_subscriber(v_event_up_name VARCHAR(100) ,
	v_method_id INTEGER ,
	v_subscriber_id UUID,
    v_tag_name VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
      if (v_tag_name IS NULL) then
         delete from event_subscriber
         where event_up_name = v_event_up_name
         and method_id = v_method_id
         and subscriber_id = v_subscriber_id;
      else
         delete from event_subscriber
         where event_up_name = v_event_up_name
         and method_id = v_method_id
         and subscriber_id = v_subscriber_id
         and tag_name = v_tag_name;
      end if;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Updateevent_subscriber(v_event_up_name VARCHAR(100) ,
	v_old_method_id INTEGER ,
	v_new_method_id INTEGER ,
	v_subscriber_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      update event_subscriber set method_id = v_new_method_id
      where event_up_name = v_event_up_name
      and method_id = v_old_method_id
      and subscriber_id = v_subscriber_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetEventNotificationMethodById(v_method_id INTEGER)
RETURNS SETOF event_notification_methods STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      from event_notification_methods
      where method_id = v_method_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetEventMapByName(v_event_name VARCHAR(100))
RETURNS SETOF event_map STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      from event_map
      where event_up_name = v_event_name;
END; $procedure$
LANGUAGE plpgsql;




-------------------------------------------------------------------------------------
--- GetAllFromevent_audit_log_subscriber used to get un notified events
-------------------------------------------------------------------------------------


Create or replace FUNCTION GetAllFromevent_audit_log_subscriber_only() RETURNS SETOF event_audit_log_subscriber_view STABLE
   AS $procedure$
   DECLARE
   v_last  BIGINT;
BEGIN
        -- get last event
        select   audit_log_id INTO v_last from audit_log order by audit_log_id desc LIMIT 1;
        -- get from view all events with id <= @last
        RETURN QUERY select *
                   from event_audit_log_subscriber_view  where audit_log_id <= v_last;
END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [dbo].[event_notification_hist] Table
----------------------------------------------------------------


Create or replace FUNCTION Deleteevent_notification_hist(v_sent_at TIMESTAMP)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM event_notification_hist WHERE sent_at < v_sent_at;
END; $procedure$
LANGUAGE plpgsql;


