

----------------------------------------------------------------
-- [tags_vm_pool_map] Table
--
CREATE OR REPLACE FUNCTION Inserttags_vm_pool_map (
    v_tag_id UUID,
    v_vm_pool_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO tags_vm_pool_map (
        tag_id,
        vm_pool_id
        )
    VALUES (
        v_tag_id,
        v_vm_pool_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatetags_vm_pool_map (
    v_tag_id INT,
    v_vm_pool_id INT
    )
RETURNS VOID
    --The [tags_vm_pool_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletetags_vm_pool_map (
    v_tag_id UUID,
    v_vm_pool_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM tags_vm_pool_map
    WHERE tag_id = v_tag_id
        AND vm_pool_id = v_vm_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromtags_vm_pool_map ()
RETURNS SETOF tags_vm_pool_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_vm_pool_map.*
    FROM tags_vm_pool_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Gettags_vm_pool_mapBytag_idAndByvm_pool_id (
    v_tag_id UUID,
    v_vm_pool_id UUID
    )
RETURNS SETOF tags_vm_pool_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT tags_vm_pool_map.*
    FROM tags_vm_pool_map
    WHERE tag_id = v_tag_id
        AND vm_pool_id = v_vm_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [tags_vm_pool_map] table doesn't have at least one foreign key column or the foreign keys are also primary keys.
----custom
CREATE OR REPLACE FUNCTION GetTagsByVmpoolId (v_vm_pool_ids VARCHAR(4000))
RETURNS SETOF tags_vm_pool_map_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT tags_vm_pool_map_view.*
    FROM tags_vm_pool_map_view
    WHERE vm_pool_id IN (
            SELECT *
            FROM fnSplitterUuid(v_vm_pool_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;


