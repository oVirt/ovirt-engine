CREATE OR REPLACE FUNCTION __temp_Upgrade_add_job_table()
RETURNS void
AS $function$
BEGIN
   IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name ILIKE 'job') THEN

       -- Add the job table.
       CREATE TABLE job
       (
          job_id UUID NOT NULL,
          action_type VARCHAR(50) NOT NULL,
          description TEXT NOT NULL,
          status VARCHAR(32) NOT NULL,
          owner_id UUID,
          visible BOOLEAN NOT NULL DEFAULT true,
          start_time TIMESTAMP WITH TIME ZONE NOT NULL,
          end_time TIMESTAMP WITH TIME ZONE default NULL,
          last_update_time TIMESTAMP WITH TIME ZONE default NULL,
          correlation_id VARCHAR(50) NOT NULL,
          CONSTRAINT pk_jobs PRIMARY KEY(job_id)
       )
       WITH OIDS;

   END IF;
END; $function$
LANGUAGE plpgsql;


SELECT * FROM __temp_Upgrade_add_job_table();

DROP FUNCTION __temp_Upgrade_add_job_table();

