-- Updating VnicProfileUser to allow viewing children.
UPDATE roles
SET allows_viewing_children = true
WHERE name = 'VnicProfileUser';

