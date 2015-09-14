CREATE OR REPLACE FUNCTION __temp_create_storage_server_connection_extension_table() RETURNS void AS $FUNCTION$
BEGIN
IF (NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name ILIKE 'storage_server_connection_extension')) THEN
  CREATE TABLE storage_server_connection_extension (
      id uuid NOT NULL,
      vds_id uuid NOT NULL,
      iqn character varying(128) NOT NULL,
      user_name text NOT NULL,
      password text NOT NULL
  );

  ALTER TABLE storage_server_connection_extension ADD CONSTRAINT pk_storage_server_connection_extension PRIMARY KEY (id);
  ALTER TABLE storage_server_connection_extension ADD CONSTRAINT storage_server_connection_extension_vds_id_iqn UNIQUE(vds_id, iqn);
END IF;
END; $FUNCTION$
LANGUAGE plpgsql;

SELECT  __temp_create_storage_server_connection_extension_table();
DROP FUNCTION __temp_create_storage_server_connection_extension_table();
