

----------------------------------------------------------------
-- [tags] Table
--
CREATE OR REPLACE FUNCTION Inserttags (
    v_description VARCHAR(4000),
    v_tag_id UUID,
    v_tag_name VARCHAR(50),
    v_parent_id UUID,
    v_readonly BOOLEAN,
    v_type INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO tags (
        tag_id,
        description,
        tag_name,
        parent_id,
        readonly,
        type
        )
    VALUES (
        v_tag_id,
        v_description,
        v_tag_name,
        v_parent_id,
        v_readonly,
        v_type
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatetags (
    v_description VARCHAR(4000),
    v_tag_id UUID,
    v_tag_name VARCHAR(50),
    v_parent_id UUID,
    v_readonly BOOLEAN,
    v_type INT
    )
RETURNS VOID
    --The [tags] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE tags
    SET description = v_description,
        tag_name = v_tag_name,
        parent_id = v_parent_id,
        readonly = v_readonly,
        type = v_type,
        _update_date = LOCALTIMESTAMP
    WHERE tag_id = v_tag_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletetags (v_tag_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT tag_id
    INTO v_val
    FROM tags
    WHERE tag_id = v_tag_id
    FOR

    UPDATE;

    DELETE
    FROM tags_user_group_map
    WHERE tag_id = v_tag_id;

    DELETE
    FROM tags_user_map
    WHERE tag_id = v_tag_id;

    DELETE
    FROM tags_vm_map
    WHERE tag_id = v_tag_id;

    DELETE
    FROM tags_vds_map
    WHERE tag_id = v_tag_id;

    DELETE
    FROM tags_vm_pool_map
    WHERE tag_id = v_tag_id;

    DELETE
    FROM tags
    WHERE tag_id = v_tag_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromtags ()
RETURNS SETOF tags STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags.*
    FROM tags;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GettagsBytag_id (v_tag_id UUID)
RETURNS SETOF tags STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags.*
    FROM tags
    WHERE tag_id = v_tag_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GettagsByparent_id (v_parent_id UUID)
RETURNS SETOF tags STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags.*
    FROM tags
    WHERE parent_id = v_parent_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GettagsBytag_name (v_tag_name VARCHAR(50))
RETURNS SETOF tags STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags.*
    FROM tags
    WHERE tag_name = v_tag_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [tags_user_group_map] Table
--
CREATE OR REPLACE FUNCTION Inserttags_user_group_map (
    v_group_id UUID,
    v_tag_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO tags_user_group_map (
        group_id,
        tag_id
        )
    VALUES (
        v_group_id,
        v_tag_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletetags_user_group_map (
    v_group_id UUID,
    v_tag_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM tags_user_group_map
    WHERE group_id = v_group_id
        AND tag_id = v_tag_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromtags_user_group_map ()
RETURNS SETOF tags_user_group_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_user_group_map.*
    FROM tags_user_group_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagUserGroupByGroupIdAndByTagId (
    v_group_id UUID,
    v_tag_id UUID
    )
RETURNS SETOF tags_user_group_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_user_group_map.*
    FROM tags_user_group_map
    WHERE group_id = v_group_id
        AND tag_id = v_tag_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagsByUserGroupId (v_group_ids VARCHAR(4000))
RETURNS SETOF tags_user_group_map_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT tags_user_group_map_view.*
    FROM tags_user_group_map_view
    WHERE group_id IN (
            SELECT *
            FROM fnSplitterUuid(v_group_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [tags_user_map] Table
--
CREATE OR REPLACE FUNCTION Inserttags_user_map (
    v_tag_id UUID,
    v_user_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO tags_user_map (
        tag_id,
        user_id
        )
    VALUES (
        v_tag_id,
        v_user_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletetags_user_map (
    v_tag_id UUID,
    v_user_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM tags_user_map
    WHERE tag_id = v_tag_id
        AND user_id = v_user_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromtags_user_map ()
RETURNS SETOF tags_user_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_user_map.*
    FROM tags_user_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagUserByTagIdAndByuserId (
    v_tag_id UUID,
    v_user_id UUID
    )
RETURNS SETOF tags_user_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_user_map.*
    FROM tags_user_map
    WHERE tag_id = v_tag_id
        AND user_id = v_user_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagsByUserId (v_user_ids VARCHAR(4000))
RETURNS SETOF tags_user_map_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT tags_user_map_view.*
    FROM tags_user_map_view
    WHERE user_id IN (
            SELECT *
            FROM fnSplitterUuid(v_user_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetUserTagsByTagIds (v_tag_ids VARCHAR(4000))
RETURNS SETOF tags_user_map_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT tags_user_map_view.*
    FROM tags_user_map_view
    WHERE tag_id IN (
            SELECT *
            FROM fnSplitterUuid(v_tag_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [tags_vds_map] Table
--
CREATE OR REPLACE FUNCTION Inserttags_vds_map (
    v_tag_id UUID,
    v_vds_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO tags_vds_map (
        tag_id,
        vds_id
        )
    VALUES (
        v_tag_id,
        v_vds_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletetags_vds_map (
    v_tag_id UUID,
    v_vds_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM tags_vds_map
    WHERE tag_id = v_tag_id
        AND vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllTagsVdsMapForHost (
    v_vds_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM tags_vds_map
    WHERE vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromtags_vds_map ()
RETURNS SETOF tags_vds_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_vds_map.*
    FROM tags_vds_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagVdsBytagIdAndByVdsId (
    v_tag_id UUID,
    v_vds_id UUID
    )
RETURNS SETOF tags_vds_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_vds_map.*
    FROM tags_vds_map
    WHERE tag_id = v_tag_id
        AND vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagsByVdsId (v_vds_ids VARCHAR(4000))
RETURNS SETOF tags_vds_map_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT tags_vds_map_view.*
    FROM tags_vds_map_view
    WHERE vds_id IN (
            SELECT *
            FROM fnSplitterUuid(v_vds_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [tags_vm_map] Table
--
CREATE OR REPLACE FUNCTION Inserttags_vm_map (
    v_tag_id UUID,
    v_vm_id UUID,
    v_DefaultDisplayType INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO tags_vm_map (
        tag_id,
        vm_id,
        DefaultDisplayType
        )
    VALUES (
        v_tag_id,
        v_vm_id,
        v_DefaultDisplayType
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletetags_vm_map (
    v_tag_id UUID,
    v_vm_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM tags_vm_map
    WHERE tag_id = v_tag_id
        AND vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromtags_vm_map ()
RETURNS SETOF tags_vm_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_vm_map.*
    FROM tags_vm_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagVmByTagIdAndByvmId (
    v_tag_id UUID,
    v_vm_id UUID
    )
RETURNS SETOF tags_vm_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_vm_map.*
    FROM tags_vm_map
    WHERE tag_id = v_tag_id
        AND vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTagsByVmId (v_vm_ids VARCHAR(4000))
RETURNS SETOF tags_vm_map_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT tags_vm_map_view.*
    FROM tags_vm_map_view
    WHERE vm_id IN (
            SELECT *
            FROM fnSplitterUuid(v_vm_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmTagsByTagId (v_tag_ids VARCHAR(4000))
RETURNS SETOF tags_vm_map_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT tags_vm_map_view.*
    FROM tags_vm_map_view
    WHERE tag_id IN (
            SELECT *
            FROM fnSplitterUuid(v_tag_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmTagsDefaultDisplayType (
    v_tag_id UUID,
    v_vm_id UUID,
    v_DefaultDisplayType INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE tags_vm_map
    SET DefaultDisplayType = v_DefaultDisplayType
    WHERE tags_vm_map.tag_id = v_tag_id
        AND tags_vm_map.vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetnVmTagsByVmId (v_vm_id UUID)
RETURNS SETOF tags_vm_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM tags_vm_map
    WHERE tags_vm_map.vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetnVmTagsByVmIdAndDefaultTag (v_vm_id UUID)
RETURNS SETOF tags_vm_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_vm_map.*
    FROM tags_vm_map
    INNER JOIN tags
        ON tags.tag_id = tags_vm_map.tag_id
    WHERE tags_vm_map.vm_id = v_vm_id
        AND tags.type = 1;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveAllVmTagsByVmId (v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM tags_vm_map
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


