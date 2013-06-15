

----------------------------------------------------------------
-- [bookmarks] Table
--




Create or replace FUNCTION InsertBookmark(v_bookmark_id UUID,
	v_bookmark_name VARCHAR(40),
	v_bookmark_value VARCHAR(300))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO bookmarks(bookmark_Id,bookmark_name, bookmark_value)
	VALUES(v_bookmark_id, v_bookmark_name, v_bookmark_value);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateBookmark(v_bookmark_id UUID,
    v_bookmark_name VARCHAR(40),
	v_bookmark_value VARCHAR(300))
RETURNS VOID

	--The [bookmarks] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE bookmarks
      SET bookmark_name = v_bookmark_name,bookmark_value = v_bookmark_value
      WHERE bookmark_Id = v_bookmark_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteBookmark(v_bookmark_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM bookmarks
      WHERE bookmark_Id = v_bookmark_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromBookmarks() RETURNS SETOF bookmarks
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM bookmarks;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetBookmarkBybookmark_name(v_bookmark_name VARCHAR(40))
RETURNS SETOF bookmarks
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM bookmarks
      WHERE bookmark_name = v_bookmark_name;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetBookmarkBybookmark_id(v_bookmark_id UUID)
RETURNS SETOF bookmarks
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM bookmarks
      WHERE bookmark_Id = v_bookmark_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllVm_poolsByUser_id_with_groups_and_UserRoles(v_user_id UUID)
RETURNS SETOF vm_pools_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT pools.*
	FROM vm_pools_view pools
    INNER JOIN user_vm_pool_permissions_view ON user_id = v_user_id AND entity_id = pools.vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;

