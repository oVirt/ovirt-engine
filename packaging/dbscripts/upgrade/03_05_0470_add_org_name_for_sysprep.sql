-- ----------------------------------------------------------------------
-- Adding org name for vm_init table (Sysprep usage)
-- ----------------------------------------------------------------------
alter table vm_init
add column org_name VARCHAR(256) DEFAULT NULL;
