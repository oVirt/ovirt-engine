Create or replace FUNCTION __temp_preserve_network_user_role_id()
RETURNS VOID
AS $procedure$
DECLARE
    v_VNIC_PROFILE_USER_ID UUID;
    v_NETWORK_USER_ID UUID;

BEGIN
    v_VNIC_PROFILE_USER_ID := 'DEF00020-0000-0000-0000-DEF000000010';
    v_NETWORK_USER_ID := 'DEF0000A-0000-0000-0000-DEF000000010';

------------------------------------------------------
-- Update VnicProfileUser roleId to NetworkUser roleId
------------------------------------------------------
    ALTER TABLE roles_groups DROP CONSTRAINT fk_roles_groups_action_id;
    ALTER TABLE permissions DROP CONSTRAINT fk_permissions_roles;

    UPDATE roles
    SET id = v_NETWORK_USER_ID
    WHERE id = v_VNIC_PROFILE_USER_ID;

    UPDATE permissions
    SET role_id = v_NETWORK_USER_ID
    WHERE role_id = v_VNIC_PROFILE_USER_ID;

    UPDATE roles_groups
    SET role_id = v_NETWORK_USER_ID
    WHERE role_id = v_VNIC_PROFILE_USER_ID;

    ALTER TABLE roles_groups ADD CONSTRAINT fk_roles_groups_action_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;
    ALTER TABLE permissions ADD CONSTRAINT fk_permissions_roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_preserve_network_user_role_id();
DROP FUNCTION __temp_preserve_network_user_role_id();
