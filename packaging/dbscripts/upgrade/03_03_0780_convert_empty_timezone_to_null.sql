-- update VMs from old engine version that used
-- empty string to represent 'default' time_zome
update vm_static
set time_zone = NULL
where time_zone = '';
