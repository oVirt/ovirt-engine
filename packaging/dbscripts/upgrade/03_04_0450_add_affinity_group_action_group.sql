-- Add manipulate affinity action group (1550) to predefined roles
-- Super user
INSERT INTO roles_groups(role_id,action_group_id) VALUES('00000000-0000-0000-0000-000000000001', 1550);
-- DC admin role
INSERT INTO roles_groups(role_id,action_group_id) VALUES('DEF00002-0000-0000-0000-DEF000000002', 1550);
-- Cluster admin role
INSERT INTO roles_groups(role_id,action_group_id) VALUES('DEF00001-0000-0000-0000-DEF000000001', 1550);
