-- ----------------------------------------------------------------------
-- Adding user name for vm_init table (cloud-init usage)
-- ----------------------------------------------------------------------
alter table vm_init
add column active_directory_ou VARCHAR(256) DEFAULT NULL;
