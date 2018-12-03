-- Drop fk_image_transfers_command_enitites constraint if exists (for upgrading from engine >4.2.5)
ALTER TABLE ONLY image_transfers DROP CONSTRAINT IF EXISTS fk_image_transfers_command_enitites;
