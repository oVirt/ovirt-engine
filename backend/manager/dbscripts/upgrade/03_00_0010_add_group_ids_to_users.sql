-- refactor the db to query groups of usres by their group id.
-- represent the relation of group -> user by adding id of the group
-- to users

-- The old way to get user groups was by parsing the groups name list
-- The fix is to store the list of the groups uuid's and use it instead of list of group names
-- The fix is around switching between fnsplitter(users.groups) to fnsplitteruuid(users.group_ids)

select fn_db_add_column('users', 'group_ids', 'VARCHAR(2048)');
