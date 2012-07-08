DROP INDEX if exists IDX_audit_log_job_id;
CREATE INDEX IDX_audit_log_job_id ON audit_log(job_id);

DROP INDEX if exists IDX_audit_log_correlation_id;
CREATE INDEX IDX_audit_correlation_id ON audit_log(correlation_id);

