-- Inserts a link between quota role and quota action group only if missing
-- guid = consume_quota_role
-- number = consume quota action group
INSERT INTO roles_groups(role_id,action_group_id)
SELECT 'DEF0000a-0000-0000-0000-DEF00000000a', 901 WHERE
NOT EXISTS (SELECT role_id,action_group_id FROM roles_groups WHERE
role_id = 'DEF0000a-0000-0000-0000-DEF00000000a' AND action_group_id = 901);
