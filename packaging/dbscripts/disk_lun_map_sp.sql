

----------------------------------------------------------------
-- [disk_lun_map] Table
--
CREATE OR REPLACE FUNCTION InsertDiskLunMap (
    v_disk_id UUID,
    v_lun_id VARCHAR(255)
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
    v_lun_id VARCHAR(255)
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
    v_lun_id VARCHAR(255)
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

CREATE OR REPLACE FUNCTION GetDiskLunMapByLunId (v_lun_id VARCHAR(255))
RETURNS SETOF disk_lun_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_lun_map
    WHERE lun_id = v_lun_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDiskLunMapByDiskId (v_disk_id UUID)
RETURNS SETOF disk_lun_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_lun_map
    WHERE disk_id = v_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDiskLunMapsForVmsInPool (v_storage_pool_id UUID)
RETURNS SETOF disk_lun_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_lun_map
    WHERE EXISTS (
        SELECT *
        FROM disk_vm_element
        INNER JOIN vm_static
            ON disk_vm_element.vm_id = vm_static.vm_guid
        INNER JOIN cluster
            ON vm_static.cluster_id = cluster.cluster_id
        INNER JOIN storage_pool
            ON cluster.storage_pool_id = storage_pool.id
        WHERE disk_lun_map.disk_id = disk_vm_element.disk_id
            AND storage_pool.id = v_storage_pool_id);
END;$PROCEDURE$
LANGUAGE plpgsql;
