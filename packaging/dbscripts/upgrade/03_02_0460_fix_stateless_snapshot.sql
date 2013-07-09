UPDATE snapshots
SET snapshot_type = 'REGULAR'
WHERE description != 'stateless snapshot'
AND snapshot_type = 'STATELESS'
AND vm_id in (SELECT vm_guid from vm_pool_map);


