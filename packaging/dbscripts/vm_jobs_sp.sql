---------------------
-- vm_jobs functions
---------------------

Create or replace FUNCTION GetAllVmJobIds() RETURNS SETOF uuid STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_jobs.vm_job_id
      FROM vm_jobs;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmJobsByVmId(v_vm_id uuid) RETURNS SETOF vm_jobs STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_jobs.*
   FROM vm_jobs
   WHERE vm_jobs.vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetVmJobsByVmAndImage(v_vm_id uuid, v_image_group_id uuid) RETURNS SETOF vm_jobs STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT vm_jobs.*
   FROM vm_jobs
   WHERE vm_jobs.vm_id = v_vm_id
     AND vm_jobs.image_group_id = v_image_group_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateVmJobs(
    v_vm_job_id UUID,
    v_vm_id UUID,
    v_job_state INTEGER,
    v_job_type INTEGER,
    v_block_job_type INTEGER,
    v_bandwidth INTEGER,
    v_cursor_cur BIGINT,
    v_cursor_end BIGINT,
    v_image_group_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE vm_jobs
      SET vm_job_id=v_vm_job_id, vm_id=v_vm_id, job_state=v_job_state, job_type=v_job_type,
          block_job_type=v_block_job_type, bandwidth=v_bandwidth, cursor_cur=v_cursor_cur,
          cursor_end=v_cursor_end, image_group_id=v_image_group_id
      WHERE vm_job_id = v_vm_job_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteVmJobs(v_vm_job_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM vm_jobs
      WHERE vm_job_id = v_vm_job_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertVmJobs(
    v_vm_job_id UUID,
    v_vm_id UUID,
    v_job_state INTEGER,
    v_job_type INTEGER,
    v_block_job_type INTEGER,
    v_bandwidth INTEGER,
    v_cursor_cur BIGINT,
    v_cursor_end BIGINT,
    v_image_group_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vm_jobs(vm_job_id, vm_id, job_state, job_type, block_job_type, bandwidth,
                    cursor_cur, cursor_end, image_group_id)
       VALUES (v_vm_job_id, v_vm_id, v_job_state, v_job_type, v_block_job_type, v_bandwidth,
               v_cursor_cur, v_cursor_end, v_image_group_id);
END; $procedure$
LANGUAGE plpgsql;
