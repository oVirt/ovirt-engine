
update vdc_options
set option_value = (select vds_group_id from vds_groups where name ilike 'Default')
where option_name = 'AutoRegistrationDefaultVdsGroupID' and version = 'general';

