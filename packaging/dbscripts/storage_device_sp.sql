/* ----------------------------------------------------------------
 Stored procedures for database operations on Storage Device
 related table: storage_device
----------------------------------------------------------------*/

Create or replace FUNCTION InsertStorageDevice(v_id UUID,
                                               v_name VARCHAR(1000),
                                               v_device_uuid VARCHAR(38),
                                               v_filesystem_uuid VARCHAR(38),
                                               v_vds_id UUID,
                                               v_description VARCHAR(2000),
                                               v_device_type VARCHAR(50),
                                               v_device_path VARCHAR(4096),
                                               v_filesystem_type VARCHAR(50),
                                               v_mount_point VARCHAR(4096),
                                               v_size BIGINT,
                                               v_is_free BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO storage_device(id, name, device_uuid, filesystem_uuid, vds_id,
    description, device_type, device_path, filesystem_type, mount_point, size, is_free)
    VALUES (v_id, v_name, v_device_uuid, v_filesystem_uuid, v_vds_id, v_description, v_device_type,
    v_device_path, v_filesystem_type, v_mount_point, v_size, v_is_free);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateStorageDevice(v_id UUID,
                                               v_name VARCHAR(1000),
                                               v_device_uuid VARCHAR(38),
                                               v_filesystem_uuid VARCHAR(38),
                                               v_description VARCHAR(2000),
                                               v_device_type VARCHAR(50),
                                               v_device_path VARCHAR(4096),
                                               v_filesystem_type VARCHAR(50),
                                               v_mount_point VARCHAR(4096),
                                               v_size BIGINT,
                                               v_is_free BOOLEAN)
RETURNS VOID
AS $procedure$
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
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetStorageDeviceById(v_id UUID)
RETURNS SETOF storage_device STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  storage_device
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetStorageDevicesByVdsId(v_vds_id UUID)
RETURNS SETOF storage_device STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  storage_device
    WHERE vds_id = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteStorageDeviceById(v_id UUID)
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM storage_device
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateIsFreeFlagById(v_id UUID,
                                                v_is_free BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE storage_device
    SET is_free = v_is_free,
    _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;
