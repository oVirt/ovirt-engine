Create or replace FUNCTION InsertDiskVmElement(
    v_disk_id UUID,
    v_vm_id UUID,
    v_is_boot boolean,
    v_pass_discard boolean,
    v_disk_interface VARCHAR(32),
    v_is_using_scsi_reservation boolean)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO disk_vm_element (
        disk_id,
        vm_id,
        is_boot,
        pass_discard,
        disk_interface,
        is_using_scsi_reservation)
    VALUES (
        v_disk_id,
        v_vm_id,
        v_is_boot,
        v_pass_discard,
        v_disk_interface,
        v_is_using_scsi_reservation);
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION UpdateDiskVmElement(
    v_disk_id UUID,
    v_vm_id UUID,
    v_is_boot boolean,
    v_pass_discard boolean,
    v_disk_interface VARCHAR(32),
    v_is_using_scsi_reservation boolean)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE disk_vm_element
    SET disk_id = v_disk_id,
        vm_id = v_vm_id,
        is_boot = v_is_boot,
        pass_discard = v_pass_discard,
        disk_interface = v_disk_interface,
        is_using_scsi_reservation = v_is_using_scsi_reservation
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



CREATE OR REPLACE FUNCTION GetAllFromDiskVmElements()
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended;
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetDiskVmElementByDiskVmElementId(
    v_disk_id UUID,
    v_vm_id UUID,
    v_user_id UUID,
    v_is_filtered boolean)
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended
    WHERE disk_id = v_disk_id
        AND vm_id = v_vm_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetDiskVmElementsByDiskVmElementsIds(
    v_disks_ids UUID[])
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended
    WHERE disk_id = ANY(v_disks_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetDiskVmElementsForVm(
    v_vm_id UUID,
    v_user_id UUID,
    v_is_filtered boolean)
RETURNS SETOF disk_vm_element_extended STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM disk_vm_element_extended
    WHERE vm_id = v_vm_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            );
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
