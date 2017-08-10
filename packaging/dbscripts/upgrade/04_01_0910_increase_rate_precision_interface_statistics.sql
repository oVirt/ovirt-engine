SELECT fn_db_change_column_type('vds_interface_statistics','rx_drop','NUMERIC','DECIMAL(18, 4)');
SELECT fn_db_change_column_type('vds_interface_statistics','rx_rate','NUMERIC','DECIMAL(18, 4)');
SELECT fn_db_change_column_type('vds_interface_statistics','tx_drop','NUMERIC','DECIMAL(18, 4)');
SELECT fn_db_change_column_type('vds_interface_statistics','tx_rate','NUMERIC','DECIMAL(18, 4)');

SELECT fn_db_change_column_type('vm_interface_statistics','rx_drop','NUMERIC','DECIMAL(18, 4)');
SELECT fn_db_change_column_type('vm_interface_statistics','rx_rate','NUMERIC','DECIMAL(18, 4)');
SELECT fn_db_change_column_type('vm_interface_statistics','tx_drop','NUMERIC','DECIMAL(18, 4)');
SELECT fn_db_change_column_type('vm_interface_statistics','tx_rate','NUMERIC','DECIMAL(18, 4)');
