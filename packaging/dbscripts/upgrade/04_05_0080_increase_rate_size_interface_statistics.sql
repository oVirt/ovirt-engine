SELECT fn_db_change_column_type('vds_interface_statistics','rx_drop','NUMERIC','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vds_interface_statistics','tx_drop','NUMERIC','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vds_interface_statistics','rx_total','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vds_interface_statistics','tx_total','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vds_interface_statistics','rx_offset','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vds_interface_statistics','tx_offset','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vds_interface_statistics','rx_rate','NUMERIC','NUMERIC(24, 4)');
SELECT fn_db_change_column_type('vds_interface_statistics','tx_rate','NUMERIC','NUMERIC(24, 4)');

SELECT fn_db_change_column_type('vm_interface_statistics','rx_drop','NUMERIC','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vm_interface_statistics','tx_drop','NUMERIC','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vm_interface_statistics','rx_total','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vm_interface_statistics','tx_total','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vm_interface_statistics','rx_offset','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vm_interface_statistics','tx_offset','BIGINT','NUMERIC(20, 0)');
SELECT fn_db_change_column_type('vm_interface_statistics','rx_rate','NUMERIC','NUMERIC(24, 4)');
SELECT fn_db_change_column_type('vm_interface_statistics','tx_rate','NUMERIC','NUMERIC(24, 4)');
