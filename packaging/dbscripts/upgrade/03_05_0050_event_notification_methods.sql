-- DROP event_notification_methods - redundant table
ALTER TABLE event_subscriber DROP CONSTRAINT fk_event_subscriber_event_notification_methods;
DROP TABLE event_notification_methods ;

-- save EventNotificationMethod as string with constraint.
ALTER TABLE event_subscriber ADD COLUMN notification_method CHARACTER VARYING(32) DEFAULT 'EMAIL'
  CHECK (notification_method IN ('EMAIL', 'SNMP_TRAP'));
ALTER TABLE event_subscriber DROP CONSTRAINT pk_event_subscriber;
ALTER TABLE event_subscriber
  ADD CONSTRAINT pk_event_subscriber PRIMARY KEY (subscriber_id, event_up_name, notification_method, tag_name);
ALTER TABLE event_subscriber DROP COLUMN method_id;

ALTER TABLE event_subscriber ALTER notification_method SET NOT NULL;

------------------------------------------------------------------------------------------
-- Remove the connection between a subscription address and the subscribing system user --
------------------------------------------------------------------------------------------

-- Up to this change if a subscriber had no email it's address was taken from the users table
-- this behaviour is removed from here on.
UPDATE event_subscriber AS es
SET method_address = u.email
FROM event_subscriber AS es2
  INNER JOIN users u ON es2.subscriber_id = u.user_id
WHERE (es.method_address is NULL OR trim(both from es.method_address) = '');

-- change events history table
ALTER TABLE event_notification_hist DROP COLUMN subscriber_id;
