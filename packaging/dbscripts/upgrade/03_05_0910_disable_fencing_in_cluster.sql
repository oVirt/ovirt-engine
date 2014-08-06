-- By default set to false not to break upgrade
SELECT fn_db_add_column('vds_groups', 'fencing_enabled', 'boolean DEFAULT true');
