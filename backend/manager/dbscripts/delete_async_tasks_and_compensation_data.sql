truncate table  async_tasks;

truncate table business_entity_snapshot;

-- Change status from LOCKED to OK after upgrade since the tasks will be cacelled.
UPDATE images
SET imagestatus = 1
WHERE imagestatus = 2;



