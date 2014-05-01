ALTER TABLE event_subscriber DROP CONSTRAINT fk_event_subscriber_event_map;
update event_subscriber set event_up_name = 'HOST_INTERFACE_HIGH_NETWORK_USE' where event_up_name='VDS_HIGH_NETWORK_USE';
update event_map set event_up_name = 'HOST_INTERFACE_HIGH_NETWORK_USE' where event_up_name='VDS_HIGH_NETWORK_USE';
ALTER TABLE event_subscriber ADD CONSTRAINT fk_event_subscriber_event_map FOREIGN KEY (event_up_name) REFERENCES event_map(event_up_name) ON DELETE CASCADE;
