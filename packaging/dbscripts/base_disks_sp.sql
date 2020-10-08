

----------------------------------------------------------------
-- [base_disks] Table
--
CREATE OR REPLACE FUNCTION InsertBaseDisk (
    v_disk_id UUID,
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors VARCHAR(32),
    v_disk_alias VARCHAR(50),
    v_disk_description VARCHAR(500),
    v_shareable BOOLEAN,
    v_sgio INT,
    v_disk_storage_type SMALLINT,
    v_cinder_volume_type VARCHAR(255),
    v_disk_content_type SMALLINT,
    v_backup VARCHAR(32),
    v_backup_mode VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO base_disks (
        disk_id,
        wipe_after_delete,
        propagate_errors,
        disk_alias,
        disk_description,
        shareable,
        sgio,
        disk_storage_type,
        cinder_volume_type,
        disk_content_type,
        backup,
        backup_mode
        )
    VALUES (
        v_disk_id,
        v_wipe_after_delete,
        v_propagate_errors,
        v_disk_alias,
        v_disk_description,
        v_shareable,
        v_sgio,
        v_disk_storage_type,
        v_cinder_volume_type,
        v_disk_content_type,
        v_backup,
        v_backup_mode
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateBaseDisk (
    v_disk_id UUID,
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors VARCHAR(32),
    v_disk_alias VARCHAR(50),
    v_disk_description VARCHAR(500),
    v_shareable BOOLEAN,
    v_sgio INT,
    v_disk_storage_type SMALLINT,
    v_cinder_volume_type VARCHAR(255),
    v_disk_content_type SMALLINT,
    v_backup VARCHAR(32),
    v_backup_mode VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE base_disks
    SET wipe_after_delete = v_wipe_after_delete,
        propagate_errors = v_propagate_errors,
        disk_alias = v_disk_alias,
        disk_description = v_disk_description,
        shareable = v_shareable,
        sgio = v_sgio,
        disk_storage_type = v_disk_storage_type,
        cinder_volume_type = v_cinder_volume_type,
        disk_content_type = v_disk_content_type,
        backup = v_backup,
        backup_mode = v_backup_mode
    WHERE disk_id = v_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteBaseDisk (v_disk_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM base_disks
    WHERE disk_id = v_disk_id;

    -- Delete the disk's permissions
    PERFORM DeletePermissionsByEntityId(v_disk_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromBaseDisks ()
RETURNS SETOF base_disks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM base_disks;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBaseDiskByBaseDiskId (v_disk_id UUID)
RETURNS SETOF base_disks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM base_disks
    WHERE disk_id = v_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


Create or replace FUNCTION GetBaseDisksByAlias(v_disk_alias varchar(255))
RETURNS SETOF base_disks STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   base_disks
    WHERE  disk_alias = v_disk_alias;
END; $procedure$
LANGUAGE plpgsql;


