

----------------------------------------------------------------
-- [images] Table
--




Create or replace FUNCTION InsertImage(
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_image_guid UUID,
    v_it_guid UUID,
    v_size BIGINT,
    v_ParentId UUID,
    v_imageStatus INTEGER ,
    v_lastModified TIMESTAMP WITH TIME ZONE,
    v_vm_snapshot_id UUID ,
    v_volume_type INTEGER,
    v_volume_format INTEGER,
    v_image_group_id UUID ,
    v_active BOOLEAN ,
    v_volume_classification SMALLINT)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO images(
        creation_date,
        image_guid,
        it_guid,
        size,
        ParentId,
        imageStatus,
        lastModified,
        vm_snapshot_id,
        volume_type,
        image_group_id,
        volume_format,
        active,
        volume_classification)
    VALUES(
        v_creation_date,
        v_image_guid,
        v_it_guid,
        v_size,
        v_ParentId,
        v_imageStatus,
        v_lastModified,
        v_vm_snapshot_id,
        v_volume_type,
        v_image_group_id,
        v_volume_format,
        v_active,
        v_volume_classification);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION UpdateImageStatus(
    v_image_id UUID,
    v_status INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE images
    SET    imageStatus = v_status
    WHERE  image_guid = v_image_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION UpdateStatusOfImagesByImageGroupId(
    v_image_group_id UUID,
    v_status INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE images
    SET    imageStatus = v_status
    WHERE  image_group_id = v_image_group_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION UpdateImageVmSnapshotId(
    v_image_id UUID,
    v_vm_snapshot_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE images
    SET    vm_snapshot_id = v_vm_snapshot_id
    WHERE  image_guid = v_image_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION UpdateImageSize(
    v_image_id UUID,
    v_size BIGINT,
    v_lastModified TIMESTAMP WITH TIME ZONE)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE images
    SET    size = v_size,
           lastModified = v_lastModified
    WHERE  image_guid = v_image_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION UpdateImage(
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_image_guid UUID,
    v_it_guid UUID,
    v_size BIGINT,
    v_ParentId UUID,
    v_imageStatus INTEGER ,
    v_lastModified TIMESTAMP WITH TIME ZONE,
    v_vm_snapshot_id UUID ,
    v_volume_type INTEGER,
    v_volume_format INTEGER,
    v_image_group_id UUID ,
    v_active BOOLEAN ,
    v_volume_classification SMALLINT)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE images
    SET    creation_date = v_creation_date,
           it_guid = v_it_guid,
           size = v_size,
           ParentId = v_ParentId,
           imageStatus = v_imageStatus,
           lastModified = v_lastModified,
           vm_snapshot_id = v_vm_snapshot_id,
           volume_type = v_volume_type,
           image_group_id = v_image_group_id,
           volume_format = v_volume_format,
           active = v_active,
           volume_classification = v_volume_classification,
           _update_date = LOCALTIMESTAMP
    WHERE  image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteImage(v_image_guid UUID)
RETURNS VOID
AS $procedure$
DECLARE
    v_val  UUID;
BEGIN
    DELETE
    FROM   images
    WHERE  image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromImages() RETURNS SETOF images STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   images;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetImageByImageId(v_image_guid UUID)
RETURNS SETOF images STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   images
    WHERE  image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;


