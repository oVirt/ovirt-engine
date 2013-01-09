

----------------------------------------------------------------
-- [base_disks] Table
--




Create or replace FUNCTION InsertBaseDisk(
    v_disk_id UUID,
    v_disk_interface VARCHAR(32),
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors VARCHAR(32),
    v_disk_alias VARCHAR(50),
    v_disk_description VARCHAR(500),
    v_shareable BOOLEAN,
    v_boot BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO base_disks(
        disk_id,
        disk_interface,
        wipe_after_delete,
        propagate_errors,
        disk_alias,
        disk_description,
        shareable,
        boot)
    VALUES(
        v_disk_id,
        v_disk_interface,
        v_wipe_after_delete,
        v_propagate_errors,
        v_disk_alias,
        v_disk_description,
        v_shareable,
        v_boot);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateBaseDisk(
    v_disk_id UUID,
    v_disk_interface VARCHAR(32),
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors VARCHAR(32),
    v_disk_alias VARCHAR(50),
    v_disk_description VARCHAR(500),
    v_shareable BOOLEAN,
    v_boot BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE base_disks
    SET    disk_interface = v_disk_interface,
           wipe_after_delete = v_wipe_after_delete,
           propagate_errors = v_propagate_errors,
           disk_alias = v_disk_alias,
           disk_description = v_disk_description,
           shareable = v_shareable,
           boot = v_boot
    WHERE  disk_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteBaseDisk(v_disk_id UUID)
RETURNS VOID
AS $procedure$
DECLARE
    v_val  UUID;
BEGIN
    DELETE
    FROM   base_disks
    WHERE  disk_id = v_disk_id;

    -- Delete the disk's permissions
   DELETE FROM permissions WHERE object_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromBaseDisks() RETURNS SETOF base_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   base_disks;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetBaseDiskByBaseDiskId(v_disk_id UUID)
RETURNS SETOF base_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   base_disks
    WHERE  disk_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;


