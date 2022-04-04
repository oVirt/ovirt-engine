-- 0: no progress
-- >0: the progress
SELECT fn_db_add_column('cluster',
                        'upgrade_percent_complete',
                        'SMALLINT NOT NULL DEFAULT 0');

SELECT fn_db_add_column('cluster',
                        'upgrade_correlation_id',
                        'VARCHAR(50)');
