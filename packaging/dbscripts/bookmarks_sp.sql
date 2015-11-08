

----------------------------------------------------------------
-- [bookmarks] Table
--
CREATE OR REPLACE FUNCTION InsertBookmark (
    v_bookmark_id UUID,
    v_bookmark_name VARCHAR(40),
    v_bookmark_value VARCHAR(300)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO bookmarks (
        bookmark_Id,
        bookmark_name,
        bookmark_value
        )
    VALUES (
        v_bookmark_id,
        v_bookmark_name,
        v_bookmark_value
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateBookmark (
    v_bookmark_id UUID,
    v_bookmark_name VARCHAR(40),
    v_bookmark_value VARCHAR(300)
    )
RETURNS VOID
    --The [bookmarks] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE bookmarks
    SET bookmark_name = v_bookmark_name,
        bookmark_value = v_bookmark_value
    WHERE bookmark_Id = v_bookmark_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteBookmark (v_bookmark_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM bookmarks
    WHERE bookmark_Id = v_bookmark_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromBookmarks ()
RETURNS SETOF bookmarks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM bookmarks;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBookmarkBybookmark_name (v_bookmark_name VARCHAR(40))
RETURNS SETOF bookmarks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM bookmarks
    WHERE bookmark_name = v_bookmark_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBookmarkBybookmark_id (v_bookmark_id UUID)
RETURNS SETOF bookmarks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM bookmarks
    WHERE bookmark_Id = v_bookmark_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


