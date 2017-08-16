UPDATE repo_file_meta_data SET file_type = DEFAULT WHERE file_type IS NULL;
ALTER TABLE repo_file_meta_data ALTER COLUMN file_type SET NOT NULL;
