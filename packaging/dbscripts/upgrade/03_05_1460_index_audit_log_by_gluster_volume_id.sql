-- Create partial index for fetching audit_log by gluster_volume_id ID, when it's not null
CREATE INDEX idx_audit_log_gluster_volume_id ON audit_log(gluster_volume_id) WHERE gluster_volume_id IS NOT NULL;
