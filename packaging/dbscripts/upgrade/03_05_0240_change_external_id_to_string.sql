ALTER TABLE users ALTER COLUMN external_id DROP DEFAULT;
SELECT fn_db_change_column_type('users', 'external_id', 'bytea', 'text');
ALTER TABLE users ALTER COLUMN external_id SET NOT NULL;
ALTER TABLE ad_groups ALTER COLUMN external_id DROP DEFAULT;
SELECT fn_db_change_column_type('ad_groups', 'external_id', 'bytea', 'text');
ALTER TABLE ad_groups ALTER COLUMN external_id SET NOT NULL;
--In previous scripts the PK of ad_groups and users
-- was set based on external_id
--In this script, external_id will be set as
--String representation of the PK
UPDATE ad_groups SET external_id = CAST( id AS text );
UPDATE users SET external_id = CAST (user_id as text );
