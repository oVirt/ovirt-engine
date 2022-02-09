


----------------------------------------------------------------
-- [vdc_options] Table
--
CREATE OR REPLACE FUNCTION InsertVdcOption (
    v_option_name VARCHAR(50),
    v_option_value TEXT,
    v_default_value TEXT,
    v_version VARCHAR(40),
    INOUT v_option_id INT
    ) AS $PROCEDURE$
BEGIN
    INSERT INTO vdc_options (
        option_name,
        option_value,
	default_value,
        version
        )
    VALUES (
        v_option_name,
        v_option_value,
	v_default_value,
        v_version
        );

    v_option_id := CURRVAL('vdc_options_seq');
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdcOption (
    v_option_name VARCHAR(50),
    v_option_value TEXT,
    v_option_id INT,
    v_version VARCHAR(40)
    )
RETURNS VOID
    --The [vdc_options] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vdc_options
    SET option_name = v_option_name,
        option_value = v_option_value,
        version = v_version
    WHERE option_id = v_option_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVdcOption (v_option_id INT)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vdc_options
    WHERE option_id = v_option_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVdcOption ()
RETURNS SETOF vdc_options STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vdc_options.*
    FROM vdc_options;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdcOptionById (v_option_id INT)
RETURNS SETOF vdc_options STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vdc_options.*
    FROM vdc_options
    WHERE option_id = v_option_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdcOptionByName (
    v_option_name VARCHAR(50),
    v_version VARCHAR(40)
    )
RETURNS SETOF vdc_options STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vdc_options.*
    FROM vdc_options
    WHERE OPTION_name = v_option_name
        AND version = v_version;
END;$PROCEDURE$
LANGUAGE plpgsql;


