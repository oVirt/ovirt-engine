UPDATE images
SET lastmodified = creation_date
WHERE lastmodified is NULL;

ALTER TABLE images ALTER COLUMN lastmodified SET NOT NULL;