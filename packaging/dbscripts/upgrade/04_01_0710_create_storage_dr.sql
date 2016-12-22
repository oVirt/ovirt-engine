CREATE TABLE storage_domain_dr (
    storage_domain_id UUID NOT NULL,
    georep_session_id UUID NOT NULL,
    sync_schedule VARCHAR(256),
    qrtz_job_id VARCHAR(256),
    CONSTRAINT pk_storage_domain_dr PRIMARY KEY (storage_domain_id, georep_session_id)
);

SELECT fn_db_create_constraint('storage_domain_dr', 'fk_storage_domain_dr_storage_domain_id', 'FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE');
SELECT fn_db_create_constraint('storage_domain_dr', 'fk_storage_domain_dr_georep_session_id', 'FOREIGN KEY (georep_session_id) REFERENCES gluster_georep_session(session_id)');

