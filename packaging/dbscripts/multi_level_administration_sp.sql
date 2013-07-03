

----------------------------------------------------------------
-- [permissions] Table
--




Create or replace FUNCTION InsertPermission(v_ad_element_id UUID,
	v_id UUID,
	v_role_id UUID,
	v_object_id UUID,
	v_object_type_id INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO permissions(ad_element_id, id, role_id, object_id, object_type_id)
	VALUES(v_ad_element_id, v_id, v_role_id, v_object_id, v_object_type_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeletePermission(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM permissions  WHERE id = v_id     FOR UPDATE;

   DELETE FROM permissions
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetPermissionsByid(v_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION get_user_permissions_for_domain(v_name VARCHAR(255), v_domain VARCHAR(255))
RETURNS SETOF permissions_view
   AS $procedure$
   DECLARE
   v_user_name VARCHAR(255);
   v_index  INTEGER;
BEGIN
-- find if name already includes domain (@)
   v_index := POSITION('@' IN v_name);

   if (v_index > 0) then
      v_user_name := substr(v_name, 0, v_index);
   else
      v_user_name := v_name;
   end if;
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE permissions_view.ad_element_id in (
                SELECT users.user_id
                FROM users
                WHERE users.domain = v_domain
                AND (users.name = v_user_name OR
                     users.name = v_user_name || '@' || upper(v_domain)
                    ));

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetConsumedPermissionsForQuotaId(v_quota_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY    SELECT *
   FROM permissions_view
   WHERE role_id in (SELECT role_id FROM ROLES_groups where action_group_id = 901)
     AND object_id in(select id from  fn_get_entity_parents(v_quota_id,17));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetPermissionsByAdElementId(v_ad_element_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE
   (permissions_view.ad_element_id = v_ad_element_id
    OR    ad_element_id IN (SELECT * FROM getUserAndGroupsById(v_ad_element_id)))
   AND (NOT v_is_filtered OR EXISTS (SELECT 1 FROM user_permissions_permissions_view WHERE user_id = v_user_id));

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByRoleId(v_role_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetPermissionsByRoleIdAndAdElementId(v_role_id UUID,
	v_ad_element_id UUID) RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and ad_element_id = v_ad_element_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByRoleIdAndAdElementIdAndObjectId(v_role_id UUID,
	v_ad_element_id UUID,v_object_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and ad_element_id = v_ad_element_id and object_id = v_object_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetDirectPermissionsByAdElementId(v_ad_element_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE permissions_view.ad_element_id = v_ad_element_id;

END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [roles] Table
--


Create or replace FUNCTION InsertRole(v_description VARCHAR(4000) ,
	v_id UUID,
	v_name VARCHAR(126),
	v_is_readonly BOOLEAN,
	v_role_type INTEGER,
        v_allows_viewing_children BOOLEAN)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO roles(description, id, name, is_readonly, role_type, allows_viewing_children)
	VALUES(v_description, v_id, v_name, v_is_readonly, v_role_type, v_allows_viewing_children);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateRole(v_description VARCHAR(4000) ,
	v_id UUID,
	v_name VARCHAR(126),
	v_is_readonly BOOLEAN,
	v_role_type INTEGER,
        v_allows_viewing_children BOOLEAN)
RETURNS VOID
	--The [roles] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE roles
      SET description    = v_description,
          name           = v_name,
          is_readonly    = v_is_readonly,
          role_type      = v_role_type,
          allows_viewing_children = v_allows_viewing_children
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteRole(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
   select   id INTO v_val FROM roles  WHERE id = v_id     FOR UPDATE;

   DELETE FROM roles
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllFromRole() RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetRolsByid(v_id UUID) RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetRoleByName(v_name VARCHAR(126))
RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE name = v_name;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllRolesByUserIdAndGroupIds(v_user_id UUID, v_group_ids text)
RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT roles.*
   FROM roles INNER JOIN
   permissions ON permissions.role_id = roles.id
   WHERE permissions.ad_element_id = v_user_id
   or permissions.ad_element_id in(select id from getElementIdsByIdAndGroups(v_user_id, v_group_ids));

END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [permissions] Table
--






Create or replace FUNCTION GetPermissionByRoleId(v_role_id UUID)
RETURNS SETOF permissions
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   from permissions
   where role_id = v_role_id;
END; $procedure$
LANGUAGE plpgsql;





-- gets entity permissions given the user id, action group id and the object type and id
Create or replace FUNCTION get_entity_permissions(v_user_id UUID,v_action_group_id INTEGER,v_object_id UUID,v_object_type_id INTEGER)
RETURNS SETOF UUID
	-- Add the parameters for the stored procedure here
   AS $procedure$
   DECLARE
   v_everyone_object_id  UUID;
BEGIN
   v_everyone_object_id := getGlobalIds('everyone'); -- hardcoded also in MLA Handler
   RETURN QUERY
   select   id from permissions where
		-- get all roles of action
   role_id in(select role_id from roles_groups where action_group_id = v_action_group_id)
		-- get allparents of object
   and (object_id in(select id from  fn_get_entity_parents(v_object_id,v_object_type_id)))
		-- get user and his groups
   and (ad_element_id = v_everyone_object_id or
   ad_element_id = v_user_id or ad_element_id in(select * from getUserAndGroupsById(v_user_id)))   LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

-- gets entity permissions given the user id, groups, action group id and the object type and object id
Create or replace FUNCTION get_entity_permissions_for_user_and_groups(v_user_id UUID,v_group_ids text,v_action_group_id INTEGER,v_object_id UUID,v_object_type_id INTEGER,
v_ignore_everyone BOOLEAN)
RETURNS SETOF UUID
	-- Add the parameters for the stored procedure here
   AS $procedure$
   DECLARE
   v_everyone_object_id  UUID;
BEGIN
   v_everyone_object_id := getGlobalIds('everyone'); -- hardcoded also in MLA Handler
   RETURN QUERY
   select   id from permissions where
		-- get all roles of action
   role_id in(select role_id from roles_groups where action_group_id = v_action_group_id)
		-- get allparents of object
   and (object_id in(select id from  fn_get_entity_parents(v_object_id,v_object_type_id)))
		-- get user and his groups
   and ((NOT v_ignore_everyone and ad_element_id = v_everyone_object_id) or
   ad_element_id = v_user_id or ad_element_id in(select * from fnsplitteruuid(v_group_ids)))   LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [roles_groups] Table
--



Create or replace FUNCTION Insert_roles_groups(v_action_group_id INTEGER,
	v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO roles_groups(action_group_id, role_id)
	VALUES(v_action_group_id, v_role_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Delete_roles_groups(v_action_group_id INTEGER,
	v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM roles_groups
   WHERE action_group_id = v_action_group_id AND role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Get_roles_groups_By_action_group_id_And_By_role_id(v_action_group_id INTEGER,v_role_id UUID) RETURNS SETOF roles_groups
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_groups
   where
   action_group_id = v_action_group_id AND
   role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Get_role_groups_By_role_id(v_role_id UUID)
RETURNS SETOF roles_groups
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_groups
   where
   role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByEntityId(v_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF permissions_view
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE object_id = v_id
   AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   GetUserPermissionsByEntityId(v_id, v_user_id, v_is_filtered)));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetUserPermissionsByEntityId(v_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF permissions_view
    -- SET NOCOUNT ON added to prevent extra result sets from
    -- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view p
   WHERE object_id = v_id
   AND   (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_flat_groups u
                                       WHERE  p.ad_element_id = u.granted_id
                                       AND    u.user_id       = v_user_id));
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION DeletePermissionsByEntityId(v_id UUID)
RETURNS VOID
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   DELETE FROM permissions
   WHERE object_id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetRoleActionGroupsByRoleId(v_id UUID)
RETURNS SETOF roles_groups
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_groups
   WHERE role_id = v_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetPermissionsTreeByEntityId
(v_id UUID, v_object_type_id INTEGER, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF permissions_view
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM   permissions_view p
   WHERE  object_id in(select id from  fn_get_entity_parents(v_id,v_object_type_id))
   AND    (NOT v_is_filtered OR EXISTS (SELECT 1
                                        FROM   user_flat_groups u
                                        WHERE  p.ad_element_id = u.granted_id
                                        AND    u.user_id       = v_user_id));

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByRoleIdAndObjectId(v_role_id UUID,
	v_object_id UUID) RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and object_id = v_object_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetForRoleAndAdElementAndObject_wGroupCheck(v_role_id UUID,
	v_ad_element_id UUID, v_object_id UUID) RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and object_id = v_object_id and ad_element_id in (
         select * from getUserAndGroupsById(v_ad_element_id));
END; $procedure$
LANGUAGE plpgsql;

