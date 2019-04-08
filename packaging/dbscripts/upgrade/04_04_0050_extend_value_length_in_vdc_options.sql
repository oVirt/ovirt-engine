SELECT fn_db_change_column_type('vdc_options', 'default_value', 'VARCHAR(4000) NOT NULL', 'TEXT NOT NULL');
SELECT fn_db_change_column_type('vdc_options', 'option_value', 'VARCHAR(4000) NOT NULL', 'TEXT NOT NULL');
