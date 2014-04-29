delete from event_map
where event_up_name in (
	'NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM',
	'NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM');

insert into event_map(event_up_name, event_down_name)
values('NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM', 'UNASSIGNED');

insert into event_map(event_up_name, event_down_name)
values('NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM', 'UNASSIGNED');
