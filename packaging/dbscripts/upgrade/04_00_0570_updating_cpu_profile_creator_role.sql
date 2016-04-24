-- Updating CpuProfileCreator to be a 'admin' role instead of 'user'.
UPDATE roles
SET role_type = 1
WHERE name = 'CpuProfileCreator';
