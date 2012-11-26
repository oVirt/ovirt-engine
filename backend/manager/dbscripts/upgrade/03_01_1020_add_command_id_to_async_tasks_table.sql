-- setting the column with default value to insure that upgrade works even if table is not empty
select fn_db_add_column('async_tasks', 'command_id', 'UUID NOT NULL DEFAULT ''00000000-0000-0000-0000-000000000000''');
-- dropping the default value
ALTER TABLE async_tasks ALTER COLUMN command_id DROP DEFAULT;

