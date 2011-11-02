CREATE OR REPLACE FUNCTION upgrade_remove_default_security_auth_03_00_0310(a_input VARCHAR(40))
  RETURNS void AS
$BODY$
   DECLARE
   v_entry VARCHAR(4000);
   v_pos integer;
BEGIN
    v_entry := option_value FROM vdc_options WHERE option_name='LDAPSecurityAuthentication';
    v_pos := strpos(lower(v_entry), ',' || lower(a_input) || ',');

    IF (v_pos = 0) THEN
                UPDATE vdc_options
                SET option_value = regexp_replace(option_value, ',?' || a_input || ',?' ,'','i')
                WHERE option_name = 'LDAPSecurityAuthentication';
    ELSE
                UPDATE vdc_options
                SET option_value = regexp_replace(option_value, ',' || a_input || ',' ,',','i')
                WHERE option_name = 'LDAPSecurityAuthentication';
    END IF;

END; $BODY$
LANGUAGE plpgsql;

SELECT upgrade_remove_default_security_auth_03_00_0310('default:GSSAPI');
SELECT upgrade_remove_default_security_auth_03_00_0310('default:SIMPLE');

DROP FUNCTION upgrade_remove_default_security_auth_03_00_0310(VARCHAR);
