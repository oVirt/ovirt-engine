

----------------------------------------------------------------
-- [vdc_options] Table
--




Create or replace FUNCTION InsertVdcOption(v_option_name VARCHAR(50),
	v_option_value VARCHAR(50),
	v_version VARCHAR(40),
	INOUT v_option_id INTEGER)
   AS $procedure$
BEGIN
INSERT INTO vdc_options(OPTION_NAME, option_value, version)
	VALUES(v_option_name, v_option_value, v_version);

      v_option_id := CURRVAL('vdc_options_seq');
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVdcOption(v_option_name VARCHAR(50),
	v_option_value VARCHAR(50),
	v_option_id INTEGER,
	v_version VARCHAR(40))
RETURNS VOID

	--The [vdc_options] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vdc_options
      SET OPTION_NAME = v_option_name,option_value = v_option_value,version = v_version
      WHERE option_id = v_option_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVdcOption(v_option_id INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM vdc_options
      WHERE option_id = v_option_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromVdcOption() RETURNS SETOF vdc_options
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vdc_options.*
      FROM vdc_options;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdcOptionById(v_option_id INTEGER) RETURNS SETOF vdc_options
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vdc_options.*
      FROM vdc_options
      WHERE option_id = v_option_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVdcOptionByName(v_option_name VARCHAR(50),
	v_version VARCHAR(40)) RETURNS SETOF vdc_options
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vdc_options.*
      FROM vdc_options
      WHERE OPTION_NAME = v_option_name and version = v_version;
END; $procedure$
LANGUAGE plpgsql;


