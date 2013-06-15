

----------------------------------------------------------------
-- [ad_groups] Table
--




Create or replace FUNCTION Insertad_groups(v_id UUID,
	v_name VARCHAR(255),
	v_status INTEGER,
	v_domain VARCHAR(100),
	v_distinguishedname VARCHAR(4000))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO ad_groups(id, name,status,domain,distinguishedname)
	VALUES(v_id, v_name,v_status,v_domain,v_distinguishedname);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatead_groups(v_id UUID,
	v_name VARCHAR(255),
	v_status INTEGER,
	v_domain VARCHAR(100),
	v_distinguishedname VARCHAR(4000))
RETURNS VOID

	--The [ad_groups] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE ad_groups
      SET name = v_name,status = v_status,domain = v_domain,distinguishedname =v_distinguishedname
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletead_groups(v_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM tags_user_group_map
      WHERE group_id = v_id;
      DELETE FROM ad_groups
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromad_groups() RETURNS SETOF ad_groups
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getad_groupsByid(v_id UUID) RETURNS SETOF ad_groups
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Getad_groupsByName(v_name VARCHAR(256)) RETURNS SETOF ad_groups
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM ad_groups
      WHERE name = v_name;
END; $procedure$
LANGUAGE plpgsql;

