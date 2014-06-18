-- VM_MIGRATION_FAILED_FROM_TO was not in use
update event_map set event_up_name = 'VM_MIGRATION_TO_SERVER_FAILED' where event_up_name='VM_MIGRATION_FAILED_FROM_TO';

