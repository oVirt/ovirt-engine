






----------------------------------------------------------------
-- [event_notification_hist] Table
--


Create or replace FUNCTION insertevent_notification_hist(v_audit_log_id BIGINT,
	v_event_name VARCHAR(100),
	v_method_type  CHAR(10),
	v_reason CHAR(255) ,
	v_sent_at TIMESTAMP WITH TIME ZONE,
	v_status BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO event_notification_hist(audit_log_id, event_name, method_type, reason, sent_at, status)
	VALUES(v_audit_log_id, v_event_name, v_method_type, v_reason, v_sent_at, v_status);
END; $procedure$
LANGUAGE plpgsql;





----------------------------------------------------------------
-- [event_subscriber] Table
--


Create or replace FUNCTION Insertevent_subscriber(v_event_up_name VARCHAR(100),
	v_notification_method VARCHAR(32),
    v_method_address VARCHAR(255),
	v_subscriber_id UUID,
	v_tag_name VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
      if not exists(select * from  event_subscriber where
      subscriber_id = v_subscriber_id and
      event_up_name = v_event_up_name and
      notification_method = v_notification_method and
      tag_name = v_tag_name) then

INSERT INTO event_subscriber(event_up_name, notification_method, method_address, subscriber_id, tag_name)
			VALUES(v_event_up_name, v_notification_method, v_method_address, v_subscriber_id,v_tag_name);
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

Create or replace FUNCTION Deleteevent_subscriber(v_event_up_name VARCHAR(100) ,
  v_notification_method VARCHAR(32),
	v_subscriber_id UUID,
    v_tag_name VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
      if (v_tag_name IS NULL) then
         delete from event_subscriber
         where event_up_name = v_event_up_name
         and notification_method = v_notification_method
         and subscriber_id = v_subscriber_id;
      else
         delete from event_subscriber
         where event_up_name = v_event_up_name
         and notification_method = v_notification_method
         and subscriber_id = v_subscriber_id
         and tag_name = v_tag_name;
      end if;
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


