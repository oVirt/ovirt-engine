-- delete old key
select fn_db_delete_config_value('LdapServers','3.0');

-- add new key in the correct version
select fn_db_add_config_value('LdapServers','','general');

