SELECT fn_db_add_column('permissions', 'creation_date', 'bigint default (extract (epoch from now()))');
