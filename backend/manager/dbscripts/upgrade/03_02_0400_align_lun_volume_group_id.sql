UPDATE luns SET volume_group_id = '' WHERE volume_group_id IS NULL;
ALTER TABLE luns ALTER COLUMN volume_group_id SET NOT NULL;
