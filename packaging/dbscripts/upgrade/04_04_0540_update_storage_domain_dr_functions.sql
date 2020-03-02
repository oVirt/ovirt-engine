select fn_db_change_column_type('storage_domain_dr','qrtz_job_id','VARCHAR(256)','uuid');
select fn_db_rename_column('storage_domain_dr','qrtz_job_id','gluster_scheduler_job_id');
