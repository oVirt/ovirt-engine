

----------------------------------------------------------------
-- [user_profiles] Table
--
CREATE OR REPLACE FUNCTION InsertUserProfileProperty (
    v_user_id UUID,
    v_property_id UUID,
    v_property_name TEXT,
    v_property_type TEXT,
    v_property_content TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO user_profiles (
        user_id,
        property_id,
        property_name,
        property_type,
        property_content
        )
    VALUES (
        v_user_id,
        v_property_id,
        v_property_name,
        v_property_type,
        v_property_content::jsonb
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateUserProfileProperty (
    v_user_id UUID,
    v_property_id UUID,
    v_property_name TEXT,
    v_property_type TEXT,
    v_property_content TEXT,
    v_new_property_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE user_profiles
    -- update is valid only if provided property_id matches the ID
    -- currently stored in DB (for the property with the same name)
    -- otherwise NULL property_id will violate the PK constraint and prevent update
    -- the property_id should change each time the content changes
    SET property_id = CASE WHEN property_id = v_property_id THEN v_new_property_id ELSE NULL END,
        property_content = v_property_content::jsonb
    WHERE
        user_id = v_user_id AND
        property_name = v_property_name AND
        property_type = v_property_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteUserProfileProperty (v_property_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM user_profiles
    WHERE property_id = v_property_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllPublicSshKeysFromUserProfiles ()
RETURNS SETOF user_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT user_profiles_view.*
    FROM user_profiles_view
    WHERE user_profiles_view.property_type = 'SSH_PUBLIC_KEY';
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

CREATE OR REPLACE FUNCTION GetUserProfileProperty (v_property_id UUID)
RETURNS SETOF user_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM user_profiles_view
    WHERE property_id = v_property_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

