

----------------------------------------------------------------
-- [ad_groups] Table
--




Create or replace FUNCTION InsertGroup(v_id UUID,
	v_name VARCHAR(255),
	v_domain VARCHAR(100),
	v_distinguishedname VARCHAR(4000),
	v_external_id TEXT,
	v_namespace VARCHAR(2048))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO ad_groups(id, name,domain,distinguishedname,external_id, namespace)
	VALUES(v_id, v_name,v_domain,v_distinguishedname,v_external_id, v_namespace);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateGroup(v_id UUID,
	v_name VARCHAR(255),
	v_domain VARCHAR(100),
	v_distinguishedname VARCHAR(4000),
	v_external_id TEXT,
	v_namespace VARCHAR(2048))
RETURNS VOID

	--The [ad_groups] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE ad_groups
      SET name = v_name,domain = v_domain,distinguishedname = v_distinguishedname,external_id = v_external_id, namespace = v_namespace
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteGroup(v_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM tags_user_group_map
      WHERE group_id = v_id;
      DELETE FROM ad_groups
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllGroups() RETURNS SETOF ad_groups STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetGroupById(v_id UUID) RETURNS SETOF ad_groups STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetGroupsByUserId(v_id UUID) RETURNS SETOF ad_groups STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups
      WHERE id in (SELECT fnsplitteruuid(users.group_ids) from users where user_id = v_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetGroupByExternalId(v_domain VARCHAR(100), v_external_id TEXT) RETURNS SETOF ad_groups STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups
      WHERE domain = v_domain AND external_id = v_external_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetGroupByName(v_name VARCHAR(256)) RETURNS SETOF ad_groups STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups
      WHERE name = v_name;
END; $procedure$
LANGUAGE plpgsql;

