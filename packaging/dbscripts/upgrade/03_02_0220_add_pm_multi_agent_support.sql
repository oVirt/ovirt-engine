-- Adding secondary PM agent device fields
select fn_db_add_column('vds_static', 'pm_secondary_ip', 'VARCHAR(255) NULL');
select fn_db_add_column('vds_static', 'pm_secondary_type', 'VARCHAR(20) NULL');
select fn_db_add_column('vds_static', 'pm_secondary_user', 'VARCHAR(50) NULL');
select fn_db_add_column('vds_static', 'pm_secondary_password', 'TEXT NULL');
select fn_db_add_column('vds_static', 'pm_secondary_port', 'INTEGER NULL ');
select fn_db_add_column('vds_static', 'pm_secondary_options', 'VARCHAR(4000) NULL');
select fn_db_add_column('vds_static', 'pm_secondary_concurrent', 'BOOLEAN NULL DEFAULT false');
