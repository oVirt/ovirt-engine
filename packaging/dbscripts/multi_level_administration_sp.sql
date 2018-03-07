


----------------------------------------------------------------
-- [permissions] Table
--
CREATE OR REPLACE FUNCTION InsertPermission (
    v_ad_element_id UUID,
    v_id UUID,
    v_role_id UUID,
    v_object_id UUID,
    v_object_type_id INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO permissions (
        ad_element_id,
        id,
        role_id,
        object_id,
        object_type_id
        )
    VALUES (
        v_ad_element_id,
        v_id,
        v_role_id,
        v_object_id,
        v_object_type_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeletePermission (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM permissions
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM permissions
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPermissionsByid (v_id UUID)
RETURNS SETOF permissions_view STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_user_permissions_for_domain (
    v_name VARCHAR(255),
    v_domain VARCHAR(255)
    )
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
DECLARE v_user_name VARCHAR(255);

v_index INT;

BEGIN
    -- find if name already includes domain (@)
    v_index := POSITION('@' IN v_name);

    IF (v_index > 0) THEN
        v_user_name := substr(v_name, 0, v_index);
    ELSE
        v_user_name := v_name;
    END IF;

    RETURN QUERY

SELECT *
FROM permissions_view
WHERE permissions_view.ad_element_id IN (
        SELECT users.user_id
        FROM users
        WHERE users.domain = v_domain
            AND (
                users.name = v_user_name
                OR users.name = v_user_name || '@' || upper(v_domain)
                )
        );END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetConsumedPermissionsForQuotaId (v_quota_id UUID)
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE role_id IN (
            SELECT role_id
            FROM ROLES_groups
            WHERE action_group_id = 901
            )
        AND object_id IN (
            SELECT id
            FROM fn_get_entity_parents(v_quota_id, 17)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPermissionsByAdElementIdAndGroupIds(
    v_ad_element_id UUID,
    v_user_id UUID,
    v_user_groups UUID[],
    v_is_filtered BOOLEAN,
    v_app_mode INT
    )
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE (permissions_view.app_mode & v_app_mode) > 0
        AND (
            permissions_view.ad_element_id = v_ad_element_id
            OR ad_element_id = ANY(array_append(v_user_groups, 'EEE00000-0000-0000-0000-123456789EEE'))
            )
        AND (NOT v_is_filtered OR v_user_id = v_ad_element_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPermissionsByAdElementId (
    v_ad_element_id UUID,
    v_engine_session_seq_id INT,
    v_is_filtered BOOLEAN,
    v_app_mode INT
    )
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE (permissions_view.app_mode & v_app_mode) > 0
        AND (
            permissions_view.ad_element_id = v_ad_element_id
            OR ad_element_id IN (
                SELECT *
                FROM GetSessionUserAndGroupsById(v_ad_element_id, v_engine_session_seq_id)
                )
            )
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_permissions_permissions_view uv,
                    engine_sessions
                WHERE uv.user_id = engine_sessions.user_id
                    AND engine_sessions.id = v_engine_session_seq_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPermissionsByRoleId (v_role_id UUID)
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE role_id = v_role_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPermissionsByRoleIdAndAdElementId (
    v_role_id UUID,
    v_ad_element_id UUID
    )
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE role_id = v_role_id
        AND ad_element_id = v_ad_element_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPermissionsByRoleIdAndAdElementIdAndObjectId (
    v_role_id UUID,
    v_ad_element_id UUID,
    v_object_id UUID
    )
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE role_id = v_role_id
        AND ad_element_id = v_ad_element_id
        AND object_id = v_object_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDirectPermissionsByAdElementId (v_ad_element_id UUID)
RETURNS SETOF permissions_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM permissions_view
    WHERE permissions_view.ad_element_id = v_ad_element_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [roles] Table
--
CREATE OR REPLACE FUNCTION InsertRole (
    v_description VARCHAR(4000),
    v_id UUID,
    v_name VARCHAR(126),
    v_is_readonly BOOLEAN,
    v_role_type INT,
    v_allows_viewing_children BOOLEAN,
    v_app_mode INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO roles (
        description,
        id,
        name,
        is_readonly,
        role_type,
        allows_viewing_children,
        app_mode
        )
    VALUES (
        v_description,
        v_id,
        v_name,
        v_is_readonly,
        v_role_type,
        v_allows_viewing_children,
        v_app_mode
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateRole (
    v_description VARCHAR(4000),
    v_id UUID,
    v_name VARCHAR(126),
    v_is_readonly BOOLEAN,
    v_role_type INT,
    v_allows_viewing_children BOOLEAN
    )
RETURNS VOID
    --The [roles] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE roles
    SET description = v_description,
        name = v_name,
        is_readonly = v_is_readonly,
        role_type = v_role_type,
        allows_viewing_children = v_allows_viewing_children
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteRole (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM roles
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM roles
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- The logic to get the applicable set of roles for a given application mode is as below-
-- Calculate the bitwise AND of application mode from vdc_options and app_mode value of current
-- role from roles table.
-- If the calculated value is greater than 0, the role is applicable.
--
-- To explain with an example-
-- Currently supported application modes which can be set for a role are-
-- 1. VirtOnly		0000 0001
-- 2. GlusterOnly	0000 0010
-- 3. AllModes		1111 1111
--
-- Now suppose the value of application mode set in vdc_options is 2 (0000 0010), then
-- set of applicable roles would include all the roles with app_mode values either 2 or 255
-- Now start doing bitwise AND for valid app_mode values 1, 2 and 255 with application mode
-- value 2 from vdc_options. Only bitwise AND with 2 and 255 would result in a value greater
-- than ZERO (0) and applicable set of roles are identified.
--
-- 1 & 2 (0000 0001 & 0000 0010) = 0000 0000 = 0			Roles with this app_mode would NOT be listed
-- 2 & 2 (0000 0010 & 0000 0010) = 0000 0010 = 2 > 0		Roles with this app_mode would be listed
-- 255 & 2 (1111 1111 & 0000 0010) = 0000 0010 = 2 > 0		Roles with this app_mode would be listed

CREATE OR REPLACE FUNCTION GetAllFromRole(v_app_mode INTEGER)
RETURNS SETOF roles STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE (roles.app_mode & v_app_mode) > 0;

END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllNonAdminRoles(v_app_mode INTEGER)
RETURNS SETOF roles STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE (roles.app_mode & v_app_mode) > 0
      AND role_type != 1;

END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetRolsByid(v_id UUID)
RETURNS SETOF roles STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION GetRoleByName(v_name VARCHAR(126))
RETURNS SETOF roles STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE name = v_name;

END; $procedure$
LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION GetAnyAdminRoleByUserIdAndGroupIds(
    v_user_id UUID,
    v_group_ids text,
    v_app_mode INTEGER)
RETURNS SETOF roles STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT roles.*
   FROM roles INNER JOIN
   permissions ON permissions.role_id = roles.id
   WHERE (roles.app_mode & v_app_mode) > 0
       AND role_type = 1 -- admin
       AND (permissions.ad_element_id = v_user_id
           OR permissions.ad_element_id in(
               SELECT id
               FROM getElementIdsByIdAndGroups(v_user_id, v_group_ids))
       ) LIMIT 1;

END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [permissions] Table
--






CREATE OR REPLACE FUNCTION GetPermissionByRoleId(v_role_id UUID)
RETURNS SETOF permissions STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions
   WHERE role_id = v_role_id;
END; $procedure$
LANGUAGE plpgsql;





-- gets entity permissions given the user id, action group id and the object type and id
CREATE OR REPLACE FUNCTION get_entity_permissions(
    v_user_id UUID,
    v_action_group_id INTEGER,
    v_object_id UUID,v_object_type_id INTEGER)
RETURNS SETOF UUID STABLE
   -- Add the parameters for the stored procedure here
   AS $procedure$
   DECLARE
   v_everyone_object_id  UUID;
BEGIN
   v_everyone_object_id := getGlobalIds('everyone'); -- hardcoded also in MLA Handler
   RETURN QUERY
   SELECT   id
   FROM permissions
   WHERE
       role_id IN(
           SELECT role_id
           FROM roles_groups
           WHERE action_group_id = v_action_group_id)
       -- get allparents of object
       AND (object_id IN(
           SELECT id
           FROM fn_get_entity_parents(v_object_id,v_object_type_id)))
       -- get user and his groups
       AND (ad_element_id = v_everyone_object_id
       OR ad_element_id = v_user_id
       OR ad_element_id IN(
           SELECT *
           FROM getUserAndGroupsById(v_user_id)
       )) LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

-- gets entity permissions given the user id, groups, action group id and the object type and object id
CREATE OR REPLACE FUNCTION get_entity_permissions_for_user_and_groups(
    v_user_id UUID,
    v_group_ids text,
    v_action_group_id INTEGER,
    v_object_id UUID,
    v_object_type_id INTEGER,
    v_ignore_everyone BOOLEAN)
RETURNS SETOF UUID STABLE
	-- Add the parameters for the stored procedure here
AS $procedure$
   DECLARE
   v_everyone_object_id  UUID;
BEGIN
   v_everyone_object_id := getGlobalIds('everyone'); -- hardcoded also in MLA Handler
   RETURN QUERY
   SELECT id from permissions
   WHERE
   -- get all roles of action
       role_id IN(
           SELECT role_id from roles_groups
           WHERE action_group_id = v_action_group_id)
	-- get allparents of object
       AND (object_id IN(
           SELECT id
           FROM fn_get_entity_parents(v_object_id,v_object_type_id)))
	-- get user and his groups
       AND ((NOT v_ignore_everyone
           AND ad_element_id = v_everyone_object_id)
       OR ad_element_id = v_user_id
       OR ad_element_id IN(
            SELECT *
            FROM fnsplitteruuid(v_group_ids)
       )) LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [roles_groups] Table
--



CREATE OR REPLACE FUNCTION Insert_roles_groups(
    v_action_group_id INTEGER,
    v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
    INSERT INTO roles_groups(
        action_group_id,
        role_id)
    VALUES(
        v_action_group_id,
        v_role_id);
END; $procedure$
LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION Delete_roles_groups(
    v_action_group_id INTEGER,
    v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

    DELETE FROM roles_groups
    WHERE action_group_id = v_action_group_id
        AND role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION Get_roles_groups_By_action_group_id_And_By_role_id(
    v_action_group_id INTEGER,
    v_role_id UUID)
RETURNS SETOF roles_groups STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM roles_groups
    WHERE
        action_group_id = v_action_group_id
        AND role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION Get_role_groups_By_role_id(v_role_id UUID)
RETURNS SETOF roles_groups STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM roles_groups
    WHERE
        role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION GetPermissionsByEntityId(
    v_id UUID,
    v_engine_session_seq_id INTEGER,
    v_is_filtered BOOLEAN,
    v_app_mode INTEGER)
RETURNS SETOF permissions_view STABLE
-- SET NOCOUNT ON added to prevent extra result sets from
-- interfering with SELECT statements.
   AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM permissions_view
    WHERE  (permissions_view.app_mode & v_app_mode) > 0
        AND object_id = v_id
        AND (NOT v_is_filtered OR EXISTS (
            SELECT 1
            FROM GetUserPermissionsByEntityId(
                v_id,
                v_engine_session_seq_id,
                v_is_filtered)
        )
    );
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllUsersWithPermissionsOnEntityByEntityId(
    v_id UUID,
    v_engine_session_seq_id INTEGER,
    v_is_filtered BOOLEAN,
    v_app_mode INTEGER)
RETURNS SETOF permissions_view STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM permissions_view
    WHERE (permissions_view.app_mode & v_app_mode) > 0
        AND object_id = v_id
        AND (NOT v_is_filtered OR EXISTS (
            SELECT 1
            FROM GetAllUsersWithPermissionsByEntityId(
                v_id,
                v_engine_session_seq_id,
                v_is_filtered)
        )
    );
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetUserPermissionsByEntityId(
    v_id UUID,
    v_engine_session_seq_id INTEGER,
    v_is_filtered BOOLEAN)
RETURNS SETOF permissions_view STABLE
-- SET NOCOUNT ON added to prevent extra result sets from
-- interfering with SELECT statements.
   AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM permissions_view p
    WHERE object_id = v_id
    AND (NOT v_is_filtered OR EXISTS (
        SELECT 1
        FROM  engine_session_user_flat_groups u
        WHERE  p.ad_element_id = u.granted_id
            AND u.engine_session_seq_id = v_engine_session_seq_id));
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetAllUsersWithPermissionsByEntityId(
     v_id UUID,
     v_engine_session_seq_id INTEGER,
     v_is_filtered BOOLEAN)
RETURNS SETOF permissions_view STABLE
   AS $procedure$
DECLARE
    r_type int4;
BEGIN
    FOR r_type IN (
        SELECT DISTINCT role_type
        FROM permissions_view p
        WHERE object_id = v_id)
    LOOP
        RETURN QUERY SELECT *
        FROM permissions_view p
        WHERE object_id in (select id from fn_get_entity_parents(v_id, r_type))
            AND (NOT v_is_filtered OR EXISTS (
                SELECT 1
                FROM   engine_session_user_flat_groups u
                WHERE  p.ad_element_id = u.granted_id
                    AND u.engine_session_seq_id = v_engine_session_seq_id));
    END LOOP;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION DeletePermissionsByEntityId(v_id UUID)
RETURNS VOID
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   DELETE FROM permissions
   WHERE object_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION DeletePermissionsByEntityIds(v_ids UUID[])
RETURNS VOID
   AS $procedure$
BEGIN
   DELETE FROM permissions
   WHERE object_id = ANY(v_ids);
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION GetRoleActionGroupsByRoleId(v_id UUID)
RETURNS SETOF roles_groups STABLE
    -- SET NOCOUNT ON added to prevent extra result sets from
    -- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_groups
   WHERE role_id = v_id;
END; $procedure$
LANGUAGE plpgsql;






CREATE OR REPLACE FUNCTION GetPermissionsTreeByEntityId(
    v_id UUID,
    v_object_type_id INTEGER,
    v_engine_session_seq_id INTEGER,
    v_is_filtered BOOLEAN, v_app_mode INTEGER)
RETURNS SETOF permissions_view STABLE
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  permissions_view p
    WHERE  (p.app_mode & v_app_mode) > 0
        AND object_id IN(
            SELECT id
            FROM fn_get_entity_parents(v_id,v_object_type_id))
        AND (NOT v_is_filtered
             OR EXISTS (
                 SELECT 1
                 FROM engine_session_user_flat_groups u
                 WHERE  p.ad_element_id = u.granted_id
                     AND u.engine_session_seq_id = v_engine_session_seq_id)
        );

END; $procedure$
LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION GetPermissionsByRoleIdAndObjectId(
    v_role_id UUID,
    v_object_id UUID)
RETURNS SETOF permissions_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id
       AND object_id = v_object_id;

END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetForRoleAndAdElementAndObject_wGroupCheck(
    v_role_id UUID,
    v_ad_element_id UUID,
    v_object_id UUID)
RETURNS SETOF permissions_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id
       AND object_id = v_object_id
       AND ad_element_id IN (
           SELECT *
           FROM getUserAndGroupsById(v_ad_element_id)
       );
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPermissionsByAdElementAndObjectId(
    v_ad_element_id UUID,
    v_object_id UUID)
RETURNS SETOF permissions_view STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM permissions_view
    WHERE ad_element_id = v_ad_element_id
        AND object_id = v_object_id;
END; $procedure$
LANGUAGE plpgsql;
