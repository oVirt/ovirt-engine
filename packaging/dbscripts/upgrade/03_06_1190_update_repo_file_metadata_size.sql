ALTER TABLE repo_file_meta_data ALTER COLUMN size SET DEFAULT NULL;

UPDATE repo_file_meta_data
SET    size = NULL
WHERE  size = 0;