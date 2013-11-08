DROP INDEX if exists IDX_job_start_time;
CREATE INDEX IDX_job_start_time ON job(start_time);

