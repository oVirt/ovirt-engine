SELECT fn_db_add_column('job', 'engine_session_seq_id', 'BIGINT');
CREATE INDEX idx_job_engine_session_seq_id ON job(engine_session_seq_id);
