-- Modify the user_name field type to support longer usernames, as some storage
-- appliances support 256 character long usernames, and some software based solutions support
-- usernames as long as 512 characters.

SELECT fn_db_change_column_type('storage_server_connections','user_name','varchar','text');
