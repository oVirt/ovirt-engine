-- Remove the group, group ids column from the users table

select fn_db_drop_column('users', 'active');
select fn_db_drop_column('users', 'role');
select fn_db_drop_column('users', 'groups');
select fn_db_drop_column('users', 'group_ids');
