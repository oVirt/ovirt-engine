-- Add vmjobs table
CREATE TABLE vm_jobs
(
     vm_job_id UUID NOT NULL,
     vm_id UUID NOT NULL,
     job_state INTEGER DEFAULT 0 NOT NULL,
     job_type INTEGER NOT NULL,
     block_job_type INTEGER,
     bandwidth INTEGER,
     cursor_cur BIGINT,
     cursor_end BIGINT,
     image_group_id UUID,
     CONSTRAINT pk_vm_jobs PRIMARY KEY(vm_job_id)
) WITH OIDS;

CREATE INDEX IDX_vm_jobs_vm_id ON vm_jobs(vm_id);

ALTER TABLE ONLY vm_jobs
    ADD CONSTRAINT fk_vm_jobs_vm_static FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE;
