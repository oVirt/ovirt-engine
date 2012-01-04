

----------------------------------------------------------------
-- [disks] Table
--




Create or replace FUNCTION InsertDisk(
    v_disk_id UUID,
    v_internal_drive_mapping INTEGER,
    v_disk_type VARCHAR(32),
    v_disk_interface VARCHAR(32),
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO disks(
        disk_id,
        internal_drive_mapping,
        disk_type,
        disk_interface,
        wipe_after_delete,
        propagate_errors)
    VALUES(
        v_disk_id,
        v_internal_drive_mapping,
        v_disk_type,
        v_disk_interface,
        v_wipe_after_delete,
        v_propagate_errors);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateDisk(
    v_disk_id UUID,
    v_internal_drive_mapping INTEGER,
    v_disk_type VARCHAR(32),
    v_disk_interface VARCHAR(32),
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE disks
    SET    internal_drive_mapping = v_internal_drive_mapping,
           disk_type = v_disk_type,
           disk_interface = v_disk_interface,
           wipe_after_delete = v_wipe_after_delete,
           propagate_errors = v_propagate_errors
    WHERE  disk_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteDisk(v_disk_id UUID)
RETURNS VOID
AS $procedure$
DECLARE
    v_val  UUID;
BEGIN
    DELETE
    FROM   disks
    WHERE  disk_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromDisks() RETURNS SETOF disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   disks;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetDiskByDiskId(v_disk_id UUID)
RETURNS SETOF disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   disks
    WHERE  disk_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;


