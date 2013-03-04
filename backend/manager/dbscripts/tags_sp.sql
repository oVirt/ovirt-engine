

----------------------------------------------------------------
-- [tags] Table
--




Create or replace FUNCTION Inserttags(v_description VARCHAR(4000) ,
	v_tag_id UUID ,
	v_tag_name VARCHAR(50),
	v_parent_id UUID,
	v_readonly BOOLEAN ,
    v_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      INSERT INTO tags(tag_id,description, tag_name,parent_id,readonly,type)
	VALUES(v_tag_id,v_description, v_tag_name,v_parent_id,v_readonly,v_type);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatetags(v_description VARCHAR(4000) ,
	v_tag_id UUID,
	v_tag_name VARCHAR(50),
	v_parent_id UUID,
	v_readonly BOOLEAN ,
    v_type INTEGER)
RETURNS VOID

	--The [tags] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE tags
      SET description = v_description,tag_name = v_tag_name,parent_id = v_parent_id,
      readonly	= v_readonly,type = v_type,_update_date = LOCALTIMESTAMP
      WHERE tag_id = v_tag_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletetags(v_tag_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
			-- Get (and keep) a shared lock with "right to upgrade to exclusive"
			-- in order to force locking parent before children
      select   tag_id INTO v_val FROM tags  WHERE tag_id = v_tag_id     FOR UPDATE;
      DELETE FROM tags_user_group_map
      WHERE tag_id = v_tag_id;
      DELETE FROM tags_user_map
      WHERE tag_id = v_tag_id;
      DELETE FROM tags_vm_map
      WHERE tag_id = v_tag_id;
      DELETE FROM tags_vds_map
      WHERE tag_id = v_tag_id;
      DELETE FROM tags_vm_pool_map
      WHERE tag_id = v_tag_id;
      DELETE FROM tags
      WHERE tag_id = v_tag_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromtags() RETURNS SETOF tags
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags.*
      FROM tags;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GettagsBytag_id(v_tag_id UUID) RETURNS SETOF tags
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags.*
      FROM tags
      WHERE tag_id = v_tag_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GettagsByparent_id(v_parent_id UUID) RETURNS SETOF tags
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags.*
      FROM tags
      WHERE parent_id = v_parent_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GettagsBytag_name(v_tag_name VARCHAR(50)) RETURNS SETOF tags
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags.*
      FROM tags
      WHERE tag_name = v_tag_name;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [tags_user_group_map] Table
--


Create or replace FUNCTION Inserttags_user_group_map(v_group_id UUID,
 v_tag_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO tags_user_group_map(group_id, tag_id)
	VALUES(v_group_id, v_tag_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletetags_user_group_map(v_group_id UUID,
 v_tag_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM tags_user_group_map
      WHERE group_id = v_group_id AND tag_id = v_tag_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromtags_user_group_map() RETURNS SETOF tags_user_group_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_user_group_map.*
      FROM tags_user_group_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetTagUserGroupByGroupIdAndByTagId(v_group_id UUID,
 v_tag_id UUID) RETURNS SETOF tags_user_group_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_user_group_map.*
      FROM tags_user_group_map
      WHERE group_id = v_group_id AND tag_id = v_tag_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Gettags_user_group_mapByTagName(v_tag_name VARCHAR(50)) RETURNS SETOF tags_user_group_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_user_group_map_view.*
      FROM tags_user_group_map_view
      WHERE tag_name = v_tag_name;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetTagsByUserGroupId(v_group_ids VARCHAR(4000)) RETURNS SETOF tags_user_group_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_user_group_map_view.*
      FROM tags_user_group_map_view
      WHERE group_id in(select * from fnSplitterUuid(v_group_ids));
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetUserGroupTagsByTagIds(v_tag_ids VARCHAR(4000)) RETURNS SETOF tags_user_group_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_user_group_map_view.*
      FROM tags_user_group_map_view
      WHERE tag_id in(select * from fnSplitterUuid(v_tag_ids));
END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [tags_user_map] Table
--


Create or replace FUNCTION Inserttags_user_map(v_tag_id UUID,
 v_user_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO tags_user_map(tag_id, user_id)
	VALUES(v_tag_id, v_user_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletetags_user_map(v_tag_id UUID,
 v_user_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM tags_user_map
      WHERE tag_id = v_tag_id AND user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromtags_user_map() RETURNS SETOF tags_user_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_user_map.*
      FROM tags_user_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetTagUserByTagIdAndByuserId(v_tag_id UUID,
 v_user_id UUID) RETURNS SETOF tags_user_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_user_map.*
      FROM tags_user_map
      WHERE tag_id = v_tag_id AND user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetTagsByUserId(v_user_ids VARCHAR(4000)) RETURNS SETOF tags_user_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_user_map_view.*
      FROM tags_user_map_view
      WHERE user_id in(select * from fnSplitterUuid(v_user_ids));
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetUserTagsByTagIds(v_tag_ids VARCHAR(4000)) RETURNS SETOF tags_user_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_user_map_view.*
      FROM tags_user_map_view
      WHERE tag_id in(select * from fnSplitterUuid(v_tag_ids));
END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [tags_vds_map] Table
--


Create or replace FUNCTION Inserttags_vds_map(v_tag_id UUID,
 v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO tags_vds_map(tag_id, vds_id)
	VALUES(v_tag_id, v_vds_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletetags_vds_map(v_tag_id UUID,
 v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM tags_vds_map
      WHERE tag_id = v_tag_id AND vds_id = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromtags_vds_map() RETURNS SETOF tags_vds_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_vds_map.*
      FROM tags_vds_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetTagVdsBytagIdAndByVdsId(v_tag_id UUID,
 v_vds_id UUID) RETURNS SETOF tags_vds_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_vds_map.*
      FROM tags_vds_map
      WHERE tag_id = v_tag_id AND vds_id = v_vds_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Gettags_vds_mapByTagName(v_tag_name VARCHAR(50)) RETURNS SETOF tags_vds_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_vds_map_view.*
      FROM tags_vds_map_view
      WHERE tag_name = v_tag_name;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetTagsByVdsId(v_vds_ids VARCHAR(4000)) RETURNS SETOF tags_vds_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_vds_map_view.*
      FROM tags_vds_map_view
      WHERE vds_id in(select * from fnSplitterUuid(v_vds_ids));
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVdsTagsByTagIds(v_tag_ids VARCHAR(4000)) RETURNS SETOF tags_vds_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_vds_map_view.*
      FROM tags_vds_map_view
      WHERE tag_id in(select * from fnSplitterUuid(v_tag_ids));
END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [tags_vm_map] Table
--


Create or replace FUNCTION Inserttags_vm_map(v_tag_id UUID,
 v_vm_id UUID,
    v_DefaultDisplayType INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO tags_vm_map(tag_id, vm_id, DefaultDisplayType)
	VALUES(v_tag_id, v_vm_id, v_DefaultDisplayType);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletetags_vm_map(v_tag_id UUID,
 v_vm_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM tags_vm_map
      WHERE tag_id = v_tag_id AND vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetAllFromtags_vm_map() RETURNS SETOF tags_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_vm_map.*
      FROM tags_vm_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetTagVmByTagIdAndByvmId(v_tag_id UUID,
 v_vm_id UUID) RETURNS SETOF tags_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_vm_map.*
      FROM tags_vm_map
      WHERE tag_id = v_tag_id AND vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Gettags_vm_mapByTagName(v_tag_name VARCHAR(50)) RETURNS SETOF tags_vm_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT tags_vm_map_view.*
      FROM tags_vm_map_view
      WHERE tag_name = v_tag_name;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetTagsByVmId(v_vm_ids VARCHAR(4000)) RETURNS SETOF tags_vm_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_vm_map_view.*
      FROM tags_vm_map_view
      WHERE vm_id in(select * from fnSplitterUuid(v_vm_ids));
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVmTagsByTagId(v_tag_ids VARCHAR(4000)) RETURNS SETOF tags_vm_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_vm_map_view.*
      FROM tags_vm_map_view
      WHERE tag_id in(select * from fnSplitterUuid(v_tag_ids));
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateVmTagsDefaultDisplayType(v_tag_id UUID,
	v_vm_id UUID,
    v_DefaultDisplayType INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      update tags_vm_map
      set DefaultDisplayType = v_DefaultDisplayType
      where tags_vm_map.tag_id = v_tag_id
      and tags_vm_map.vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetnVmTagsByVmId(v_vm_id UUID) RETURNS SETOF tags_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY select * from tags_vm_map
      where tags_vm_map.vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetnVmTagsByVmIdAndDefaultTag(v_vm_id UUID) RETURNS SETOF tags_vm_map
   AS $procedure$
BEGIN
      RETURN QUERY select tags_vm_map.* from tags_vm_map
      inner join tags on tags.tag_id = tags_vm_map.tag_id
      where tags_vm_map.vm_id = v_vm_id
      and tags.type = 1;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION RemoveAllVmTagsByVmId(v_vm_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      delete FROM tags_vm_map
      where vm_id = v_vm_id;
END; $procedure$
LANGUAGE plpgsql;




