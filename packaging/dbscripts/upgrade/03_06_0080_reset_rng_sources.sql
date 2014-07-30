-- rng sources required by cluster
UPDATE vds_groups SET required_rng_sources = ''
WHERE cast(compatibility_version as float) <= 3.5
 AND cast (vds_group_id as text) = (select option_value from vdc_options where option_name = 'AutoRegistrationDefaultVdsGroupID');

