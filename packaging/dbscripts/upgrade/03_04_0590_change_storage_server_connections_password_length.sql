-- with ISCSI data domain, after a manual discovery of a
-- password protected target, the adding failed with SQL exception
-- due to the difference in length of the password field.

SELECT fn_db_change_column_type('storage_server_connections','password','varchar','text');

