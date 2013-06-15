----------------------------------------------------------------
-- [tags_vm_pool_map] Table
--


Create or replace FUNCTION Inserttags_vm_pool_map(v_tag_id UUID,
 v_vm_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO tags_vm_pool_map(tag_id, vm_pool_id)
	VALUES(v_tag_id, v_vm_pool_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatetags_vm_pool_map(v_tag_id INTEGER,
	v_vm_pool_id INTEGER)
RETURNS VOID

	--The [tags_vm_pool_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletetags_vm_pool_map(v_tag_id UUID,
 v_vm_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM tags_vm_pool_map
   WHERE tag_id = v_tag_id AND vm_pool_id = v_vm_pool_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromtags_vm_pool_map() RETURNS SETOF tags_vm_pool_map
   AS $procedure$
BEGIN

   RETURN QUERY SELECT tags_vm_pool_map.*
   FROM tags_vm_pool_map;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Gettags_vm_pool_mapBytag_idAndByvm_pool_id(v_tag_id UUID,
 v_vm_pool_id UUID) RETURNS SETOF tags_vm_pool_map
   AS $procedure$
BEGIN

   RETURN QUERY SELECT tags_vm_pool_map.*
   FROM tags_vm_pool_map
   WHERE tag_id = v_tag_id AND vm_pool_id = v_vm_pool_id;

END; $procedure$
LANGUAGE plpgsql;




--The GetByFK stored procedure cannot be created because the [tags_vm_pool_map] table doesn't have at least one foreign key column or the foreign keys are also primary keys.


----custom
Create or replace FUNCTION GetTagsByVmpoolId(v_vm_pool_ids VARCHAR(4000)) RETURNS SETOF tags_vm_pool_map_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT tags_vm_pool_map_view.*
      FROM tags_vm_pool_map_view
      WHERE vm_pool_id in(select * from fnSplitterUuid(v_vm_pool_ids));
END; $procedure$
LANGUAGE plpgsql;
