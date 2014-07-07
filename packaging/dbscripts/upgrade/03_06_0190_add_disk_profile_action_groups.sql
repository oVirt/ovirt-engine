-- add permissions to disk profile (according to existing permissions on storage).

------------------------------------------------
--- Update existing roles with new Action Groups
------------------------------------------------
-- Add ActionGroup 1560 (CONFIGURE_STORAGE_DISK_PROFILE) to any role which contains EDIT_STORAGE_DOMAIN_CONFIGURATION(601) or MANIPULATE_STORAGE_DOMAIN(603)
INSERT INTO roles_groups (role_id, action_group_id)
SELECT DISTINCT role_id, 1560
FROM roles_groups a
WHERE NOT EXISTS (SELECT 1
                      FROM roles_groups b
                      WHERE b.role_id = a.role_id
                      AND b.action_group_id = 1560)
    AND EXISTS (SELECT 1
                FROM roles_groups b
                WHERE b.role_id = a.role_id
                AND (b.action_group_id = 601 OR b.action_group_id = 603));

-- Add ActionGroup 1561 (CREATE_STORAGE_DISK_PROFILE) to any role which contains ActionGroup CREATE_STORAGE_DOMAIN(600)
INSERT INTO roles_groups (role_id, action_group_id)
SELECT DISTINCT role_id, 1561
FROM roles_groups a
WHERE NOT EXISTS (SELECT 1
                      FROM roles_groups b
                      WHERE b.role_id = a.role_id
                      AND b.action_group_id = 1561)
    AND EXISTS (SELECT 1
                FROM roles_groups b
                WHERE b.role_id = a.role_id
                AND b.action_group_id = 600);

-- Add ActionGroup 1562 (DELETE_STORAGE_DISK_PROFILE) to any role which contains ActionGroup DELETE_STORAGE_DOMAIN(602)
INSERT INTO roles_groups (role_id, action_group_id)
SELECT DISTINCT role_id, 1562
FROM roles_groups a
WHERE NOT EXISTS (SELECT 1
                      FROM roles_groups b
                      WHERE b.role_id = a.role_id
                      AND b.action_group_id = 1562)
    AND EXISTS (SELECT 1
                FROM roles_groups b
                WHERE b.role_id = a.role_id
                AND b.action_group_id = 602);


