
    -- Constraint is not used dropping it to clean the dependency before dropping the function.
    ----------------------------------
-- 		create functions		--
----------------------------------

DROP TYPE IF EXISTS idTextType CASCADE;
DROP TYPE IF EXISTS idUuidType CASCADE;
DROP TYPE IF EXISTS booleanResultType CASCADE;
CREATE TYPE idTextType AS(id text);
CREATE TYPE idUuidType AS(id UUID);
CREATE TYPE booleanResultType AS(result BOOLEAN);
DROP TYPE IF EXISTS authzEntryInfoType CASCADE;
CREATE TYPE authzEntryInfoType AS(name text, namespace VARCHAR(2048), authz VARCHAR(255));


CREATE OR REPLACE FUNCTION getGlobalIds(v_name VARCHAR(4000))
RETURNS UUID IMMUTABLE STRICT
   AS $function$
   DECLARE
   v_id  UUID;
BEGIN
   if (v_name = 'system') then
      v_id := 'AAA00000-0000-0000-0000-123456789AAA';
   elsif (v_name = 'everyone') then
      v_id := 'EEE00000-0000-0000-0000-123456789EEE';
   -- bottom is an object which all the objects in the system are its parents
   -- useful to denote we want all objects when checking for permissions
   elsif (v_name = 'bottom') then
      v_id := 'BBB00000-0000-0000-0000-123456789BBB';
   end if;
   return  v_id;
END; $function$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.fnSplitter(ids TEXT)  RETURNS SETOF idTextType IMMUTABLE AS
$function$
BEGIN
	RETURN QUERY
		SELECT regexp_split_to_table(ids, ',') AS id;
END; $function$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION fnSplitterUuid(ids TEXT)  RETURNS SETOF UUID IMMUTABLE AS
$function$
BEGIN
 IF ids != '' THEN
	RETURN QUERY
		SELECT CAST(regexp_split_to_table(ids, ',') AS UUID);
 END IF;
END; $function$
LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION public.fnSplitterWithSeperator(ids TEXT, separator VARCHAR(10))  RETURNS SETOF idTextType IMMUTABLE AS
$function$
BEGIN
  RETURN QUERY
    SELECT regexp_split_to_table(ids, separator) AS id;
END; $function$
LANGUAGE plpgsql;




--All permissions of current user (include groups)
DROP TYPE IF EXISTS user_permissions CASCADE;
CREATE TYPE user_permissions AS(permission_id UUID, role_id UUID, user_id UUID);
CREATE OR REPLACE FUNCTION public.fn_user_permissions(v_userId IN uuid) RETURNS SETOF user_permissions STABLE AS
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


CREATE OR REPLACE FUNCTION public.fn_get_entity_parents(v_entity_id IN uuid, v_object_type IN int4) RETURNS SETOF idUuidType STABLE AS
$function$
/*	Gets a list of all parent GUID to the system root (including)

	Object Types (compatible with VdcObjectType, XXX entries are unused currently)
		Unknown XXX,
		System XXX,
        Bottom = 0,
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
		Role = 16,
		Quota = 17,
		GlusterVolume = 18,
        Disk = 19,
        Network = 20,
        VNICProfile = 27
*/
DECLARE
	v_entity_type int4 := v_object_type;
	system_root_id uuid;
	cluster_id uuid;
	ds_id uuid;
	v_image_id uuid;
	v_storage_id uuid;
    v_vm_id uuid;
    v_storage_pool_id uuid;
    v_profile_network_id uuid;

BEGIN

	system_root_id := ( SELECT getGlobalIds('system') ); -- hardcoded also in MLA Handler
	CASE
	WHEN v_entity_type = 0 THEN -- Bottom
		RETURN QUERY
			SELECT object_id
			FROM permissions;
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
		v_image_id := ( SELECT image_guid FROM images i JOIN vm_device vd ON i.image_group_id = vd.device_id WHERE vm_id = v_entity_id limit 1);
		-- get the storage id from images
		v_storage_id := ( SELECT storage_domain_id FROM image_storage_domain_map WHERE image_id = v_image_id limit 1);
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
	WHEN v_entity_type = 11 THEN -- Storage Domain

		RETURN QUERY
			SELECT system_root_id AS id
			UNION ALL
			SELECT storage_pool_id as id FROM storage_pool_iso_map WHERE storage_id = v_entity_id
			UNION
			SELECT v_entity_id AS id;
	WHEN v_entity_type = 17 THEN -- Quota
		-- get data center id
		ds_id := ( SELECT storage_pool_id FROM quota WHERE id = v_entity_id );

		RETURN QUERY
			SELECT system_root_id AS id
			UNION
			SELECT ds_id AS id
			UNION
			SELECT v_entity_id AS id;
	WHEN v_entity_type = 18 THEN -- GlusterVolume
        -- get cluster id
		cluster_id := ( SELECT v.cluster_id FROM gluster_volumes v WHERE id = v_entity_id );
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

	WHEN v_entity_type = 19 THEN -- Disk

        -- get data center, storage domain and vm
        SELECT INTO ds_id, v_storage_id, v_vm_id
                    storage_pool_id, storage_id, vm_id
        FROM images_storage_domain_view
        LEFT OUTER JOIN vm_device ON vm_device.device_id = images_storage_domain_view.disk_id
        WHERE image_group_id = v_entity_id;

        -- get cluster
        cluster_id := ( SELECT vds_group_id FROM vm_static WHERE vm_guid = v_vm_id );

        RETURN QUERY
            SELECT system_root_id AS id
            UNION
            SELECT ds_id AS id
            UNION
            SELECT v_storage_id AS id
            UNION
            SELECT v_vm_id AS id
            UNION
            SELECT cluster_id AS id
            UNION
            SELECT v_entity_id AS id;

	WHEN v_entity_type = 20 THEN -- Network

        SELECT INTO v_storage_pool_id
                    network.storage_pool_id
        FROM network
        WHERE network.id = v_entity_id;

        RETURN QUERY
            SELECT system_root_id AS id
            UNION
            SELECT v_storage_pool_id AS id
            UNION
            SELECT v_entity_id AS id;

	WHEN v_entity_type = 27 THEN -- VNICProfile

        SELECT INTO v_profile_network_id
                    vnic_profiles.network_id
        FROM vnic_profiles
        WHERE vnic_profiles.id = v_entity_id;
        SELECT INTO v_storage_pool_id
                    network.storage_pool_id
        FROM network
        WHERE network.id = v_profile_network_id;

        RETURN QUERY
            SELECT system_root_id AS id
            UNION
            SELECT v_storage_pool_id AS id
            UNION
            SELECT v_profile_network_id AS id
            UNION
            SELECT v_entity_id AS id;

    WHEN v_entity_type = 23 THEN -- Gluster Hook

        -- get cluster id
        cluster_id := ( SELECT cluster_id FROM gluster_hooks WHERE id = v_entity_id );
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

	WHEN v_entity_type = 25 THEN -- Gluster Service

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
	ELSE
		IF v_entity_type IN ( 1,14,15,16 ) THEN -- Data Center, users and roles are under system
			RETURN QUERY
				SELECT system_root_id AS id
				UNION
				SELECT v_entity_id AS id;
		END IF;
	END CASE;
END;$function$
LANGUAGE 'plpgsql';




CREATE OR REPLACE FUNCTION public.fn_get_disk_commited_value_by_storage(v_storage_domain_id IN uuid) RETURNS integer STABLE AS
$function$
DECLARE
    result integer;
    mult bigint;

BEGIN
	mult := ( SELECT
	    		COALESCE(SUM(images_storage_domain_view.size),0)
				FROM images_storage_domain_view
				WHERE images_storage_domain_view.storage_id = v_storage_domain_id );
        -- convert to GB from bytes
	mult := CAST((mult * 0.000000000931322574615478515625) AS bigint);
    result := CAST(mult as integer);

	RETURN result;
END;$function$
LANGUAGE 'plpgsql';




CREATE OR REPLACE FUNCTION public.fn_get_actual_images_size_by_storage(v_storage_domain_id IN uuid) RETURNS integer STABLE AS
$function$
DECLARE
    result integer;
    mult bigint;

BEGIN
	mult := ( SELECT
	    		COALESCE(SUM(disk_image_dynamic.actual_size),0)
				FROM images_storage_domain_view JOIN
	    			disk_image_dynamic ON ( images_storage_domain_view.image_guid = disk_image_dynamic.image_id )
				WHERE images_storage_domain_view.storage_id = v_storage_domain_id );
        -- convert to GB from bytes
	mult := CAST((mult * 0.000000000931322574615478515625) AS bigint);
    result := CAST(mult as integer);

	RETURN result;
END;$function$
LANGUAGE 'plpgsql';




CREATE OR REPLACE FUNCTION fn_get_storage_domain_shared_status_by_domain_id(v_storage_domain_id UUID,
	v_storage_status INTEGER,
	v_storage_domain_type INTEGER)
RETURNS INTEGER
   AS $function$
   DECLARE
   v_result  INTEGER;
   v_rowsCount  INTEGER;
   v_status  INTEGER;
BEGIN
    if (v_storage_domain_type != 2) then
      if (v_storage_status is null) then
         v_result := 0;
      else
         -- if 1 row and status active (3) then domain is active (1)
         if v_storage_status = 3 then
            v_result := 1;
		 -- if 1 row and status not active then domain is inactive (2)
         else
            v_result := 2;
         end if;
      end if;
    else
      BEGIN
         CREATE TEMPORARY TABLE tt_TEMP22
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

      select count(*) INTO v_rowsCount from tt_TEMP22;

      -- if return 0 rows then the domain is unattached
      if (v_rowsCount = 0) then
         v_result := 0;
      else
         if (v_rowsCount = 1) then
	        -- if 1 row and status active (3) then domain is active (1)
            if v_storage_status = 3 then
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
    end if;
   return v_result;
END; $function$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.fn_authz_entry_info(v_ad_element_id IN uuid) RETURNS AuthzEntryInfoType STABLE AS
$function$
DECLARE
    result authzEntryInfoType;

BEGIN
   if (v_ad_element_id = getGlobalIds('everyone')) then
      select 'Everyone','*','' into result;
   else
      select(COALESCE(name,'') || ' ' || COALESCE(surname,'') || ' (' || COALESCE(username,'') || '@' || COALESCE(domain,'') || ')'), namespace, domain INTO result from users where user_id = v_ad_element_id;
      if (result is null) then
         select   name, namespace, domain INTO result from ad_groups where ID = v_ad_element_id;
      end if;
   end if;
   return result;
END; $function$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION public.fn_get_ad_element_name(v_ad_element_id IN uuid) RETURNS text STABLE AS
$function$
DECLARE
    result text;

BEGIN
   if (v_ad_element_id = getGlobalIds('everyone')) then
      result := 'Everyone';
   else
      select(COALESCE(name,'') || ' ' || COALESCE(surname,'') || ' (' || COALESCE(username,'') || '@' || COALESCE(domain,'') || ')') INTO result from users where user_id = v_ad_element_id;
      if (result is null) then
         select   name INTO result from ad_groups where ID = v_ad_element_id;
      end if;
   end if;
   return result;
END; $function$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.fn_get_entity_name(v_entity_id IN uuid, v_object_type IN int4) RETURNS text STABLE AS
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
        Role = 16,
        Quota = 17,
        GlusterVolume = 18,
        Disk = 19,
        Network = 20,
        VNICProfile = 27
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
    WHEN v_entity_type = 7 THEN
        result := ( SELECT tag_name FROM tags WHERE tag_id = v_entity_id );
    WHEN v_entity_type = 8 THEN
        result := ( SELECT bookmark_name FROM bookmarks WHERE bookmark_id = v_entity_id );
    WHEN v_entity_type = 9 THEN
        result := ( SELECT name FROM vds_groups WHERE vds_group_id = v_entity_id );
    WHEN v_entity_type = 11 THEN
        result := ( SELECT storage_name FROM storage_domain_static WHERE id = v_entity_id );
    WHEN v_entity_type = 14 THEN
        result := ( SELECT name FROM storage_pool WHERE id = v_entity_id );
    WHEN v_entity_type = 15 THEN
        result := ( SELECT username FROM users WHERE user_id = v_entity_id );
    WHEN v_entity_type = 16 THEN
        result := ( SELECT name FROM roles WHERE id = v_entity_id );
    WHEN v_entity_type = 17 THEN
        result := ( SELECT quota_name FROM quota WHERE id = v_entity_id );
   WHEN v_entity_type = 18 THEN
        result := ( SELECT vol_name FROM gluster_volumes WHERE id = v_entity_id );
    WHEN v_entity_type = 19 THEN
        result := ( SELECT disk_alias FROM base_disks WHERE disk_id = v_entity_id );
    WHEN v_entity_type = 20 THEN
        result := ( SELECT name FROM network WHERE id = v_entity_id );
    WHEN v_entity_type = 23 THEN
        result := ( SELECT concat(gluster_command,'-',stage,'-',name) FROM gluster_hooks where id = v_entity_id );
    WHEN v_entity_type = 25 THEN
        result := ( SELECT service_name FROM gluster_services where id = v_entity_id );
    WHEN v_entity_type = 27 THEN
        result := ( SELECT name FROM vnic_profiles where id = v_entity_id );
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
RETURNS SETOF idUuidType STABLE
   AS $function$
BEGIN
   RETURN QUERY
   select ID from ad_groups,users where users.user_id = v_id
   and ad_groups.id in(select * from fnsplitteruuid(users.group_ids))
   UNION
   select v_id
   UNION
   -- user is also member of 'Everyone'
   select 'EEE00000-0000-0000-0000-123456789EEE';
END; $function$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION getElementIdsByIdAndGroups(v_id UUID,v_group_ids text)
RETURNS SETOF idUuidType STABLE
   AS $function$
BEGIN
  RETURN QUERY
  select * from fnsplitteruuid(v_group_ids)
  UNION
  select v_id
  UNION
  -- user is also member of 'Everyone'
  select 'EEE00000-0000-0000-0000-123456789EEE';
END; $function$
LANGUAGE plpgsql;

-----------------------
-- Quota Functions ----
-----------------------
DROP TYPE IF EXISTS vds_group_usage_rs CASCADE;
CREATE TYPE vds_group_usage_rs AS
    ( virtual_cpu_usage INTEGER,mem_size_mb_usage BIGINT);

-- returns a set of integers representing vm statuses on which the vm shouldn't
-- be used for quota calculation
CREATE OR REPLACE FUNCTION getNonCountableQutoaVmStatuses()
RETURNS SETOF INTEGER IMMUTABLE
    AS $BODY$
BEGIN
    RETURN query select 0 union select 13 union select 14 union select 15;
--(Down(0), Suspended(13), ImageIllegal(14), ImageLocked(15))
END; $BODY$
LANGUAGE plpgsql;


-- Summarize the VCPU usage and the RAM usage for all the VMs in the quota which are not down or suspended
-- If vds group id is null, then returns the global usage of the quota, other wise returns only the summarize of all VMs in the specific cluster.
-- NOTE: VmDynamic status (0/13/14/15) must be persistent with UpdateVmCommand
CREATE OR REPLACE FUNCTION CalculateVdsGroupUsage(v_quota_id UUID, v_vds_group_id UUID)
RETURNS SETOF vds_group_usage_rs STABLE
AS $function$
BEGIN
    RETURN QUERY SELECT cast(COALESCE(sum(num_of_sockets * cpu_per_socket), 0) as INTEGER) as virtual_cpu_usage,
    COALESCE(sum(mem_size_mb), 0) as mem_size_mb_usage
    FROM vm_static,vm_dynamic
    WHERE quota_id = v_quota_id
      AND vm_dynamic.vm_guid = vm_static.vm_guid
      AND vm_dynamic.status not in (SELECT getNonCountableQutoaVmStatuses())
      AND (v_vds_group_id = vm_static.vds_group_id or v_vds_group_id IS NULL);
END; $function$
LANGUAGE plpgsql;


DROP TYPE IF EXISTS all_vds_group_usage_rs CASCADE;
CREATE TYPE all_vds_group_usage_rs AS
    (quota_vds_group_id UUID, quota_id UUID,vds_group_id UUID,vds_group_name character varying(40),virtual_cpu INTEGER,virtual_cpu_usage INTEGER,mem_size_mb BIGINT,mem_size_mb_usage BIGINT);


-- Summarize the VCPU usage and the RAM usage for all the VMs in the quota which are not down or suspended
-- If vds group id is null, then returns the global usage of the quota, otherwise returns only the sum of all VMs in the specific cluster.
-- NOTE: VmDynamic status (0/13/14/15) must be persistent with UpdateVmCommand
CREATE OR REPLACE FUNCTION calculateAllVdsGroupUsage()
RETURNS SETOF all_vds_group_usage_rs STABLE
AS $function$
BEGIN
    RETURN QUERY SELECT
        quota_limitation.id AS quota_vds_group_id,
        quota_limitation.quota_id as quota_id,
        quota_limitation.vds_group_id as vds_group_id,
        vds_groups.name AS vds_group_name,
        quota_limitation.virtual_cpu,
        cast(COALESCE(sum(num_of_sockets * cpu_per_socket * cast(vm_dynamic.status not in (SELECT getNonCountableQutoaVmStatuses()) as INTEGER)), 0) as INTEGER) as virtual_cpu_usage,
        quota_limitation.mem_size_mb,
        COALESCE(sum(vm_static.mem_size_mb), 0) as mem_size_mb_usage
    FROM quota_limitation
        LEFT JOIN vm_static ON vm_static.quota_id = quota_limitation.quota_id
        LEFT JOIN vm_dynamic ON vm_dynamic.vm_guid = vm_static.vm_guid
        LEFT JOIN vds_groups ON vds_groups.vds_group_id = vm_static.vds_group_id
    WHERE quota_limitation.virtual_cpu IS NOT NULL
        AND quota_limitation.mem_size_mb IS NOT NULL
    GROUP BY quota_limitation.quota_id, quota_limitation.vds_group_id, vds_group_name, quota_limitation.virtual_cpu, quota_limitation.mem_size_mb,
        vm_static.quota_id, vds_groups.vds_group_id, vm_static.vds_group_id, quota_limitation.id;
END; $function$
LANGUAGE plpgsql;



DROP TYPE IF EXISTS all_storage_usage_rs CASCADE;
CREATE TYPE all_storage_usage_rs AS
    (quota_storage_id UUID,quota_id UUID,storage_id UUID,storage_name character varying(250),storage_size_gb BIGINT,storage_size_gb_usage double precision);


CREATE OR REPLACE FUNCTION calculateAllStorageUsage()
RETURNS SETOF all_storage_usage_rs STABLE
AS $function$
BEGIN
    -- Summarize size of all disks that are active.
    RETURN QUERY SELECT
        quota_limitation.id AS quota_storage_id,
        quota_limitation.quota_id as quota_id,
        quota_limitation.storage_id as storage_id,
        storage_domain_static.storage_name,
        quota_limitation.storage_size_gb,
        cast(COALESCE(sum(size * cast(active as integer) + disk_image_dynamic.actual_size * cast((not active) as integer)) / 1073741824 ,0) as double precision)  as storage_usage -- 1073741824 is 1024^3 (for GB)
    FROM quota_limitation
        LEFT JOIN image_storage_domain_map ON quota_limitation.quota_id = image_storage_domain_map.quota_id
        LEFT JOIN images ON images.image_guid = image_storage_domain_map.image_id
        LEFT JOIN disk_image_dynamic ON images.image_guid = disk_image_dynamic.image_id
        LEFT JOIN storage_domain_static ON image_storage_domain_map.storage_domain_id = storage_domain_static.id
    WHERE quota_limitation.storage_size_gb IS NOT NULL
    GROUP BY quota_limitation.quota_id, storage_id,quota_limitation.id,storage_domain_static.storage_name,quota_limitation.storage_size_gb;
END; $function$
LANGUAGE plpgsql;


-- Summarize the storage usage for all the disks in the quota
-- For active disks, we summarize the full size and for snapshots and other disks, we summarize only the actual size.
-- If v_storage_id is null, then return only the global usage of the quota, other wise return only the summarize in the specific storage.
CREATE OR REPLACE FUNCTION CalculateStorageUsage(v_quota_id UUID, v_storage_id UUID)
RETURNS double precision STABLE
AS $function$
DECLARE
	v_virtual_size double precision;
	v_actual_size double precision;
BEGIN
	-- Summarize size of all disks that are active.
    SELECT COALESCE(sum(size) / (1024 * 1024 * 1024),0) INTO v_virtual_size
	FROM disk_image_dynamic, images_storage_domain_view
	WHERE image_guid = disk_image_dynamic.image_id
    AND image_guid in (SELECT image_guid FROM  images WHERE  active = TRUE)
	AND quota_id = v_quota_id
    AND (v_storage_id = images_storage_domain_view.storage_id or v_storage_id IS NULL);

	-- Summarize the actual size of all the rest disks that are read only disks such as snapshots, not active, template disks.
	SELECT COALESCE(sum(disk_image_dynamic.actual_size) / (1024 * 1024 * 1024),0) INTO v_actual_size
	FROM disk_image_dynamic, images_storage_domain_view
	WHERE image_guid = disk_image_dynamic.image_id
    AND image_guid not in (SELECT image_guid
                           FROM   images i JOIN vm_device vd ON i.image_group_id = vd.device_id
                           WHERE  active = TRUE)
	AND quota_id = v_quota_id
	AND (v_storage_id = images_storage_domain_view.storage_id or v_storage_id IS NULL);
	RETURN v_actual_size + v_virtual_size;
END; $function$
LANGUAGE plpgsql;


--
-- Create the sequence used to generate UUIDs:
--
create or replace function create_uuid_sequence() returns void
as $procedure$
begin
  if not exists (select 1 from information_schema.sequences where sequence_name = 'uuid_sequence') then
    create sequence uuid_sequence increment by 1 start with 1;
  end if;
end; $procedure$
language plpgsql;

select create_uuid_sequence();

drop function create_uuid_sequence();


--
-- This function replaces the same function from the uuid-ossp extension
-- so that we don't need that extension any more:
--
create or replace function uuid_generate_v1() returns uuid STABLE
as $procedure$
declare
    v_val bigint;
    v_4_part char(4);
    v_8_part char(8);
    v_12_part char(12);
    v_4_part_max int;
begin
    -- The only part we should use modulo is the 4 digit part, all the
    -- rest are really big numbers (i.e 16^8 - 1 and 16^12 - 1)
    -- The use of round(random() * 1000 is for getting a different id
    -- for DC/Cluster in different installations
    v_4_part_max = 65535; -- this is 16^4 -1
    v_val := nextval('uuid_sequence');
    v_4_part := lpad(to_hex(v_val % v_4_part_max), 4, '0');
    v_8_part := lpad(to_hex(v_val), 8, '0');
    v_12_part := lpad(to_hex((v_val + (round(random() * 1000))::bigint)), 12, '0');
    return v_8_part || v_4_part || v_4_part || v_4_part || v_12_part;
end; $procedure$
language plpgsql;

-- This function turns a string of IP addresses to an array of IP
-- addreses, in order to correct sorting.
CREATE OR REPLACE FUNCTION fn_get_comparable_ip_list(text) RETURNS inet[] IMMUTABLE STRICT
AS $procedure$
BEGIN
CASE
    WHEN ($1 IS NULL) OR ($1 ~ E'^\s*$') THEN
        RETURN NULL;
    ELSE
        RETURN regexp_split_to_array(trim(both from $1), E'\s+')::inet[];
END CASE;
END; $procedure$
LANGUAGE plpgsql;
