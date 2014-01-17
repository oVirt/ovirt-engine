update event_map set event_down_name = 'VDS_DETECTED' where event_up_name='VDS_SET_NONOPERATIONAL';
update event_map set event_down_name = 'VDS_DETECTED' where event_up_name='VDS_SET_NONOPERATIONAL_IFACE_DOWN';
update event_map set event_down_name = 'VDS_DETECTED' where event_up_name='VDS_SET_NONOPERATIONAL_DOMAIN';
