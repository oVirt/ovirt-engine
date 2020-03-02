CREATE TABLE gluster_scheduler_job_details (
    job_id uuid UNIQUE NOT NULL ,
    job_name character varying(300) NOT NULL,
    job_class_name character varying(300) NOT NULL,
    cron_schedule character varying(300) NOT NULL,
    start_date DATE,
    end_date DATE,
    timezone VARCHAR(300)
);

CREATE TABLE gluster_scheduler_job_params (
    id uuid NOT NULL ,
    job_id uuid NOT NULL ,
    params_class_name character varying(300) NOT NULL,
    params_class_value character varying(300)
);

ALTER TABLE ONLY gluster_scheduler_job_details
    ADD CONSTRAINT pk_gluster_scheduler_job_details PRIMARY KEY (job_id);

ALTER TABLE ONLY gluster_scheduler_job_params
    ADD CONSTRAINT pk_gluster_scheduler_job_params PRIMARY KEY (id);
