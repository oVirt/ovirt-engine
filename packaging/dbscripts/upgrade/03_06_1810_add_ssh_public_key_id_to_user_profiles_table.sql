SELECT fn_db_add_column('user_profiles', 'ssh_public_key_id', 'uuid');

UPDATE user_profiles
SET ssh_public_key_id = uuid_generate_v1()
WHERE ssh_public_key_id = null;
