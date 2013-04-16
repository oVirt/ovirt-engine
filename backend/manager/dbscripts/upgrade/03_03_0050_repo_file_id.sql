-- The current file names from now on will be used as images id in the ISO domains
select fn_db_rename_column('repo_file_meta_data', 'repo_file_name', 'repo_image_id');
select fn_db_add_column('repo_file_meta_data', 'repo_image_name', 'varchar(256)');
