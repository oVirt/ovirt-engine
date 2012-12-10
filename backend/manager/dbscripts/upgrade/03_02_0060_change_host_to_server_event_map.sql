-- Change Host to Server in gluster related events
update event_map set event_up_name = 'GLUSTER_SERVER_ADD_FAILED' where event_up_name = 'GLUSTER_HOST_ADD_FAILED';
update event_map set event_up_name = 'GLUSTER_SERVER_REMOVE_FAILED' where event_up_name = 'GLUSTER_HOST_REMOVE_FAILED';