





----------------------------------------------------------------
-- [user_profiles] Table
--





Create or replace FUNCTION InsertUserProfile(
    v_profile_id UUID,
    v_user_id UUID,
    v_ssh_public_key TEXT)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO user_profiles (
        profile_id,
        user_id,
        ssh_public_key)
    VALUES(
        v_profile_id,
        v_user_id,
        v_ssh_public_key);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateUserProfile(
    v_profile_id UUID,
    v_user_id UUID,
    v_ssh_public_key TEXT)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE user_profiles
    SET    profile_id = v_profile_id,
           user_id = v_user_id,
           ssh_public_key = v_ssh_public_key
    WHERE  profile_id = v_profile_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteUserProfile(v_profile_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   user_profiles
    WHERE  profile_id = v_profile_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromUserProfiles() RETURNS SETOF user_profiles STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT user_profiles.*
      FROM user_profiles;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetUserProfileByUserId(v_user_id UUID)
RETURNS SETOF user_profiles STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   user_profiles
    WHERE  user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetUserProfileByProfileId(v_profile_id UUID)
RETURNS SETOF user_profiles STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   user_profiles
    WHERE  profile_id = v_profile_id;
END; $procedure$
LANGUAGE plpgsql;
