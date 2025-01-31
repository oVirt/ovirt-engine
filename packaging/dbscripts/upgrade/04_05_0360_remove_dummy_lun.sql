-- Step 1: Copy the dummy LUN connections to all other LUNs of the same volume group
-- (attaching a connection to a domain maps it to every LUN of the domain)
INSERT INTO lun_storage_server_connection_map (lun_id, storage_server_connection)
SELECT l2.lun_id, ls.storage_server_connection
FROM lun_storage_server_connection_map ls
JOIN luns l1 ON l1.lun_id = ls.lun_id
JOIN luns l2 ON l2.volume_group_id = l1.volume_group_id
WHERE ls.lun_id LIKE 'DUMMY_LUN%'
  AND l1.lun_id != l2.lun_id
  AND NOT EXISTS (
      -- A dummy LUN can duplicate an already existing connection, filter these out.
      SELECT 1
      FROM lun_storage_server_connection_map ls2
      WHERE ls2.lun_id = l2.lun_id
      AND ls2.storage_server_connection = ls.storage_server_connection
  );

-- Step 2: Delete the dummy LUNs after copying their connections to the real LUNs;
-- the map rows must be deleted first as they reference luns
DELETE FROM lun_storage_server_connection_map
WHERE lun_id LIKE 'DUMMY_LUN%';

DELETE FROM luns
WHERE lun_id LIKE 'DUMMY_LUN%';
