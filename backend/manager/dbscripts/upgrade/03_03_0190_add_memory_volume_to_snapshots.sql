--Add memory volume into snapshots
SELECT fn_db_add_column('snapshots', 'memory_volume', 'character varying(255)');
