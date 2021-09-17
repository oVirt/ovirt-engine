-- -2: auto, parallel or non-parallel
-- -1: auto, parallel only
-- 0: disabled
-- >0: the given number
SELECT fn_db_add_column('cluster',
                        'parallel_migrations',
                        'SMALLINT NOT NULL DEFAULT 0');

-- NULL: cluster default
-- -2: auto, parallel or non-parallel
-- -1: auto, parallel only
-- 0: disabled
-- >0: the given number
SELECT fn_db_add_column('vm_static',
                        'parallel_migrations',
                        'SMALLINT');
