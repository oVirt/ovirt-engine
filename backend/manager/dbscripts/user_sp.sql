





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
	v_desktop_device VARCHAR(255) ,
	v_domain VARCHAR(255),
	v_email VARCHAR(255) ,
	v_groups VARCHAR,
	v_name VARCHAR(255) ,
	v_note VARCHAR(255) ,
	v_role VARCHAR(255) ,
	v_status INTEGER,
	v_surname VARCHAR(255) ,
	v_user_icon_path VARCHAR(255) ,
	v_user_id UUID,
    v_session_count INTEGER,
	v_username VARCHAR(255),
	v_group_ids VARCHAR(2048))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO users(department, desktop_device, domain, email, groups, name, note, role, status, surname, user_icon_path, user_id, session_count, username, group_ids)
	VALUES(v_department, v_desktop_device, v_domain, v_email, v_groups, v_name, v_note, v_role, v_status, v_surname, v_user_icon_path, v_user_id, v_session_count, v_username, v_group_ids);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateUser(v_department VARCHAR(255) ,
	v_desktop_device VARCHAR(255) ,
	v_domain VARCHAR(255),
	v_email VARCHAR(255) ,
	v_groups VARCHAR(4000),
	v_name VARCHAR(255) ,
	v_note VARCHAR(255) ,
	v_role VARCHAR(255) ,
	v_status INTEGER,
	v_surname VARCHAR(255) ,
	v_user_icon_path VARCHAR(255) ,
	v_user_id UUID,
    v_session_count INTEGER,
	v_username VARCHAR(255),
	v_last_admin_check_status BOOLEAN,
	v_group_ids VARCHAR(2048))
RETURNS VOID

	--The [users] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE users
      SET department = v_department,desktop_device = v_desktop_device,domain = v_domain,
      email = v_email,groups = v_groups,name = v_name,note = v_note,
      role = v_role,status = v_status,surname = v_surname,user_icon_path = v_user_icon_path,
      username = v_username,session_count = v_session_count,
      last_admin_check_status = v_last_admin_check_status,
      group_ids = v_group_ids
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





Create or replace FUNCTION GetAllFromUsers() RETURNS SETOF users
   AS $procedure$
BEGIN
      RETURN QUERY SELECT users.*
      FROM users;
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




----------------------------------------------------------------
-- [user_sessions] Table
----------------------------------------------------------------


Create or replace FUNCTION Insertuser_sessions(v_browser CHAR(10) ,
	v_client_type CHAR(10) ,
	v_login_time TIMESTAMP WITH TIME ZONE ,
	v_os CHAR(10) ,
	v_session_id CHAR(32),
	v_user_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      if (not exists(select session_id from user_sessions
      where session_id = v_session_id and user_id = v_user_id)) then

INSERT INTO user_sessions(browser, client_type, login_time, os, session_id, user_id)
				VALUES(v_browser, v_client_type, v_login_time, v_os, v_session_id, v_user_id);

         UPDATE users
         SET session_count = session_count+1
         WHERE
         user_id = v_user_id;
      end if;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deleteuser_sessions(v_session_id CHAR(32),
	v_user_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_counter  INTEGER;
   SWV_RowCount INTEGER;
BEGIN
      DELETE FROM user_sessions
      WHERE session_id = v_session_id AND user_id = v_user_id;
      GET DIAGNOSTICS SWV_RowCount = ROW_COUNT;
      if (SWV_RowCount > 0) then
         select   session_count INTO v_counter from users WHERE
         user_id = v_user_id;
         if (v_counter > 0) then
            UPDATE users
            SET session_count = session_count -1
            WHERE
            user_id = v_user_id;
         end if;
      end if;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deleteuser_sessionsByuser_id(v_user_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM user_sessions
      WHERE user_id = v_user_id;
      UPDATE users
      SET session_count = 0
      WHERE
      user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromuser_sessions() RETURNS SETOF user_sessions
   AS $procedure$
BEGIN
      RETURN QUERY SELECT user_sessions.*
      FROM user_sessions;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getuser_sessionsBysession_idAndByuser_id(v_session_id CHAR(32),
	v_user_id UUID) RETURNS SETOF user_sessions
   AS $procedure$
BEGIN
      RETURN QUERY SELECT user_sessions.*
      FROM user_sessions
      WHERE session_id = v_session_id AND user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteAlluser_sessions()
RETURNS VOID
   AS $procedure$
BEGIN
      TRUNCATE TABLE user_sessions;
      UPDATE users
      SET session_count = 0
      WHERE
      session_count > 0;
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


