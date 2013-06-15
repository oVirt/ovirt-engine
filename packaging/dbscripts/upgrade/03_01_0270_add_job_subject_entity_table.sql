CREATE OR REPLACE FUNCTION __temp_Upgrade_add_job_subject_entity_table()
RETURNS void
AS $function$
BEGIN
   IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name ILIKE 'job_subject_entity') THEN

   -- Add the job_subject_entity table.
      CREATE TABLE job_subject_entity
      (
         job_id UUID NOT NULL  CONSTRAINT fk_job_subject_entity_job REFERENCES job(job_id) ON DELETE CASCADE,
         entity_id UUID NOT NULL,
         entity_type VARCHAR(32) NOT NULL,
         CONSTRAINT pk_jobs_subject_entity PRIMARY KEY(job_id,entity_id)
      )
      WITH OIDS;

      CREATE INDEX idx_job_subject_entity_entity_id ON job_subject_entity(entity_id);

   END IF;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_add_job_subject_entity_table();

DROP FUNCTION __temp_Upgrade_add_job_subject_entity_table();

