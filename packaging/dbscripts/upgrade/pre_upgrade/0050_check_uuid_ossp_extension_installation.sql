CREATE OR REPLACE FUNCTION __temp_check_uuid_ossp_extension_installation()
RETURNS void
AS $PROCEDURE$
BEGIN

    IF EXISTS (SELECT 1 from pg_available_extensions WHERE name = 'uuid-ossp' AND installed_version IS NULL) THEN
        RAISE EXCEPTION 'UUID generation function does not exist or is not valid,
                         Please run the following commands as a database administrator from pgsql prompt and retry running engine-setup again'
              USING DETAIL='DROP FUNCTION IF EXISTS uuid_generate_v1();
         CREATE EXTENSION "uuid-ossp";';

    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

SELECT __temp_check_uuid_ossp_extension_installation();

DROP FUNCTION __temp_check_uuid_ossp_extension_installation();


