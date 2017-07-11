

----------------------------------------------------------------------
--  Disk Profiles
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetDiskProfileByDiskProfileId (v_id UUID)
RETURNS SETOF disk_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_profiles
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertDiskProfile (
    v_id UUID,
    v_name VARCHAR(50),
    v_storage_domain_id UUID,
    v_qos_id UUID,
    v_description TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO disk_profiles (
        id,
        name,
        storage_domain_id,
        qos_id,
        description
        )
    VALUES (
        v_id,
        v_name,
        v_storage_domain_id,
        v_qos_id,
        v_description
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateDiskProfile (
    v_id UUID,
    v_name VARCHAR(50),
    v_storage_domain_id UUID,
    v_qos_id UUID,
    v_description TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE disk_profiles
    SET id = v_id,
        name = v_name,
        storage_domain_id = v_storage_domain_id,
        qos_id = v_qos_id,
        description = v_description,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteDiskProfile (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM disk_profiles
    WHERE id = v_id;

    -- Delete the disk profiles permissions
    PERFORM DeletePermissionsByEntityId(v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromDiskProfiles ()
RETURNS SETOF disk_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_profiles;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDiskProfilesByStorageDomainId (
    v_storage_domain_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF disk_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_profiles
    WHERE storage_domain_id = v_storage_domain_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_disk_profile_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = disk_profiles.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION nullifyQosForStorageDomain (v_storage_domain_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE disk_profiles
    SET qos_id = NULL
    WHERE storage_domain_id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDiskProfilesByQosId (v_qos_id UUID)
RETURNS SETOF disk_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM disk_profiles
    WHERE qos_id = v_qos_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


