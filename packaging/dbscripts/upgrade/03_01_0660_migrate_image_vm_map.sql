
ALTER TABLE images ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE images i
SET    active = TRUE
WHERE  EXISTS (
    SELECT 1
    FROM   image_vm_map ivm
    WHERE  ivm.image_id = i.image_guid
    AND    ivm.active = TRUE);

DROP TABLE image_vm_map CASCADE;

