-- new keys
select fn_db_add_config_value('SignLockFile','/var/lock/engine/.openssl.exclusivelock','general');
select fn_db_add_config_value('SignCertTimeoutInSeconds','30','general');

