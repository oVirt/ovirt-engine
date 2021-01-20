


----------------------------------------------------------------
-- [users] Table
--
CREATE OR REPLACE FUNCTION InsertUser (
    v_department VARCHAR(255),
    v_domain VARCHAR(255),
    v_email VARCHAR(255),
    v_name VARCHAR(255),
    v_note VARCHAR(255),
    v_surname VARCHAR(255),
    v_user_id UUID,
    v_username VARCHAR(255),
    v_external_id TEXT,
    v_namespace VARCHAR(2048)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO users (
        department,
        domain,
        email,
        name,
        note,
        surname,
        user_id,
        username,
        external_id,
        namespace
        )
    VALUES (
        v_department,
        v_domain,
        v_email,
        v_name,
        v_note,
        v_surname,
        v_user_id,
        v_username,
        v_external_id,
        v_namespace
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateUserImpl (
    v_department VARCHAR(255),
    v_domain VARCHAR(255),
    v_email VARCHAR(255),
    v_name VARCHAR(255),
    v_note VARCHAR(255),
    v_surname VARCHAR(255),
    v_user_id UUID,
    v_username VARCHAR(255),
    v_external_id TEXT,
    v_namespace VARCHAR(2048)
    )
RETURNS INT
    --The [users] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
DECLARE updated_rows INT;

BEGIN
    UPDATE users
    SET department = v_department,
        domain = v_domain,
        email = v_email,
        name = v_name,
        note = v_note,
        surname = v_surname,
        username = v_username,
        external_id = v_external_id,
        namespace = v_namespace,
        _update_date = CURRENT_TIMESTAMP
    WHERE external_id = v_external_id
        AND domain = v_domain;

    GET DIAGNOSTICS updated_rows = ROW_COUNT;

    RETURN updated_rows;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateUser (
    v_department VARCHAR(255),
    v_domain VARCHAR(255),
    v_email VARCHAR(255),
    v_name VARCHAR(255),
    v_note VARCHAR(255),
    v_surname VARCHAR(255),
    v_user_id UUID,
    v_username VARCHAR(255),
    v_last_admin_check_status BOOLEAN,
    v_external_id TEXT,
    v_namespace VARCHAR(2048)
    )
RETURNS VOID
    --The [users] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    PERFORM UpdateUserImpl(
        v_department,
        v_domain,
        v_email,
        v_name,
        v_note,
        v_surname,
        v_user_id,
        v_username,
        v_external_id,
        v_namespace);

    UPDATE users
    SET last_admin_check_status = v_last_admin_check_status
    WHERE domain = v_domain
        AND external_id = v_external_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertOrUpdateUser (
    v_department VARCHAR(255),
    v_domain VARCHAR(255),
    v_email VARCHAR(255),
    v_name VARCHAR(255),
    v_note VARCHAR(255),
    v_surname VARCHAR(255),
    v_user_id UUID,
    v_username VARCHAR(255),
    v_external_id TEXT,
    v_namespace VARCHAR(2048)
    )
RETURNS VOID AS $PROCEDURE$
DECLARE updated_rows INT;

BEGIN
    SELECT UpdateUserImpl(
        v_department,
        v_domain,
        v_email,
        v_name,
        v_note,
        v_surname,
        v_user_id,
        v_username,
        v_external_id,
        v_namespace)
    INTO updated_rows;

    IF (updated_rows = 0) THEN
        PERFORM InsertUser(
            v_department,
            v_domain,
            v_email,
            v_name,
            v_note,
            v_surname,
            v_user_id,
            v_username,
            v_external_id,
            v_namespace);
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION DeleteUser (v_user_id UUID)
    RETURNS VOID AS $PROCEDURE$

    DECLARE v_val UUID;

    BEGIN
        -- Get (and keep) a shared lock with "right to upgrade to exclusive"
        -- in order to force locking parent before children
        SELECT user_id
        INTO v_val
        FROM users
        WHERE user_id = v_user_id
        FOR

        UPDATE;

        DELETE
        FROM tags_user_map
        WHERE user_id = v_user_id;

        DELETE
        FROM users
        WHERE user_id = v_user_id;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION GetAllFromUsers (
        v_user_id UUID,
        v_is_filtered BOOLEAN
        )
    RETURNS SETOF users STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT users.*
        FROM users
        WHERE (
                NOT v_is_filtered
                OR EXISTS (
                    SELECT 1
                    FROM users u,
                        user_db_users_permissions_view p
                    WHERE u.user_id = v_user_id
                        AND u.user_id = p.ad_element_id
                    )
                );
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION GetUserByUserId (
        v_user_id UUID,
        v_is_filtered BOOLEAN
        )
    RETURNS SETOF users STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT users.*
        FROM users
        WHERE user_id = v_user_id
            AND (
                NOT v_is_filtered
                OR EXISTS (
                    SELECT 1
                    FROM users u,
                        user_db_users_permissions_view p
                    WHERE u.user_id = v_user_id
                        AND u.user_id = p.ad_element_id
                    )
                );
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION GetUserByExternalId (
        v_domain VARCHAR(255),
        v_external_id TEXT
        )
    RETURNS SETOF users STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT users.*
        FROM users
        WHERE domain = v_domain
            AND external_id = v_external_id;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION GetUserByUserNameAndDomain (
        v_username VARCHAR(255),
        v_domain VARCHAR(255)
        )
    RETURNS SETOF users STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT users.*
        FROM users
        WHERE username = v_username
            AND domain = v_domain;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION GetUsersByVmGuid (v_vm_guid UUID)
    RETURNS SETOF users STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT users.*
        FROM users
        INNER JOIN permissions
            ON users.user_id = permissions.ad_element_id
        WHERE permissions.object_type_id = 2
            AND permissions.object_id = v_vm_guid;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE OR REPLACE FUNCTION GetUsersByTemplateGuid (v_template_guid UUID)
    RETURNS SETOF users STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT users.*
        FROM users
            INNER JOIN permissions
                ON users.user_id = permissions.ad_element_id
        WHERE permissions.object_type_id = 4
            AND permissions.object_id = v_template_guid;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION UpdateLastAdminCheckStatus (v_userIds VARCHAR(4000))
    RETURNS VOID AS $PROCEDURE$

    DECLARE v_id UUID;

    v_tempId VARCHAR(4000);

    myCursor CURSOR
    FOR

    SELECT id
    FROM fnSplitter(v_userIds);

    v_result INT;

    BEGIN
        -- get users and its groups
        -- get their permission based on ad_element_id.
        -- if one permissions role's type is ADMIN(1) THEN set the user last_admin_check_status to 1
        OPEN myCursor;

        FETCH myCursor
        INTO v_tempId;

        WHILE FOUND LOOP v_id := CAST(v_tempId AS UUID);
            SELECT count(*)
            INTO v_result
            FROM users
            WHERE user_id IN (
                    SELECT ad_element_id AS user_id
                    FROM permissions,
                        roles
                    WHERE permissions.role_id = roles.id
                        AND ad_element_id IN (
                            (
                                SELECT ad_groups.id
                                FROM ad_groups,
                                    engine_sessions
                                WHERE engine_sessions.user_id = v_id
                                    AND ad_groups.id IN (
                                        SELECT *
                                        FROM fnsplitteruuid(engine_sessions.group_ids)
                                        )

                                UNION

                                SELECT v_id
                                )
                            )
                        AND (
                            roles.role_type = 1
                            OR permissions.role_id = '00000000-0000-0000-0000-000000000001'
                            )
                    );

        UPDATE users
        SET last_admin_check_status = CASE
                WHEN v_result = 0
                    THEN FALSE
                ELSE TRUE
                END
        WHERE user_id = v_id;

        FETCH myCursor
        INTO v_tempId;
    END LOOP;


    CLOSE myCursor;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSessionUserAndGroupsById (
    v_id UUID,
    v_engine_session_seq_id INT
    )
RETURNS SETOF idUuidType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT ad_groups.ID
    FROM ad_groups,
        engine_sessions
    WHERE engine_sessions.id = v_engine_session_seq_id
        AND ad_groups.id IN (
            SELECT *
            FROM fnsplitteruuid(engine_sessions.group_ids)
            )

    UNION

    SELECT v_id

    UNION

    -- user is also member of 'Everyone'
    SELECT 'EEE00000-0000-0000-0000-123456789EEE';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetUsersByTemplateGuid (v_template_guid UUID)
RETURNS SETOF users STABLE AS $PROCEDURE$

BEGIN
    RETURN QUERY

    SELECT users.*
    FROM users
        INNER JOIN permissions
            ON users.user_id = permissions.ad_element_id
    WHERE permissions.object_type_id = 4
        AND permissions.object_id = v_template_guid;
END;$PROCEDURE$

LANGUAGE plpgsql;
