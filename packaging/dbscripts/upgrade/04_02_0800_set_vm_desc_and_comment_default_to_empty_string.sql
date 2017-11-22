UPDATE vm_static SET description = '' WHERE description IS NULL;
UPDATE vm_static SET free_text_comment = '' WHERE free_text_comment IS NULL;

ALTER TABLE vm_static ALTER COLUMN description SET DEFAULT '';
ALTER TABLE vm_static ALTER COLUMN free_text_comment SET DEFAULT '';

