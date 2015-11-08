

/* ----------------------------------------------------------------
 Stored procedures for database operations on Storage Device
 related table: storage_device
----------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertStorageDevice (
    v_id UUID,
    v_name TEXT,
    v_device_uuid VARCHAR(38),
    v_filesystem_uuid VARCHAR(38),
    v_vds_id UUID,
    v_description TEXT,
    v_device_type VARCHAR(50),
    v_device_path TEXT,
    v_filesystem_type VARCHAR(50),
    v_mount_point TEXT,
    v_size BIGINT,
    v_is_free BOOLEAN,
    v_is_gluster_brick BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_device (
        id,
        name,
        device_uuid,
        filesystem_uuid,
        vds_id,
        description,
        device_type,
        device_path,
        filesystem_type,
        mount_point,
        size,
        is_free,
        is_gluster_brick
        )
    VALUES (
        v_id,
        v_name,
        v_device_uuid,
        v_filesystem_uuid,
        v_vds_id,
        v_description,
        v_device_type,
        v_device_path,
        v_filesystem_type,
        v_mount_point,
        v_size,
        v_is_free,
        v_is_gluster_brick
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStorageDevice (
    v_id UUID,
    v_name TEXT,
    v_device_uuid VARCHAR(38),
    v_filesystem_uuid VARCHAR(38),
    v_description TEXT,
    v_device_type VARCHAR(50),
    v_device_path TEXT,
    v_filesystem_type VARCHAR(50),
    v_mount_point TEXT,
    v_size BIGINT,
    v_is_free BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_device
    SET name = v_name,
        device_uuid = v_device_uuid,
        filesystem_uuid = v_filesystem_uuid,
        description = v_description,
        device_type = v_device_type,
        device_path = v_device_path,
        filesystem_type = v_filesystem_type,
        mount_point = v_mount_point,
        size = v_size,
        is_free = v_is_free,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageDeviceById (v_id UUID)
RETURNS SETOF storage_device STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_device
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageDevicesByVdsId (v_vds_id UUID)
RETURNS SETOF storage_device STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_device
    WHERE vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteStorageDeviceById (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM storage_device
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateIsFreeFlagById (
    v_id UUID,
    v_is_free BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_device
    SET is_free = v_is_free,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


