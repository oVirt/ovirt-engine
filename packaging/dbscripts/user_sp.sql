





Create or replace FUNCTION GetTimeLeasedUsersVmsByGroupIdAndPoolId(v_groupId UUID,
 v_vm_pool_id UUID) RETURNS SETOF tags_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY select tags_vm_map.* from tags_vm_map
      inner join vm_pool_map on vm_pool_map.vm_guid = tags_vm_map.vm_id
      inner join tags_permissions_map on tags_permissions_map.tag_id = tags_vm_map.tag_id
      inner join permissions on permissions.id = tags_permissions_map.permission_id
      where permissions.ad_element_id
      in(select users.user_id from users
         where (users.groups LIKE '%' ||(select ad_groups.name from ad_groups where ad_groups.id = v_groupId)
         || '%'))
      and
      vm_pool_map.vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [users] Table
--


Create or replace FUNCTION InsertUser(v_department VARCHAR(255) ,
	v_domain VARCHAR(255),
	v_email VARCHAR(255) ,
	v_groups VARCHAR,
	v_name VARCHAR(255) ,
	v_note VARCHAR(255) ,
	v_role VARCHAR(255) ,
	v_status INTEGER,
	v_surname VARCHAR(255) ,
	v_user_id UUID,
	v_username VARCHAR(255),
	v_group_ids VARCHAR(2048),
	v_external_id BYTEA)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO users(department, domain, email, groups, name, note, role, status, surname, user_id, username, group_ids, external_id)
	VALUES(v_department, v_domain, v_email, v_groups, v_name, v_note, v_role, v_status, v_surname, v_user_id, v_username, v_group_ids, v_external_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateUser(v_department VARCHAR(255) ,
	v_domain VARCHAR(255),
	v_email VARCHAR(255) ,
	v_groups VARCHAR(4000),
	v_name VARCHAR(255) ,
	v_note VARCHAR(255) ,
	v_role VARCHAR(255) ,
	v_status INTEGER,
	v_surname VARCHAR(255) ,
	v_user_id UUID,
	v_username VARCHAR(255),
	v_last_admin_check_status BOOLEAN,
	v_group_ids VARCHAR(2048),
        v_external_id BYTEA)
RETURNS VOID

	--The [users] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE users
      SET department = v_department,domain = v_domain,
      email = v_email,groups = v_groups,name = v_name,note = v_note,
      role = v_role,status = v_status,surname = v_surname,
      username = v_username,
      last_admin_check_status = v_last_admin_check_status,
      group_ids = v_group_ids,
      external_id = v_external_id
      WHERE user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteUser(v_user_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
			-- Get (and keep) a shared lock with "right to upgrade to exclusive"
			-- in order to force locking parent before children
      select   user_id INTO v_val FROM users  WHERE user_id = v_user_id     FOR UPDATE;
      DELETE FROM tags_user_map
      WHERE user_id = v_user_id;
      DELETE FROM users
      WHERE user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromUsers(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF users
   AS $procedure$
BEGIN
      RETURN QUERY SELECT users.*
      FROM users
      WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                   FROM   users u, user_db_users_permissions_view p
                                   WHERE  u.user_id = v_user_id AND u.user_id = p.ad_element_id));
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetUserByUserId(v_user_id UUID) RETURNS SETOF users
   AS $procedure$
BEGIN
      RETURN QUERY SELECT users.*
      FROM users
      WHERE user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetUserByExternalId(v_domain VARCHAR(255), v_external_id BYTEA) RETURNS SETOF users
   AS $procedure$
BEGIN
      RETURN QUERY SELECT users.*
      FROM users
      WHERE domain = v_domain AND external_id = v_external_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetUserByUserName(v_username VARCHAR(255)) RETURNS SETOF users
   AS $procedure$
BEGIN
      RETURN QUERY SELECT users.*
      FROM users
      WHERE username = v_username;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetUsersByVmGuid(v_vm_guid UUID) RETURNS SETOF users
   AS $procedure$
BEGIN
      RETURN QUERY SELECT users.*
      FROM users
      inner join permissions
      on users.user_id = permissions.ad_element_id
      WHERE permissions.object_type_id = 2
      and	permissions.object_id = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION UpdateLastAdminCheckStatus(v_userIds VARCHAR(4000))
RETURNS VOID
   AS $procedure$
   DECLARE
   v_id  UUID;
   v_tempId  VARCHAR(4000);
   myCursor cursor for select id from fnSplitter(v_userIds);
   v_result  INTEGER;
BEGIN
	-- get users and its groups
	-- get their permission based on ad_element_id.
	-- if one permissions role's type is ADMIN(1) then set the user last_admin_check_status to 1
   OPEN myCursor;
   FETCH myCursor into v_tempId;
   WHILE FOUND LOOP
      v_id := CAST(v_tempId AS UUID);
      select   count(*) INTO v_result from users where user_id in(select ad_element_id as user_id from permissions,roles
         where permissions.role_id = roles.id
         and ad_element_id in((select id from ad_groups,users where users.user_id = v_id
               and ad_groups.id in(select * from fnsplitteruuid(users.group_ids))
               union
               select v_id))
         and (roles.role_type = 1 or permissions.role_id = '00000000-0000-0000-0000-000000000001'));
      update users set last_admin_check_status =
      case
      when v_result = 0 then FALSE
      else TRUE
      end
      where user_id = v_id;
      FETCH myCursor into v_tempId;
   END LOOP;
   CLOSE myCursor;
END; $procedure$
LANGUAGE plpgsql;


