UPDATE snapshots
SET memory_volume = hibernation_vol_handle
FROM vm_dynamic
WHERE snapshot_type = 'ACTIVE' AND vm_guid = vm_id AND length(hibernation_vol_handle) != 0;

SELECT fn_db_drop_column('vm_dynamic', 'hibernation_vol_handle');

