
    -- Constraint is not used dropping it to clean the dependency before dropping the function.
    ----------------------------------
-- 		create functions		--
----------------------------------

DROP TYPE IF EXISTS idTextType CASCADE;
DROP TYPE IF EXISTS idUuidType CASCADE;
CREATE TYPE idTextType AS(id text);
CREATE TYPE idUuidType AS(id UUID);


CREATE OR REPLACE FUNCTION getGlobalIds(v_name VARCHAR(4000))
RETURNS UUID
   AS $function$
   DECLARE
   v_id  UUID;
BEGIN
   if (v_name = 'system') then
      v_id := 'AAA00000-0000-0000-0000-123456789AAA';
   else 
      if (v_name = 'everyone') then
         v_id := 'EEE00000-0000-0000-0000-123456789EEE';
      end if;
   end if;
   return  v_id;
END; $function$
LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION public.fnSplitter(ids TEXT)  RETURNS SETOF idTextType AS
$function$
BEGIN
	RETURN QUERY
		SELECT regexp_split_to_table(ids, ',') AS id;
END; $function$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION fnSplitterUuid(ids TEXT)  RETURNS SETOF idUuidType AS
$function$
DECLARE
    tempc REFCURSOR;
    currow TEXT;
    result idUuidType;
BEGIN
  IF ids != '' THEN
    OPEN tempc FOR SELECT regexp_split_to_table(ids, ',');
    FETCH tempc INTO currow;
    WHILE FOUND LOOP
        result := CAST (ROW(currow) AS idUuidType);
        RETURN NEXT result;
        FETCH tempc INTO currow;
   END LOOP;
   CLOSE tempc;
  END IF;
END; $function$
LANGUAGE plpgsql;



--All permissions of current user (include groups)
DROP TYPE IF EXISTS user_permissions CASCADE;
CREATE TYPE user_permissions AS(permission_id UUID, role_id UUID, user_id UUID);
CREATE OR REPLACE FUNCTION public.fn_user_permissions(v_userId IN uuid) RETURNS SETOF user_permissions AS
$function$
DECLARE

BEGIN
	RETURN QUERY
	    SELECT
		    permissions.id AS permission_id,
		    permissions.role_id,
		    permissions.ad_element_id AS user_id
	    FROM permissions
		INNER JOIN users ON permissions.ad_element_id = users.user_id
	    WHERE users.user_id = v_userId

	    UNION

	    SELECT
		    permissions.id AS permission_id,
		    permissions.role_id,
		    temp.user_id AS user_id
	    FROM permissions INNER JOIN
	    (
		    -- get all groups of admin users
		    SELECT ad_groups.id group_id, users.user_id
		    FROM ad_groups, users
		    WHERE ad_groups.id IN
		    (SELECT * FROM fnsplitteruuid(users.group_ids))
			AND users.user_id = v_userId ) temp
		ON permissions.ad_element_id = temp.group_id;

END; $function$
LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION get_all_child_roles_of_role(v_roleId UUID)
RETURNS SETOF idUuidType
   AS $function$
   DECLARE
   SWV_Rs idUuidType;
BEGIN
   BEGIN
      CREATE GLOBAL TEMPORARY TABLE tt_TEMP
      (
         id UUID
      ) WITH OIDS;
      exception when others then
         truncate table tt_TEMP;
   END;
   insert into tt_TEMP(id) values(v_roleId);

   insert into tt_TEMP   with recursive c(role_id,role_container_id)
   as(select role_id AS role_id, role_container_id AS role_container_id from roles_relations
   where role_container_id = v_roleId
   union all
   select t.role_id AS role_id, t.role_container_id AS role_container_id
   from c join roles_relations t on c.role_id = t.role_container_id) select distinct role_id from c;

   FOR SWV_Rs IN(SELECT * FROM  tt_TEMP) LOOP
      RETURN NEXT SWV_Rs;
   END LOOP;
   RETURN;
END; $function$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.fn_get_entity_parents(v_entity_id IN uuid, v_object_type IN int4) RETURNS SETOF idUuidType AS
$function$
/*	Gets a list of all parent GUID to the system root (including)

	Object Types (compatible with VdcObjectType, XXX entries are unused currently)
		Unknown XXX,
		System XXX,
		VM = 2,
		VDS = 3,
		VmTemplate = 4,
		VmPool = 5,
		AdElements XXX,
		Tags XXX,
		Bookmarks XXX,
		VdsGroups = 9,
		MultiLevelAdministration XXX,
		Storage = 11,
		EventNotification XXX,
		ImportExport XXX,
		StoragePool = 14,
		User = 15,
		Role = 16
*/
DECLARE
	v_entity_type int4 := v_object_type;
	system_root_id uuid;
	cluster_id uuid;
	ds_id uuid;
	v_image_id uuid;
	v_storage_id uuid;

BEGIN

	system_root_id := ( SELECT getGlobalIds('system') ); -- hardcoded also in MLA Handler
	CASE
	WHEN v_entity_type = 2 THEN -- VM
        -- get cluster id
		cluster_id := ( SELECT vds_group_id FROM vm_static WHERE vm_guid = v_entity_id );
		-- get data center id
		ds_id := ( SELECT storage_pool_id FROM vds_groups WHERE vds_group_id = cluster_id );

		RETURN QUERY
			SELECT system_root_id AS id
			UNION
			SELECT ds_id AS id
			UNION
			SELECT cluster_id AS id
			UNION
			SELECT v_entity_id AS id;
	WHEN v_entity_type = 3 THEN -- VDS
        -- get cluster id
		cluster_id := ( SELECT vds_group_id FROM vds_static WHERE vds_id = v_entity_id );
		-- get data center id
		ds_id := ( SELECT storage_pool_id FROM vds_groups WHERE vds_group_id = cluster_id );

		RETURN QUERY
			SELECT system_root_id AS id
			UNION
			SELECT ds_id AS id
			UNION
			SELECT cluster_id AS id
			UNION
			SELECT v_entity_id AS id;
	WHEN v_entity_type = 4 THEN -- Template
		-- get image id first
		v_image_id := ( SELECT image_id FROM image_vm_map WHERE vm_id = v_entity_id limit 1);
		-- get the storage id from images
		v_storage_id := ( SELECT storage_id FROM images WHERE image_guid = v_image_id );
		-- finally get data center id
		ds_id := ( SELECT storage_pool_id FROM storage_pool_iso_map WHERE storage_id = v_storage_id );

		RETURN QUERY
			SELECT system_root_id AS id
			UNION
			SELECT ds_id AS id
			UNION
			SELECT v_entity_id AS id;
	WHEN v_entity_type = 5 THEN -- VM Pool
        -- get cluster id
		cluster_id := ( SELECT vds_group_id FROM vm_pools WHERE vm_pool_id = v_entity_id );
		-- get data center id
		ds_id := ( SELECT storage_pool_id FROM vds_groups WHERE vds_group_id = cluster_id );

		RETURN QUERY
			SELECT system_root_id AS id
			UNION
			SELECT ds_id AS id
			UNION
			SELECT cluster_id AS id
			UNION
			SELECT v_entity_id AS id;
	WHEN v_entity_type = 9 THEN -- Cluster
		-- get data center id
		ds_id := ( SELECT storage_pool_id FROM vds_groups WHERE vds_group_id = v_entity_id );

		RETURN QUERY
			SELECT system_root_id AS id
			UNION
			SELECT ds_id AS id
			UNION
			SELECT v_entity_id AS id;
	ELSE
		IF v_entity_type IN ( 1,14,11,15,16 ) THEN -- Data Center, storage, users and roles are under system
			RETURN QUERY
				SELECT system_root_id AS id
				UNION
				SELECT v_entity_id AS id;
		END IF;
	END CASE;
END;$function$
LANGUAGE 'plpgsql';




CREATE OR REPLACE FUNCTION public.fn_get_disk_commited_value_by_storage(v_storage_domain_id IN uuid) RETURNS integer AS
$function$
DECLARE
    result integer;
    mult bigint;

BEGIN
	mult := ( SELECT
	    		COALESCE(SUM(images.size - disk_image_dynamic.actual_size),0)
				FROM images JOIN
	    			disk_image_dynamic ON ( images.image_guid = disk_image_dynamic.image_id )
				WHERE
   					images.storage_id = v_storage_domain_id );
        -- convert to GB from bytes
	mult := CAST((mult * 0.000000000931322574615478515625) AS bigint);
        result := CAST(mult as integer); 

	RETURN result;
END;$function$
LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION fn_get_storage_domain_shared_status_by_domain_id(v_storage_domain_id UUID,
	v_storage VARCHAR(250),
	v_storage_type INTEGER)
RETURNS INTEGER
   AS $function$
   DECLARE
   v_result  INTEGER;
   v_rowsCount  INTEGER;
   v_status  INTEGER;
BEGIN
   BEGIN
      CREATE GLOBAL TEMPORARY TABLE tt_TEMP22
      (
         status INTEGER, 
         count INTEGER
      ) WITH OIDS;
      exception when others then
         truncate table tt_TEMP22;
   END;
   delete from tt_TEMP22;
   Insert INTO tt_TEMP22
   select status, count(storage_id) from storage_pool_iso_map
   where storage_id = v_storage_domain_id
   group by status;

   select   count(*) INTO v_rowsCount from tt_TEMP22;

	-- if return 0 rows then the domain is unattached (0) or locked (4) if storage is null or empty for @storage_type iscsi (2) and fcp (3)
   if (v_rowsCount = 0) then
      if (v_storage_type in(2,3) and (v_storage is null or v_storage = '')) then
         v_result := 4;
      else
         v_result := 0;
      end if;
   else 
      if (v_rowsCount = 1) then
         select   status INTO v_status from tt_TEMP22    LIMIT 1;
			-- if 1 row and status active (3) then domain is active (1)
         if v_status = 3 then
            v_result := 1;
			-- if 1 row and status not active then domain is inactive (2)
         else
            v_result := 2;
         end if;
	-- else (if return more then 1 row) 
      else
         select   count(*) INTO v_rowsCount from tt_TEMP22 where status = 3;
         if (v_rowsCount > 0) then
            v_result := 3;
			-- non of the statuses is active
         else
            v_result := 2;
         end if;
      end if;
   end if;
   return v_result;
END; $function$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.fn_get_ad_element_name(v_ad_element_id IN uuid) RETURNS text AS
$function$
DECLARE
    result text;

BEGIN
   if (v_ad_element_id = getGlobalIds('everyone')) then
      result := 'Everyone';
   else
      select(COALESCE(name,'') || ' ' || COALESCE(surname,'') || ' (' || COALESCE(username,'') || ')') INTO result from users where user_id = v_ad_element_id;
      if (result is null) then
         select   name INTO result from ad_groups where ID = v_ad_element_id;
      end if;
   end if;
   return result;
END; $function$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.fn_get_entity_name(v_entity_id IN uuid, v_object_type IN int4) RETURNS text AS
$function$
/*    Gets object name by its id and type

    Object Types (compatible with VdcObjectType, XXX entries are unused currently)
        Unknown XXX,
        System XXX,
        VM = 2,
        VDS = 3,
        VmTemplate = 4,
        VmPool = 5,
        AdElements XXX,
        Tags XXX,
        Bookmarks XXX,
        VdsGroups = 9,
        MultiLevelAdministration XXX,
        Storage = 11,
        EventNotification XXX,
        ImportExport XXX,
        StoragePool = 14,
        User = 15,
        Role = 16
*/
DECLARE
    v_entity_type int4 := v_object_type;
    result text;

BEGIN
  
    CASE
    WHEN v_entity_type = 1 THEN
        result := 'System';
    WHEN v_entity_type = 2 OR v_entity_type = 4 THEN
        result := ( SELECT vm_name FROM vm_static WHERE vm_guid = v_entity_id );
    WHEN v_entity_type = 3 THEN
        result := ( SELECT vds_name FROM vds_static WHERE vds_id = v_entity_id );
    WHEN v_entity_type = 5 THEN
        result := ( SELECT vm_pool_name FROM vm_pools WHERE vm_pool_id = v_entity_id );
    WHEN v_entity_type = 9 THEN
        result := ( SELECT name FROM vds_groups WHERE vds_group_id = v_entity_id );
    WHEN v_entity_type = 11 THEN
	result := ( SELECT storage_name FROM storage_domain_static WHERE id = v_entity_id );
    WHEN v_entity_type = 14 THEN
        result := ( SELECT name FROM storage_pool WHERE id = v_entity_id );
    ELSE
        result := 'Unknown type ' ||  v_entity_type;
    END CASE;

--      -- This should be written to an error var or include object_id that is missing
--    IF result IS NULL THEN
--        result := v_entity_id || '' NOT FOUND'';
--    END IF;
    RETURN result;
END;$function$
LANGUAGE 'plpgsql';

-- get user and his groups IDs



CREATE OR REPLACE FUNCTION getUserAndGroupsById(v_id UUID)
RETURNS SETOF idUuidType
   AS $function$
   DECLARE
   SWV_Rs idUuidType;
BEGIN
   BEGIN
      CREATE GLOBAL TEMPORARY TABLE tt_TEMP3
      (
         id UUID
      ) WITH OIDS;
      exception when others then
         truncate table tt_TEMP3;
   END;
   insert INTO tt_TEMP3
   select ID from ad_groups,users where users.user_id = v_id
   and ad_groups.id in(select ID from fnsplitteruuid(users.group_ids))
   UNION
   select v_id
   UNION
   -- user is also member of 'Everyone'
   select 'EEE00000-0000-0000-0000-123456789EEE';
   FOR SWV_Rs IN(SELECT * FROM  tt_TEMP3) LOOP
      RETURN NEXT SWV_Rs;
   END LOOP;
   RETURN;
END; $function$
LANGUAGE plpgsql;


-----------------------
-- Quota Functions ----
-----------------------
DROP TYPE IF EXISTS vds_group_usage_rs CASCADE;
CREATE TYPE vds_group_usage_rs AS
    ( virtual_cpu_usage INTEGER,mem_size_mb_usage BIGINT);


-- Summarize the VCPU usage and the RAM usage for all the VMs in the quota which are not down or suspended
-- If vds group id is null, then returns the global usage of the quota, other wise returns only the summarize of all VMs in the specific cluster.
CREATE OR REPLACE FUNCTION CalculateVdsGroupUsage(v_quota_id UUID, v_vds_group_id UUID)
RETURNS SETOF vds_group_usage_rs
AS $function$
BEGIN
    RETURN QUERY SELECT cast(COALESCE(sum(num_of_sockets * cpu_per_socket), 0) as INTEGER) as virtual_cpu_usage,
    COALESCE(sum(mem_size_mb), 0) as mem_size_mb_usage
    FROM vm_static,vm_dynamic
    WHERE quota_id = v_quota_id
      AND vm_dynamic.vm_guid = vm_static.vm_guid
      AND vm_dynamic.status not in (0, 13 , 14, 15)
      AND (v_vds_group_id = vm_static.vds_group_id or v_vds_group_id IS NULL);
END; $function$
LANGUAGE plpgsql;


-- Summarize the storage usage for all the disks in the quota
-- For active disks, we summarize the full size and for snapshots and other disks, we summarize only the actual size.
-- If v_storage_id is null, then return only the global usage of the quota, other wise return only the summarize in the specific storage.
CREATE OR REPLACE FUNCTION CalculateStorageUsage(v_quota_id UUID, v_storage_id UUID)
RETURNS double precision
AS $function$
DECLARE
	v_virtual_size double precision;
	v_actual_size double precision;
BEGIN
	-- Summarize size of all disks that are active.
    SELECT COALESCE(sum(size) / (1024 * 1024 * 1024),0) INTO v_virtual_size
	FROM disk_image_dynamic, images
	WHERE image_guid = disk_image_dynamic.image_id
    AND image_guid in (SELECT image_guid
                       FROM image_vm_map ivm,images
                       WHERE ivm.image_id = images.image_guid and active = 't')
	AND quota_id = v_quota_id
    AND (v_storage_id = images.storage_id or v_storage_id IS NULL);

	-- Summarize the actual size of all the rest disks that are read only disks such as snapshots, not active, template disks.
	SELECT COALESCE(sum(actual_size) / (1024 * 1024 * 1024),0) INTO v_actual_size
	FROM disk_image_dynamic, images
	WHERE image_guid = disk_image_dynamic.image_id
    AND image_guid not in (SELECT image_guid
                           FROM image_vm_map ivm,images
                           WHERE ivm.image_id = images.image_guid and active = 't')
	AND quota_id = v_quota_id
	AND (v_storage_id = images.storage_id or v_storage_id IS NULL);
	RETURN v_actual_size + v_virtual_size;
END; $function$
LANGUAGE plpgsql;



