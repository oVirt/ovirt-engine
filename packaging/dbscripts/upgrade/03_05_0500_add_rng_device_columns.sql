-- rng sources required by cluster
select fn_db_add_column('vds_groups', 'required_rng_sources', 'varchar(255)');
UPDATE vds_groups SET required_rng_sources = 'RANDOM' WHERE cast(compatibility_version as float) >= 3.5;

-- rng sources supported by host
select fn_db_add_column('vds_dynamic', 'supported_rng_sources', 'varchar(255)');
