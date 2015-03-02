select fn_db_add_column('vds_dynamic', 'incoming_migrations', 'INTEGER NOT NULL DEFAULT 0');
select fn_db_add_column('vds_dynamic', 'outgoing_migrations', 'INTEGER NOT NULL DEFAULT 0');
