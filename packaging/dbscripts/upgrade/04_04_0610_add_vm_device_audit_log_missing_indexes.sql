CREATE INDEX IF NOT EXISTS idx_audit_log_deleted ON audit_log(deleted);
CREATE INDEX IF NOT EXISTS idx_audit_log_severity ON audit_log(severity);
CREATE INDEX IF NOT EXISTS idx_vm_device_is_plugged ON vm_device(is_plugged);
