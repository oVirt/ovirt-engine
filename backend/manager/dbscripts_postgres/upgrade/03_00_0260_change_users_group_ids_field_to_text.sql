-- change group_ids length of users to unlimited
select fn_db_change_column_type('users','group_ids','varchar','text');
