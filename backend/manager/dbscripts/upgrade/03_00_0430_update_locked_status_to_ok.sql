-- Change status from LOCKED to OK after upgrade when no tasks exists.
UPDATE images
SET imagestatus = 1
WHERE imagestatus = 2
AND NOT EXISTS (SELECT 1 from async_tasks);
