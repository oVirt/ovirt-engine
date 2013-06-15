select fn_db_add_column('vds_static', 'vds_spm_priority', 'SMALLINT DEFAULT 5 CHECK(vds_spm_priority BETWEEN (-1) AND 10)');
