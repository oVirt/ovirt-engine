-- This sets the MAC POOL USER role to have a user type role (role_type = 2) instead of admin role (role_type = 1)
UPDATE roles SET role_type = 2 WHERE id = 'def00014-0000-0000-0000-def000000014';
