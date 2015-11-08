

----------------------------------------------------------------
-- [user_profiles] Table
--
CREATE OR REPLACE FUNCTION InsertUserProfile (
    v_profile_id UUID,
    v_user_id UUID,
    v_ssh_public_key_id UUID,
    v_ssh_public_key TEXT,
    v_user_portal_vm_auto_login boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO user_profiles (
        profile_id,
        user_id,
        ssh_public_key_id,
        ssh_public_key,
        user_portal_vm_auto_login
        )
    VALUES (
        v_profile_id,
        v_user_id,
        v_ssh_public_key_id,
        v_ssh_public_key,
        v_user_portal_vm_auto_login
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateUserProfile (
    v_profile_id UUID,
    v_user_id UUID,
    v_ssh_public_key_id UUID,
    v_ssh_public_key TEXT,
    v_user_portal_vm_auto_login boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE user_profiles
    SET profile_id = v_profile_id,
        user_id = v_user_id,
        ssh_public_key_id = v_ssh_public_key_id,
        ssh_public_key = v_ssh_public_key,
        user_portal_vm_auto_login = v_user_portal_vm_auto_login
    WHERE profile_id = v_profile_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteUserProfile (v_profile_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM user_profiles
    WHERE profile_id = v_profile_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromUserProfiles ()
RETURNS SETOF user_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT user_profiles_view.*
    FROM user_profiles_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetUserProfileByUserId (v_user_id UUID)
RETURNS SETOF user_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM user_profiles_view
    WHERE user_id = v_user_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetUserProfileByProfileId (v_profile_id UUID)
RETURNS SETOF user_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM user_profiles_view
    WHERE profile_id = v_profile_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


