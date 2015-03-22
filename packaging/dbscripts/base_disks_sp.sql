

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
    v_boot BOOLEAN,
    v_sgio INTEGER,
    v_alignment SMALLINT,
    v_last_alignment_scan TIMESTAMP WITH TIME ZONE,
    v_disk_storage_type SMALLINT)
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
        boot,
        sgio,
        alignment,
        last_alignment_scan,
        disk_storage_type)
    VALUES(
        v_disk_id,
        v_disk_interface,
        v_wipe_after_delete,
        v_propagate_errors,
        v_disk_alias,
        v_disk_description,
        v_shareable,
        v_boot,
        v_sgio,
        v_alignment,
        v_last_alignment_scan,
        v_disk_storage_type);
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
    v_boot BOOLEAN,
    v_sgio INTEGER,
    v_alignment SMALLINT,
    v_last_alignment_scan TIMESTAMP WITH TIME ZONE,
    v_disk_storage_type SMALLINT)
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
           boot = v_boot,
           sgio = v_sgio,
           alignment = v_alignment,
           last_alignment_scan = v_last_alignment_scan,
           disk_storage_type = v_disk_storage_type
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





Create or replace FUNCTION GetAllFromBaseDisks() RETURNS SETOF base_disks STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   base_disks;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetBaseDiskByBaseDiskId(v_disk_id UUID)
RETURNS SETOF base_disks STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   base_disks
    WHERE  disk_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;


