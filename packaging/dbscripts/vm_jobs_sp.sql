

---------------------
-- vm_jobs functions
---------------------
CREATE OR REPLACE FUNCTION GetAllVmJobs ()
RETURNS SETOF vm_jobs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_jobs.*
    FROM vm_jobs;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmJobs (
    v_vm_job_id UUID,
    v_vm_id UUID,
    v_job_state INT,
    v_job_type INT,
    v_block_job_type INT,
    v_bandwidth INT,
    v_cursor_cur BIGINT,
    v_cursor_end BIGINT,
    v_image_group_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_jobs
    SET vm_job_id = v_vm_job_id,
        vm_id = v_vm_id,
        job_state = v_job_state,
        job_type = v_job_type,
        block_job_type = v_block_job_type,
        bandwidth = v_bandwidth,
        cursor_cur = v_cursor_cur,
        cursor_end = v_cursor_end,
        image_group_id = v_image_group_id
    WHERE vm_job_id = v_vm_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmJobs (v_vm_job_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_jobs
    WHERE vm_job_id = v_vm_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmJobs (
    v_vm_job_id UUID,
    v_vm_id UUID,
    v_job_state INT,
    v_job_type INT,
    v_block_job_type INT,
    v_bandwidth INT,
    v_cursor_cur BIGINT,
    v_cursor_end BIGINT,
    v_image_group_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_jobs (
        vm_job_id,
        vm_id,
        job_state,
        job_type,
        block_job_type,
        bandwidth,
        cursor_cur,
        cursor_end,
        image_group_id
        )
    VALUES (
        v_vm_job_id,
        v_vm_id,
        v_job_state,
        v_job_type,
        v_block_job_type,
        v_bandwidth,
        v_cursor_cur,
        v_cursor_end,
        v_image_group_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;


