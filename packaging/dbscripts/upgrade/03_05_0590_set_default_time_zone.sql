-- set the defalut time zone from vdc_options

-- set windows timezone if not set
update vm_static set time_zone=(select option_value from vdc_options where option_name = 'DefaultWindowsTimeZone') where time_zone is NULL and os in (select os_id from dwh_osinfo where os_name ilike '%windows%') and vm_guid != '00000000-0000-0000-0000-000000000000';

-- set other os timezone if not set
update vm_static set time_zone=(select option_value from vdc_options where option_name = 'DefaultGeneralTimeZone') where time_zone is NULL and os not in (select os_id from dwh_osinfo where os_name ilike '%windows%') and vm_guid != '00000000-0000-0000-0000-000000000000';

