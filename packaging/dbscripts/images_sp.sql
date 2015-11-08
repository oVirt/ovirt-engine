

----------------------------------------------------------------
-- [images] Table
--
CREATE OR REPLACE FUNCTION InsertImage (
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_image_guid UUID,
    v_it_guid UUID,
    v_size BIGINT,
    v_ParentId UUID,
    v_imageStatus INT,
    v_lastModified TIMESTAMP WITH TIME ZONE,
    v_vm_snapshot_id UUID,
    v_volume_type INT,
    v_volume_format INT,
    v_image_group_id UUID,
    v_active BOOLEAN,
    v_volume_classification SMALLINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO images (
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
        volume_classification
        )
    VALUES (
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
        v_volume_classification
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateImageStatus (
    v_image_id UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE images
    SET imageStatus = v_status
    WHERE image_guid = v_image_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStatusOfImagesByImageGroupId (
    v_image_group_id UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE images
    SET imageStatus = v_status
    WHERE image_group_id = v_image_group_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateImageVmSnapshotId (
    v_image_id UUID,
    v_vm_snapshot_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE images
    SET vm_snapshot_id = v_vm_snapshot_id
    WHERE image_guid = v_image_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateImageSize (
    v_image_id UUID,
    v_size BIGINT,
    v_lastModified TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE images
    SET size = v_size,
        lastModified = v_lastModified
    WHERE image_guid = v_image_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateImage (
    v_creation_date TIMESTAMP WITH TIME ZONE,
    v_image_guid UUID,
    v_it_guid UUID,
    v_size BIGINT,
    v_ParentId UUID,
    v_imageStatus INT,
    v_lastModified TIMESTAMP WITH TIME ZONE,
    v_vm_snapshot_id UUID,
    v_volume_type INT,
    v_volume_format INT,
    v_image_group_id UUID,
    v_active BOOLEAN,
    v_volume_classification SMALLINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE images
    SET creation_date = v_creation_date,
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
    WHERE image_guid = v_image_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteImage (v_image_guid UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM images
    WHERE image_guid = v_image_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromImages ()
RETURNS SETOF images STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM images;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetImageByImageId (v_image_guid UUID)
RETURNS SETOF images STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM images
    WHERE image_guid = v_image_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;


