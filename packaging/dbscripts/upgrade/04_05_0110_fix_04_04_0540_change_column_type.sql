SELECT fn_db_change_column_type('storage_domain_dr','gluster_scheduler_job_id','VARCHAR(256)','UUID USING gluster_scheduler_job_id::uuid');
