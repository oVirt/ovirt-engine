-- update SignLockFile value to /var/lock/ovirt-engine/.openssl.exclusivelock
select fn_db_update_config_value('SignLockFile ','/var/lock/ovirt-engine/.openssl.exclusivelock','general');
