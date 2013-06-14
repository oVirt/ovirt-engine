-- Set utc_diff to 0 for Linux machines

UPDATE vm_dynamic AS vd
SET utc_diff = 0
FROM vm_static AS vs
WHERE vd.vm_guid = vs.vm_guid AND vs.os IN(5, 7, 8, 9, 13, 14, 15, 18, 19);

UPDATE vm_static
SET time_zone = NULL
WHERE os IN(5, 7, 8, 9, 13, 14, 15, 18, 19);
