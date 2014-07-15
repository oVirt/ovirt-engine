-- By default set to false not to break upgrade
SELECT fn_db_add_column('vds_groups', 'skip_fencing_if_sd_active', 'boolean DEFAULT false');
