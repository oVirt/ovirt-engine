
select fn_db_add_column('vm_dynamic', 'last_watchdog_event', 'bigint');
select fn_db_add_column('vm_dynamic', 'last_watchdog_action', 'varchar(8)');

