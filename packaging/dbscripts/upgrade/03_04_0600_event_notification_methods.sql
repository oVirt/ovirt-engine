-- DROP event_notification_methods - redundant table
ALTER TABLE event_subscriber DROP CONSTRAINT fk_event_subscriber_event_notification_methods;
DROP TABLE event_notification_methods ;

-- save EventNotificationMethod as string with constraint.
ALTER TABLE event_subscriber ADD COLUMN notification_method CHARACTER VARYING(32)
  CHECK (notification_method IN ('EMAIL'));
ALTER TABLE event_subscriber DROP CONSTRAINT pk_event_subscriber;
ALTER TABLE event_subscriber
  ADD CONSTRAINT pk_event_subscriber PRIMARY KEY (subscriber_id, event_up_name, notification_method, tag_name);
ALTER TABLE event_subscriber DROP COLUMN method_id;

UPDATE event_subscriber SET notification_method = 'EMAIL';

ALTER TABLE event_subscriber ALTER notification_method SET NOT NULL;
