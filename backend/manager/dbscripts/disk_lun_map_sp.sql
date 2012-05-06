

----------------------------------------------------------------
-- [disk_lun_map] Table
--




Create or replace FUNCTION InsertDiskLunMap(
    v_disk_id UUID,
    v_lun_id VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO disk_lun_map(
        disk_id,
        lun_id)
    VALUES(
        v_disk_id,
        v_lun_id);
END; $procedure$
LANGUAGE plpgsql;








Create or replace FUNCTION DeleteDiskLunMap(v_disk_id UUID, v_lun_id VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   disk_lun_map
    WHERE  disk_id = v_disk_id
    AND    lun_id = v_lun_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromDiskLunMaps() RETURNS SETOF disk_lun_map
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   disk_lun_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetDiskLunMapByDiskLunMapId(v_disk_id UUID, v_lun_id VARCHAR(50))
RETURNS SETOF disk_lun_map
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   disk_lun_map
    WHERE  disk_id = v_disk_id
    AND    lun_id = v_lun_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetDiskLunMapByLunId(v_lun_id VARCHAR(50))
RETURNS SETOF disk_lun_map
AS $procedure$
BEGIN
	RETURN QUERY
    SELECT *
    FROM   disk_lun_map
    WHERE  lun_id = v_lun_id;
END; $procedure$
LANGUAGE plpgsql;


