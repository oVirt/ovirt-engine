ALTER TABLE ONLY image_transfers ADD CONSTRAINT fk_image_transfers_command_enitites FOREIGN KEY (command_id) REFERENCES command_entities(command_id) ON DELETE CASCADE;
