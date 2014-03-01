ALTER TABLE event_subscriber RENAME COLUMN notification_method TO nmx;
ALTER TABLE event_subscriber ADD COLUMN notification_method CHARACTER VARYING(32);

UPDATE event_subscriber SET notification_method = 'smtp' WHERE nmx = 'EMAIL';
UPDATE event_subscriber SET notification_method = 'snmp' WHERE nmx = 'SNMP_TRAP';

ALTER TABLE event_subscriber DROP COLUMN nmx;

-- Name check constraint so it could be easily dropped in future if needed
ALTER TABLE event_subscriber ADD CONSTRAINT event_subscriber_method_check CHECK (notification_method IN ('smtp', 'snmp'));

