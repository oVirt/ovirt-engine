----------------------------------------------------------------
-- [gluster_scheduler_job_details] Table
--

CREATE OR REPLACE FUNCTION InsertSchedulerJob (
    v_job_id uuid,
    v_job_name VARCHAR(300),
    v_job_class_name VARCHAR(300),
    v_cron_schedule VARCHAR(300),
    v_start_date DATE DEFAULT NULL,
    v_end_date DATE DEFAULT NULL,
    v_timezone VARCHAR(300) DEFAULT NULL
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_scheduler_job_details (
        job_id,
        job_name,
        job_class_name,
        cron_schedule,
        start_date,
        end_date,
        timezone
        )
    VALUES (
        v_job_id,
        v_job_name,
        v_job_class_name,
        v_cron_schedule,
        v_start_date,
        v_end_date,
        v_timezone
        );
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllGlusterSchedulerJobs ()
RETURNS SETOF gluster_scheduler_job_details STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_scheduler_job_details;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterJobById (v_job_id uuid)
RETURNS SETOF gluster_scheduler_job_details STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_scheduler_job_details
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterJob (v_job_id uuid )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_scheduler_job_details
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertJobParams (
    v_id uuid,
    v_job_id uuid,
    v_params_class_name VARCHAR(300),
    v_params_class_value VARCHAR(300)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_scheduler_job_params (
        id,
        job_id,
        params_class_name,
        params_class_value
        )
    VALUES (
        v_id,
        v_job_id,
        v_params_class_name,
        v_params_class_value
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterJobParamsByJobId (v_job_id uuid)
RETURNS SETOF gluster_scheduler_job_params STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_scheduler_job_params
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterJobParams (v_job_id uuid)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_scheduler_job_params
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


