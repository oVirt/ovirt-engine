CREATE OR REPLACE FUNCTION __temp_Upgrade_add_async_task_entities_table()
RETURNS void
AS $function$
BEGIN
   IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name ILIKE 'async_tasks_entities') THEN
      CREATE TABLE async_tasks_entities
      (
         async_task_id UUID NOT NULL CONSTRAINT fk_async_task_entity REFERENCES async_tasks(task_id) ON DELETE CASCADE,
         entity_id UUID NOT NULL,
         entity_type varchar(128)
      )
      WITH OIDS;
   END IF;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_add_async_task_entities_table();

DROP FUNCTION __temp_Upgrade_add_async_task_entities_table();




