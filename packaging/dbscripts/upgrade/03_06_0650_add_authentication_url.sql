SELECT fn_db_add_column('providers', 'auth_url', 'TEXT DEFAULT NULL');

UPDATE providers set auth_url = (select option_value from vdc_options where option_name = 'KeystoneAuthUrl')
    WHERE auth_required and provider_type in ('OPENSTACK_IMAGE','OPENSTACK_NETWORK');

-- this must be done here since 0000_config.sql is running in the pre-upgrade stage

select fn_db_delete_config_value('KeystoneAuthUrl','general');

