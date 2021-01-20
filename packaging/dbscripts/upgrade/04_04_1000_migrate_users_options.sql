

INSERT INTO user_profiles(
    property_id,
    user_id,
    property_name,
    property_content,
    property_type)
SELECT
    uuid_generate_v1(),
    users.user_id,
    user_option.key,
    user_option.value,
    'JSON'
FROM users
CROSS JOIN jsonb_each(users.options) as user_option
ON CONFLICT(user_id, property_name) DO NOTHING;


SELECT fn_db_drop_column('users','options');
