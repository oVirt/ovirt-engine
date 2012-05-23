select fn_db_drop_column('roles', 'is_inheritable');
select fn_db_add_column('roles', 'allows_viewing_children', 'boolean not null default false');
