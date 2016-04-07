-- Updating CpuProfileCreator to be a 'user' role instead of 'admin'.
-- When adding Cpu Profile, user needs to be able to add the
-- CpuProfileCreator to itself(Can't add admin roles when not
-- admin).
UPDATE roles
SET role_type = 2
WHERE name = 'CpuProfileCreator';
