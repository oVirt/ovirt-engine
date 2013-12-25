CREATE OR REPLACE FUNCTION __temp_add_admin_operations_action_groups()
  RETURNS void AS
$BODY$
   DECLARE
      v_roles_to_filter_out uuid[];
      v_TAG_MANAGER_ROLE_ID uuid;
      v_BOOKMARK_MANAGER_ROLE_ID uuid;
      v_EVENT_NOTIFICATION_MANAGER_ROLE_ID uuid;
BEGIN
   v_TAG_MANAGER_ROLE_ID := 'DEF00011-0000-0000-0000-DEF000000013';
   v_BOOKMARK_MANAGER_ROLE_ID := 'DEF00011-0000-0000-0000-DEF000000014';
   v_EVENT_NOTIFICATION_MANAGER_ROLE_ID := 'DEF00011-0000-0000-0000-DEF000000015';

   -- We only add these action groups to ADMIN roles that have an action group that isn't the login permissions one
   v_roles_to_filter_out := array(select id from roles where role_type = 2 or (exists (select * from roles_groups where role_id = id) and not exists (select * from roles_groups where role_id = id and action_group_id != 1300)));

   -- Adding the TAG_MANAGEMENT action group
   perform fn_db_grant_action_group_to_all_roles_filter(1301, v_roles_to_filter_out);

   -- Adding the BOOKMARK_MANAGEMENT action group
   perform fn_db_grant_action_group_to_all_roles_filter(1302, v_roles_to_filter_out);

   -- Adding the EVENT_NOTIFICATION_MANAGEMENT action group
   perform fn_db_grant_action_group_to_all_roles_filter(1303, v_roles_to_filter_out);

   -- Adding the TagManager role
   DELETE FROM roles_groups WHERE role_id = v_TAG_MANAGER_ROLE_ID;

   INSERT INTO roles(id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) SELECT v_TAG_MANAGER_ROLE_ID, 'TagManager', 'Tag Manager', true, 1, false, 255
   WHERE NOT EXISTS (SELECT id
                     FROM roles
                     WHERE id = v_TAG_MANAGER_ROLE_ID);

   INSERT INTO roles_groups values(v_TAG_MANAGER_ROLE_ID, 1301);
   INSERT INTO roles_groups values(v_TAG_MANAGER_ROLE_ID, 1300);

   -- Adding the BookmarkManager role
   DELETE FROM roles_groups WHERE role_id = v_BOOKMARK_MANAGER_ROLE_ID;

   INSERT INTO roles(id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) SELECT v_BOOKMARK_MANAGER_ROLE_ID, 'BookmarkManager', 'Bookmark Manager', true, 1, false, 255
   WHERE NOT EXISTS (SELECT id
                     FROM roles
                     WHERE id = v_BOOKMARK_MANAGER_ROLE_ID);

   INSERT INTO roles_groups values(v_BOOKMARK_MANAGER_ROLE_ID, 1302);
   INSERT INTO roles_groups values(v_BOOKMARK_MANAGER_ROLE_ID, 1300);

   -- Adding the EventNotificationManager role
   DELETE FROM roles_groups WHERE role_id = v_EVENT_NOTIFICATION_MANAGER_ROLE_ID;

   INSERT INTO roles(id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) SELECT v_EVENT_NOTIFICATION_MANAGER_ROLE_ID, 'EventNotificationManager', 'Event Notification Manager', true, 1, false, 255
   WHERE NOT EXISTS (SELECT id
                     FROM roles
                     WHERE id = v_EVENT_NOTIFICATION_MANAGER_ROLE_ID);

   INSERT INTO roles_groups values(v_EVENT_NOTIFICATION_MANAGER_ROLE_ID, 1303);
   INSERT INTO roles_groups values(v_EVENT_NOTIFICATION_MANAGER_ROLE_ID, 1300);

END; $BODY$

LANGUAGE plpgsql;

select __temp_add_admin_operations_action_groups();
drop function __temp_add_admin_operations_action_groups();

