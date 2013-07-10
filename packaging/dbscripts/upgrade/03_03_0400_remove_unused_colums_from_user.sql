-- Remove the session_count and desktop_device columns from the users table, as
-- they don't have any use currently:

select fn_db_drop_column('users', 'session_count');
select fn_db_drop_column('users', 'desktop_device');
