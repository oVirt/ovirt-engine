

----------------------------------------------------------------
-- [all_disks] View
--






Create or replace FUNCTION GetAllFromDisks() RETURNS SETOF all_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   all_disks;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetDiskByDiskId(v_disk_id UUID)
RETURNS SETOF all_disks
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   all_disks
    WHERE  image_group_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;


