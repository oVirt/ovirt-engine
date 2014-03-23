-- ----------------------------------------------------------------------
-- Adding user name for vm_init table (cloud-init usage)
-- ----------------------------------------------------------------------
alter table vm_init
add column user_name VARCHAR(256) DEFAULT NULL;
