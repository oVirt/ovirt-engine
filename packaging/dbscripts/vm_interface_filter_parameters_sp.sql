----------------------------------------------------------------
-- [vm_interface_filter_parameters] Table
--
CREATE OR REPLACE FUNCTION GetVmInterfaceFilterParameterByVmInterfaceFilterParameterId (v_id UUID)
RETURNS SETOF vm_interface_filter_parameters STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface_filter_parameters.*
    FROM vm_interface_filter_parameters
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmInterfaceFilterParameters ()
RETURNS SETOF vm_interface_filter_parameters STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface_filter_parameters.*
    FROM vm_interface_filter_parameters;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfaceFilterParametersByVmInterfaceId (v_vm_interface_id UUID)
RETURNS SETOF vm_interface_filter_parameters STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface_filter_parameters.*
    FROM vm_interface_filter_parameters
    WHERE vm_interface_id = v_vm_interface_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmInterfaceFilterParameter (
    v_id UUID,
    v_name VARCHAR(255),
    v_value VARCHAR(255),
    v_vm_interface_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_interface_filter_parameters (
        id,
        name,
        value,
        vm_interface_id
        )
    VALUES (
        v_id,
        v_name,
        v_value,
        v_vm_interface_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmInterfaceFilterParameter (
    v_id UUID,
    v_name VARCHAR(255),
    v_value VARCHAR(255)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_interface_filter_parameters
    SET name = v_name,
        value = v_value
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmInterfaceFilterParameter (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM vm_interface_filter_parameters
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;
