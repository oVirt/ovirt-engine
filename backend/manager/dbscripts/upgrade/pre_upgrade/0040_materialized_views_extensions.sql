-- Determines the minimum duration we will have to wait between 2 refreshes , 0 means : no wait
select fn_db_add_column('materialized_views', 'min_refresh_rate_in_sec', 'int default 0');
-- Indicates if this is a product view or custom view
select fn_db_add_column('materialized_views', 'custom', 'boolean default false');
-- Indicates if this materialized_view is currently active
select fn_db_add_column('materialized_views', 'active', 'boolean default true');
