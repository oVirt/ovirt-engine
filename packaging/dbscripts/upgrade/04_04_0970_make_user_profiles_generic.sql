-- Previous design:
-- each row in user_profiles is a logical profile (the primary key is based only on profile_id)
-- each row contains only SSH related data (1-to-1 relationship)
-- ssh_public_key_id is used as a second ID (REST API uses it as resource ID)
-- ssh_public_key_id should change if the content changes (from REST point of view resources are immutable)
-- DB design allows multiple profiles per user but this is restricted to one profile in Java
-- DB design prevents multiple SSH public key per profile

-- New design:
-- logical profile maps to 1..n rows in user_profiles table
-- profile is a virtual entity present by default - it cannot be deleted or created
-- new DB design allows only a single profile per user (before it was only limited in Java)
-- each row maps to a profile property with an unique(within profile) human readable name and a globally unique ID
-- if there are no properties then the profile is empty
-- (differently then before) DB design allows multiple SSH public keys per profile but limits it to single key in Java
-- columns have been renamed to be more generic and allow storing different types of data


-- property_type is used to mark content that requires special treatment  i.e. SSH keys require extra validation
-- properties containing SSH data are marked by SSH_PUBLIC_KEY
-- currently supported values(defined in Java enum): JSON, SSH_PUBLIC_KEY, UNKNOWN
SELECT fn_db_add_column('user_profiles', 'property_type', 'TEXT NOT NULL DEFAULT ''UNKNOWN''');
-- human readable name to be used as a map key
-- unique per profile
SELECT fn_db_add_column('user_profiles', 'property_name', 'TEXT NOT NULL DEFAULT ''UNKNOWN''');
-- replacement for ssh_public_key column (once migration is done)
-- all types of data is encoded as JSON
SELECT fn_db_add_column('user_profiles', 'property_content', 'JSONB');
-- rename to more generic name
SELECT fn_db_rename_column('user_profiles', 'ssh_public_key_id', 'property_id');

-- generate missing property IDs - unlikely but theoretically possible since
-- legacy ssh_public_key_id column allowed NULL values
UPDATE
    user_profiles
SET property_id = uuid_generate_v1()
WHERE
    property_id IS NULL OR property_id = '00000000-0000-0000-0000-000000000000';
ALTER TABLE user_profiles ALTER COLUMN property_id SET NOT NULL;


-- replace PK based on profile_id with property_id
ALTER TABLE user_profiles
  DROP CONSTRAINT pk_profile_id,
  ADD CONSTRAINT pk_property_id PRIMARY KEY(property_id);

-- mark all existing keys as SSH (no other usage is known)
-- use the type name itself as a default property name
UPDATE
    user_profiles
SET property_name = 'SSH_PUBLIC_KEY',
    property_type = 'SSH_PUBLIC_KEY';

-- enforce global uniqueness of (user_id, property_name) pair via index
-- legacy pk_profile_id was based on profile_id so only a single row per user
-- was possible - no need to check uniqueness before creating index
CREATE UNIQUE INDEX idx_user_profiles_property_name ON user_profiles (user_id, property_name);

-- remove empty SSH entries(soft deleted)
-- content was set to empty string(via REST) or NULL(via WebAdmin) to prevent losing profile ID
-- now this functionality is not needed as we have a default virtual profile
DELETE FROM
    user_profiles
WHERE
    ssh_public_key IS NULL OR
    ssh_public_key = '';

-- encode existing SSH public keys as JSON by wrapping in double quotes
-- some_public_key -> "some_public_key"
UPDATE
    user_profiles
SET property_content = to_json(ssh_public_key);


SELECT fn_db_drop_column('user_profiles','ssh_public_key');
SELECT fn_db_drop_column('user_profiles','profile_id');


