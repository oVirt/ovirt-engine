
select fn_db_add_column('storage_server_connections', 'nfs_version', 'smallint default null');
select fn_db_add_column('storage_server_connections', 'nfs_timeo', 'smallint default null');
select fn_db_add_column('storage_server_connections', 'nfs_retrans', 'smallint default null');

