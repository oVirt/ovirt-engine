-- This was already done in 04_01_0950_fix_max_memory_size_limit.sql
-- but we want to run it again, because we didn't add then the constraint
-- we have in 04_02_0140_add_max_memory_constraint.sql, so need to fix
-- VMs broken since the upgrade to 4.1 with
-- 04_01_0950_fix_max_memory_size_limit.sql.
-- Adding this comment also so that the file checksum will be different
-- and engine-setup will not skip it.
UPDATE vm_static
SET max_memory_size_mb = mem_size_mb
WHERE max_memory_size_mb < mem_size_mb;
