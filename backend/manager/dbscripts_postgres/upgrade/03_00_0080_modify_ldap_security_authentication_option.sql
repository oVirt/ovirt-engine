-- Adjusts the LdapSecurityAuthentication vdc_options value
select fn_db_update_default_config_value('LDAPSecurityAuthentication','GSSAPI','default:GSSAPI','general',false);
select fn_db_update_default_config_value('LDAPSecurityAuthentication','SIMPLE','default:SIMPLE','general',false);

