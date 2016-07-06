Create or replace FUNCTION InsertDiskVmElement(
    v_disk_id UUID,
    v_vm_id UUID,
    v_is_boot boolean,
    v_disk_interface VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO disk_vm_element (
        disk_id,
        vm_id,
        is_boot,
        disk_interface)
    VALUES (
        v_disk_id,
        v_vm_id,
        v_is_boot,
        v_disk_interface);
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION UpdateDiskVmElement(
    v_disk_id UUID,
    v_vm_id UUID,
    v_is_boot boolean,
    v_disk_interface VARCHAR(32))
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE disk_vm_element
    SET disk_id = v_disk_id,
        vm_id = v_vm_id,
        is_boot = v_is_boot,
        disk_interface = v_disk_interface
    WHERE disk_id = v_disk_id
        AND vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION DeleteDiskVmElement(
    v_disk_id UUID,
    v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM disk_vm_element
    WHERE disk_id = v_disk_id
        AND vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetAllFromDiskVmElement()
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended;
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetDiskVmElementByDiskVmElementId(
    v_disk_id UUID,
    v_vm_id UUID)
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended
    WHERE disk_id = v_disk_id
        AND vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetDiskVmElementsForVm(
    v_vm_id UUID)
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetDiskVmElementsPluggedToVm(
    v_vm_id UUID)
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended
    WHERE vm_id = v_vm_id AND is_plugged = true;
END;$PROCEDURE$
LANGUAGE plpgsql;
