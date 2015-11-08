

----------------------------------------------------------------
-- [disk_lun_map] Table
--
CREATE OR REPLACE FUNCTION InsertDiskLunMap (
    v_disk_id UUID,
    v_lun_id VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO disk_lun_map (
        disk_id,
        lun_id
        )
    VALUES (
        v_disk_id,
        v_lun_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteDiskLunMap (
    v_disk_id UUID,
    v_lun_id VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM disk_lun_map
    WHERE disk_id = v_disk_id
        AND lun_id = v_lun_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromDiskLunMaps ()
RETURNS SETOF disk_lun_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_lun_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDiskLunMapByDiskLunMapId (
    v_disk_id UUID,
    v_lun_id VARCHAR(50)
    )
RETURNS SETOF disk_lun_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_lun_map
    WHERE disk_id = v_disk_id
        AND lun_id = v_lun_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDiskLunMapByLunId (v_lun_id VARCHAR(50))
RETURNS SETOF disk_lun_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_lun_map
    WHERE lun_id = v_lun_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


