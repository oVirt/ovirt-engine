select fn_db_add_column('base_disks', 'alignment', 'smallint default 0');
select fn_db_add_column('base_disks', 'last_alignment_scan', 'timestamp with time zone');
insert into action_version_map values (232, '3.3', '*');
