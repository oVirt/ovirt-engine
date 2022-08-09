

----------------------------------------------------------------
-- [ad_groups] Table
--
CREATE OR REPLACE FUNCTION InsertGroup (
    v_id UUID,
    v_name VARCHAR(255),
    v_domain VARCHAR(100),
    v_distinguishedname VARCHAR(4000),
    v_external_id TEXT,
    v_namespace VARCHAR(2048)
    )
RETURNS VOID AS $FUNCTION$
BEGIN
    INSERT INTO ad_groups (
        id,
        name,
        domain,
        distinguishedname,
        external_id,
        namespace
        )
    VALUES (
        v_id,
        v_name,
        v_domain,
        v_distinguishedname,
        v_external_id,
        v_namespace
        );
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGroup (
    v_id UUID,
    v_name VARCHAR(255),
    v_domain VARCHAR(100),
    v_distinguishedname VARCHAR(4000),
    v_external_id TEXT,
    v_namespace VARCHAR(2048)
    )
RETURNS VOID
    --The [ad_groups] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $FUNCTION$
BEGIN
    UPDATE ad_groups
    SET name = v_name,
        domain = v_domain,
        distinguishedname = v_distinguishedname,
        external_id = v_external_id,
        namespace = v_namespace
    WHERE id = v_id;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGroup (v_id UUID)
RETURNS VOID AS $FUNCTION$
BEGIN
    DELETE
    FROM tags_user_group_map
    WHERE group_id = v_id;

    DELETE
    FROM ad_groups
    WHERE id = v_id;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllGroups ()
RETURNS SETOF ad_groups STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM ad_groups;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGroupById (v_id UUID)
RETURNS SETOF ad_groups STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM ad_groups
    WHERE id = v_id;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGroupByExternalId (
    v_domain VARCHAR(100),
    v_external_id TEXT
    )
RETURNS SETOF ad_groups STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM ad_groups
    WHERE domain = v_domain
        AND external_id = v_external_id;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGroupByName (v_name VARCHAR(256))
RETURNS SETOF ad_groups STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM ad_groups
    WHERE name = v_name;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGroupByNameAndDomain (v_name VARCHAR(256), v_domain VARCHAR(256))
RETURNS SETOF ad_groups STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM ad_groups
    WHERE name = v_name
      AND domain = v_domain;
END;$FUNCTION$
LANGUAGE plpgsql;


