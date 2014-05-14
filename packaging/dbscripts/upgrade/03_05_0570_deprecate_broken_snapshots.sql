-- Set images in BROKEN snapshots to ILLEGAL
UPDATE images
SET imagestatus = 4
FROM snapshots
WHERE images.vm_snapshot_id = snapshots.snapshot_id
AND snapshots.status = 'BROKEN';

-- Set BROKEN snapshots to OK (as BROKEN status is deprecated)
UPDATE snapshots
SET status = 'OK'
WHERE status = 'BROKEN';