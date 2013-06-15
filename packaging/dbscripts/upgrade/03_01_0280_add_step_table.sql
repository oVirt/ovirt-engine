CREATE OR REPLACE FUNCTION __temp_Upgrade_add_step_table()
RETURNS void
AS $function$
BEGIN
   IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name ILIKE 'step') THEN

   -- Add the step table.
      CREATE TABLE step
      (
         step_id UUID NOT NULL,
         parent_step_id UUID,
         job_id UUID NOT NULL CONSTRAINT fk_step_job REFERENCES job(job_id) ON DELETE CASCADE,
         step_type VARCHAR(32) NOT NULL,
         description TEXT NOT NULL,
         step_number INTEGER NOT NULL,
         status VARCHAR(32) NOT NULL,
         start_time TIMESTAMP WITH TIME ZONE NOT NULL,
         end_time TIMESTAMP WITH TIME ZONE default NULL,
         correlation_id VARCHAR(50) NOT NULL,
         external_id UUID,
         external_system_type VARCHAR(32),
         CONSTRAINT pk_steps PRIMARY KEY(step_id)
      )
      WITH OIDS;

      CREATE INDEX idx_step_job_id ON step(job_id);

      CREATE INDEX idx_step_parent_step_id ON step(parent_step_id);

   END IF;

END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_add_step_table();

DROP FUNCTION __temp_Upgrade_add_step_table();

