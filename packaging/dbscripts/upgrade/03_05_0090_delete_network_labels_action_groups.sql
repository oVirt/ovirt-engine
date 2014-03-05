-- Delete invalid action groups from existing roles
DELETE FROM roles_groups WHERE action_group_id in (163, 164, 165, 166);


