SELECT fn_db_change_column_type('qos','inbound_average','SMALLINT','INT');
SELECT fn_db_change_column_type('qos','inbound_peak','SMALLINT','INT');
SELECT fn_db_change_column_type('qos','inbound_burst','SMALLINT','INT');
SELECT fn_db_change_column_type('qos','outbound_average','SMALLINT','INT');
SELECT fn_db_change_column_type('qos','outbound_peak','SMALLINT','INT');
SELECT fn_db_change_column_type('qos','outbound_burst','SMALLINT','INT');
