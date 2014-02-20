-- ----------------------------------------------------------------------
-- Adding setup language options for Windows Sysprep
-- ----------------------------------------------------------------------
alter table vm_init
add column input_locale VARCHAR(256) DEFAULT NULL,
add column ui_language VARCHAR(256) DEFAULT NULL,
add column system_locale VARCHAR(256) DEFAULT NULL,
add column user_locale VARCHAR(256) DEFAULT NULL;
