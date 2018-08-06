-- Delete stale image_transfers records which don't have correlate records in command_entities
-- (this is done to avoid a foreign key violation when adding the constraint).
DELETE FROM image_transfers
WHERE command_id NOT IN
    (SELECT    command_entities.command_id
     FROM      command_entities);

ALTER TABLE ONLY image_transfers ADD CONSTRAINT fk_image_transfers_command_enitites FOREIGN KEY (command_id) REFERENCES command_entities(command_id) ON DELETE CASCADE;
