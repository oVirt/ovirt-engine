

-- Constraint is not used dropping it to clean the dependency before dropping the function.
----------------------------------
--        create functions      --
----------------------------------
DROP TYPE IF EXISTS idTextType CASCADE;
CREATE TYPE idtexttype AS (id TEXT);

DROP TYPE IF EXISTS idUuidType CASCADE;
CREATE TYPE iduuidtype AS (id uuid);

DROP TYPE IF EXISTS booleanResultType CASCADE;
CREATE TYPE booleanresulttype AS (result boolean);

DROP TYPE IF EXISTS authzEntryInfoType CASCADE;
CREATE TYPE authzentryinfotype AS (
    name TEXT,
    namespace VARCHAR(2048),
    authz VARCHAR(255)
);

CREATE OR REPLACE FUNCTION getGlobalIds (v_name VARCHAR(4000))
RETURNS UUID IMMUTABLE STRICT AS $FUNCTION$

DECLARE v_id UUID;

BEGIN
    IF (v_name = 'system') THEN
        v_id := 'AAA00000-0000-0000-0000-123456789AAA';
    ELSIF(v_name = 'everyone') THEN
        v_id := 'EEE00000-0000-0000-0000-123456789EEE';

    -- bottom is an object which all the objects in the system are its parents
    -- useful to denote we want all objects when checking for permissions
    ELSIF(v_name = 'bottom') THEN
        v_id := 'BBB00000-0000-0000-0000-123456789BBB';
    END IF;

    RETURN v_id;
END;$FUNCTION$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.fnSplitterInteger(ids TEXT)
RETURNS SETOF INTEGER IMMUTABLE AS
$FUNCTION$
BEGIN
    RETURN QUERY
        SELECT CAST(regexp_split_to_table(ids, ',') AS INTEGER);
END; $FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION PUBLIC.fnSplitter (ids TEXT)
RETURNS SETOF idTextType IMMUTABLE
AS $FUNCTION$

BEGIN
    RETURN QUERY

    SELECT regexp_split_to_table(ids, ',') AS id;
END;$FUNCTION$

LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fnSplitterUuid (ids TEXT)
RETURNS SETOF UUID IMMUTABLE AS $FUNCTION$

BEGIN
    IF ids != '' THEN
        RETURN QUERY
        SELECT CAST(regexp_split_to_table(ids, ',') AS UUID);
    END IF;

END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION PUBLIC.fnSplitterWithSeperator (
    ids TEXT,
    separator VARCHAR(10)
    )
RETURNS SETOF idTextType IMMUTABLE
AS $FUNCTION$

BEGIN
    RETURN QUERY

    SELECT regexp_split_to_table(ids, separator) AS id;
END;$FUNCTION$
LANGUAGE plpgsql;

--All permissions of current user (include groups)
DROP TYPE IF EXISTS user_permissions CASCADE;
CREATE TYPE user_permissions AS (
    permission_id uuid,
    role_id uuid,
    user_id uuid
);

CREATE OR REPLACE FUNCTION PUBLIC.fn_user_permissions (v_userId IN uuid)
RETURNS SETOF user_permissions STABLE AS $FUNCTION$

DECLARE

BEGIN
    RETURN QUERY

    SELECT permissions.id AS permission_id,
        permissions.role_id,
        permissions.ad_element_id AS user_id
    FROM permissions
    INNER JOIN users
        ON permissions.ad_element_id = users.user_id
    WHERE users.user_id = v_userId

    UNION

    SELECT
        permissions.id AS permission_id,
        permissions.role_id,
        TEMP.user_id AS user_id
    FROM permissions
    INNER JOIN (
        -- get all groups of admin users
        SELECT
            ad_groups.id group_id,
            users.user_id
        FROM
            ad_groups,
            engine_sessions
        WHERE ad_groups.id IN (
            SELECT *
            FROM fnsplitteruuid(engine_sessions.group_ids)
            )
            AND users.user_id = v_userId
        ) TEMP
        ON permissions.ad_element_id = TEMP.group_id;
END;$FUNCTION$

LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION PUBLIC.fn_get_entity_parents (
    v_entity_id IN uuid,
    v_object_type IN int4
    )
RETURNS SETOF idUuidType STABLE AS $FUNCTION$

/*Gets a list of all parent GUID to the system root (including)

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
                Cluster = 9,
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
        VNICProfile = 27,
        MacPool = 28
        DiskProfile = 29
        CpuProfile = 30
*/
DECLARE
    v_entity_type int4 := v_object_type;
    system_root_id uuid;
    v_cluster_id uuid;
    ds_id uuid;
    v_image_id uuid;
    v_storage_id uuid;
    v_vm_id uuid;
    v_storage_pool_id uuid;
    v_profile_network_id uuid;
    v_disk_profile_storage_id uuid;
    v_cpu_profile_cluster_id uuid;

BEGIN
    system_root_id := (
        SELECT getGlobalIds('system')
    );-- hardcoded also in MLA Handler
    CASE
        WHEN v_entity_type = 0
            THEN -- Bottom
                RETURN QUERY SELECT object_id FROM permissions;
        WHEN v_entity_type = 2
            THEN -- VM
                -- get cluster id
                v_cluster_id := (
                    SELECT cluster_id
                    FROM vm_static
                    WHERE vm_guid = v_entity_id
                    );
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_cluster_id
                    );RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 3
            THEN -- VDS
                -- get cluster id
                v_cluster_id := (
                    SELECT cluster_id
                    FROM vds_static
                    WHERE vds_id = v_entity_id
                    );
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_cluster_id
                    );RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 4
            THEN -- Template
                -- get image id first
                v_image_id := (
                    SELECT image_guid
                    FROM images i
                    INNER JOIN vm_device vd
                        ON i.image_group_id = vd.device_id
                    WHERE vm_id = v_entity_id limit 1
                    );
                -- get the storage id from images
                v_storage_id := (
                    SELECT storage_domain_id
                    FROM image_storage_domain_map
                    WHERE image_id = v_image_id limit 1
                    );
                -- finally get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM storage_pool_iso_map
                    WHERE storage_id = v_storage_id
                    );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 5
            THEN -- VM Pool
                -- get cluster id
                v_cluster_id := (
                    SELECT cluster_id
                    FROM vm_pools
                    WHERE vm_pool_id = v_entity_id
                    );
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_cluster_id
                    );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 9
            THEN -- Cluster
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_entity_id
                    );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 11
            THEN -- Storage Domain
                RETURN QUERY SELECT system_root_id AS id

            UNION ALL

                SELECT storage_pool_id AS id
                FROM storage_pool_iso_map
                WHERE storage_id = v_entity_id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 17
            THEN -- Quota
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM quota
                    WHERE id = v_entity_id
                    );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 18
            THEN -- GlusterVolume
                -- get cluster id
                v_cluster_id := (
                    SELECT v.cluster_id
                    FROM gluster_volumes v
                    WHERE id = v_entity_id
                    );
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_cluster_id
                    );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 19
            THEN -- Disk
                -- get data center, storage domain and vm
                SELECT INTO ds_id,
                v_storage_id,
                v_vm_id storage_pool_id,
                storage_id,
                vm_id
                FROM images_storage_domain_view
                LEFT JOIN vm_device
                ON vm_device.device_id = images_storage_domain_view.disk_id
                WHERE image_group_id = v_entity_id;
                -- get cluster
                v_cluster_id := (
                    SELECT cluster_id
                    FROM vm_static
                    WHERE vm_guid = v_vm_id
                    );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_storage_id AS id

            UNION

                SELECT v_vm_id AS id

            UNION

                SELECT v_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 20
            THEN -- Network
                SELECT INTO v_storage_pool_id network.storage_pool_id
                FROM network
                WHERE network.id = v_entity_id;
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT v_storage_pool_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 27
            THEN -- VNICProfile
                SELECT INTO v_profile_network_id vnic_profiles.network_id
                FROM vnic_profiles
                WHERE vnic_profiles.id = v_entity_id;

                SELECT INTO v_storage_pool_id network.storage_pool_id
                FROM network
                WHERE network.id = v_profile_network_id;
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT v_storage_pool_id AS id

            UNION

                SELECT v_profile_network_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 29
            THEN -- DiskProfile
                SELECT INTO v_disk_profile_storage_id disk_profiles.storage_domain_id
                FROM disk_profiles
                WHERE disk_profiles.id = v_entity_id;

                SELECT INTO v_storage_pool_id storage_pool_iso_map.storage_pool_id
                FROM storage_pool_iso_map
                WHERE storage_pool_iso_map.storage_id = v_disk_profile_storage_id;
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT v_storage_pool_id AS id

            UNION

                SELECT v_disk_profile_storage_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 30
            THEN -- CpuProfile
                SELECT INTO v_cpu_profile_cluster_id cpu_profiles.cluster_id
                FROM cpu_profiles
                WHERE cpu_profiles.id = v_entity_id;

                SELECT INTO v_storage_pool_id cluster.storage_pool_id
                FROM cluster
                WHERE cluster.cluster_id = v_cpu_profile_cluster_id;
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT v_storage_pool_id AS id

            UNION

                SELECT v_cpu_profile_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 23
            THEN -- Gluster Hook
                -- get cluster id
                v_cluster_id := (
                    SELECT cluster_id
                    FROM gluster_hooks
                    WHERE id = v_entity_id
                );
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_cluster_id
                );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        WHEN v_entity_type = 25
            THEN -- Gluster Service
                -- get cluster id
                v_cluster_id := (
                    SELECT cluster_id
                    FROM vds_static
                    WHERE vds_id = v_entity_id
                );
                -- get data center id
                ds_id := (
                    SELECT storage_pool_id
                    FROM cluster
                    WHERE cluster_id = v_cluster_id
                );
                RETURN QUERY SELECT system_root_id AS id

            UNION

                SELECT ds_id AS id

            UNION

                SELECT v_cluster_id AS id

            UNION

                SELECT v_entity_id AS id;
        ELSE IF
            v_entity_type IN (
                    1,
                    14,
                    15,
                    16,
                    28
                    ) THEN -- Data Center, users, roles and mac pools are under system
            RETURN QUERY SELECT system_root_id AS id

        UNION

            SELECT v_entity_id AS id;
        END IF;

    END CASE ;
END;$FUNCTION$
LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION PUBLIC.fn_authz_entry_info(v_ad_element_id IN uuid)
RETURNS AuthzEntryInfoType STABLE
AS $FUNCTION$
DECLARE
    result authzEntryInfoType;
BEGIN
    IF (v_ad_element_id = getGlobalIds('everyone')) THEN
        SELECT 'Everyone',
            '*',
            ''
        INTO result;
    ELSE
        SELECT (COALESCE(name, '') || ' ' || COALESCE(surname, '') || ' (' || COALESCE(username, '') || ')'),
            namespace,
            domain
        INTO result
        FROM users
        WHERE user_id = v_ad_element_id;

        IF (result IS NULL) THEN
            SELECT name,
                   namespace,
                   domain
            INTO result
            FROM ad_groups
            WHERE ID = v_ad_element_id;
        END IF ;
    END IF;
    RETURN result;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION PUBLIC.fn_get_ad_element_name(v_ad_element_id IN uuid)
RETURNS TEXT STABLE
AS $FUNCTION$
DECLARE
    result TEXT;
BEGIN
    IF (v_ad_element_id = getGlobalIds('everyone')) THEN
        result := 'Everyone';
    ELSE
        SELECT (COALESCE(name, '') || ' ' || COALESCE(surname, '') || ' (' || COALESCE(username, '') || '@' || COALESCE(domain, '') || ')')
        INTO result
        FROM users
        WHERE user_id = v_ad_element_id;

        IF (result IS NULL) THEN
            SELECT name
            INTO result
            FROM ad_groups
            WHERE ID = v_ad_element_id;
        END IF ;
   END IF;
   RETURN result;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION PUBLIC.fn_get_entity_name(v_entity_id IN uuid,
    v_object_type IN int4
    )
RETURNS TEXT STABLE
AS $FUNCTION$
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
        Cluster = 9,
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
        VNICProfile = 27,
        MacPool = 28,
        DiskProfile = 29
        CpuProfile = 30
*/
    DECLARE v_entity_type int4 := v_object_type;result TEXT;BEGIN
    CASE
        WHEN v_entity_type = 1
            THEN result := 'System';
        WHEN v_entity_type = 2
        OR v_entity_type = 4
            THEN result := (
                            SELECT vm_name
                            FROM vm_static
                            WHERE vm_guid = v_entity_id
                           );
        WHEN v_entity_type = 3
            THEN result := (
                            SELECT vds_name
                            FROM vds_static
                            WHERE vds_id = v_entity_id
                           );
        WHEN v_entity_type = 5
            THEN result := (
                            SELECT vm_pool_name
                            FROM vm_pools
                            WHERE vm_pool_id = v_entity_id
                           );
        WHEN v_entity_type = 7
            THEN result := (
                            SELECT tag_name
                            FROM tags
                             WHERE tag_id = v_entity_id
                            );
        WHEN v_entity_type = 8
            THEN result := (
                            SELECT bookmark_name
                            FROM bookmarks
                            WHERE bookmark_id = v_entity_id
                           );
        WHEN v_entity_type = 9
            THEN result := (
                            SELECT name
                            FROM cluster
                            WHERE cluster_id = v_entity_id
                           );
        WHEN v_entity_type = 11
             THEN result := (
                             SELECT storage_name
                             FROM storage_domain_static
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 14
             THEN result := (
                             SELECT name
                             FROM storage_pool
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 15
             THEN result := (
                             SELECT username
                             FROM users
                             WHERE user_id = v_entity_id
                            );
        WHEN v_entity_type = 16
             THEN result := (
                             SELECT name
                             FROM roles
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 17
             THEN result := (
                             SELECT quota_name
                             FROM quota
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 18
             THEN result := (
                             SELECT vol_name
                             FROM gluster_volumes
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 19
             THEN result := (
                             SELECT disk_alias
                             FROM base_disks
                             WHERE disk_id = v_entity_id
                            );
        WHEN v_entity_type = 20
             THEN result := (
                             SELECT name
                             FROM network
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 23
             THEN result := (
                             SELECT CONCAT (
                                     gluster_command,
                                     '-',
                                     stage,
                                     '-',
                                     name
                                     )
                             FROM gluster_hooks
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 25
             THEN result := (
                             SELECT service_name
                             FROM gluster_services
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 27
             THEN result := (
                             SELECT name
                             FROM vnic_profiles
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 28
             THEN result := (
                             SELECT name
                             FROM mac_pools
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 29
             THEN result := (
                             SELECT name
                             FROM disk_profiles
                             WHERE id = v_entity_id
                            );
        WHEN v_entity_type = 30
             THEN result := (
                             SELECT name
                             FROM cpu_profiles
                             WHERE id = v_entity_id
                            );
        ELSE result := 'Unknown type ' || v_entity_type;
        END CASE ;
        -- This should be written to an error var or include object_id that is missing
        --    IF result IS NULL THEN
        --        result := v_entity_id || '' NOT FOUND'';
        --    END IF;
        RETURN result;
END;$FUNCTION$
LANGUAGE 'plpgsql';

-- get user and his groups IDs
CREATE OR REPLACE FUNCTION getUserAndGroupsById(v_id UUID)
RETURNS SETOF idUuidType STABLE
AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT ad_groups.ID
    FROM ad_groups,
         engine_sessions
    WHERE engine_sessions.user_id = v_id
        AND ad_groups.id IN (
            SELECT *
            FROM fnsplitteruuid(engine_sessions.group_ids)
        )

    UNION
    SELECT v_id

    UNION
    -- user is also member of 'Everyone'
    SELECT 'EEE00000-0000-0000-0000-123456789EEE';
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getElementIdsByIdAndGroups(
    v_id UUID,
    v_group_ids TEXT
    )
RETURNS SETOF idUuidType IMMUTABLE
AS $FUNCTION$
BEGIN
    RETURN QUERY
        SELECT *
        FROM fnsplitteruuid(v_group_ids)

        UNION

        SELECT v_id

        UNION

        -- user is also member of 'Everyone'
        SELECT 'EEE00000-0000-0000-0000-123456789EEE';
END;$FUNCTION$
LANGUAGE plpgsql;

-----------------------
-- Quota Functions ----
-----------------------
DROP TYPE IF
EXISTS cluster_usage_rs CASCADE;
CREATE TYPE cluster_usage_rs AS (
                                   virtual_cpu_usage INT,
                                   mem_size_mb_usage BIGINT
                                  );
-- returns a set of integers representing vm statuses on which the vm shouldn't
-- be used for quota calculation
CREATE OR REPLACE FUNCTION getNonCountableQutoaVmStatuses()
RETURNS SETOF INT IMMUTABLE
AS $BODY$
BEGIN
RETURN query
    SELECT 0

    UNION ALL

    SELECT 13

    UNION ALL

    SELECT 14

    UNION ALL

    SELECT 15;
    --(Down(0), Suspended(13), ImageIllegal(14), ImageLocked(15))
END;$BODY$
LANGUAGE plpgsql;

-- Summarize the VCPU usage and the RAM usage for all the VMs in the quota which are not down or suspended
-- If vds group id is null, then returns the global usage of the quota, other wise returns only the summarize of all VMs in the specific cluster.
-- NOTE: VmDynamic status (0/13/14/15) must be persistent with UpdateVmCommand
CREATE OR REPLACE FUNCTION CalculateClusterUsage(
    v_quota_id UUID,
    v_cluster_id UUID
    )
RETURNS SETOF cluster_usage_rs STABLE
AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT cast(COALESCE(sum(num_of_sockets * cpu_per_socket), 0) AS INT) AS virtual_cpu_usage,
           COALESCE(sum(mem_size_mb), 0) AS mem_size_mb_usage
    FROM vm_static,
         vm_dynamic
    WHERE quota_id = v_quota_id
        AND vm_dynamic.vm_guid = vm_static.vm_guid
        AND vm_dynamic.status NOT IN (
            SELECT getNonCountableQutoaVmStatuses()
        )
        AND (
            v_cluster_id = vm_static.cluster_id
            OR v_cluster_id IS NULL
        );
END;$FUNCTION$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS all_cluster_usage_rs CASCADE;
CREATE TYPE all_cluster_usage_rs AS (
    quota_cluster_id uuid,
    quota_id uuid,
    cluster_id uuid,
    cluster_name VARCHAR(40),
    virtual_cpu INT,
    virtual_cpu_usage INT,
    mem_size_mb BIGINT,
    mem_size_mb_usage BIGINT
    );
-- Summarize the VCPU usage and the RAM usage for all the VMs in the quota which are not down or suspended
-- If vds group id is null, then returns the global usage of the quota, otherwise returns only the sum of all VMs in the specific cluster.
-- NOTE: VmDynamic status (0/13/14/15) must be persistent with UpdateVmCommand
CREATE OR REPLACE FUNCTION calculateAllClusterUsage()
RETURNS SETOF all_cluster_usage_rs STABLE
AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT
        quota_limitation.id AS quota_cluster_id,
        quota_limitation.quota_id AS quota_id,
        quota_limitation.cluster_id AS cluster_id,
        cluster.name AS cluster_name,
        quota_limitation.virtual_cpu,
        cast(COALESCE(sum(num_of_sockets * cpu_per_socket * cast(vm_dynamic.status NOT IN (
                            SELECT getNonCountableQutoaVmStatuses()
                            ) AS INT)), 0) AS INT) AS virtual_cpu_usage,
        quota_limitation.mem_size_mb,
        COALESCE(sum(vm_static.mem_size_mb), 0) AS mem_size_mb_usage
    FROM quota_limitation
    LEFT JOIN vm_static
        ON vm_static.quota_id = quota_limitation.quota_id
    LEFT JOIN vm_dynamic
        ON vm_dynamic.vm_guid = vm_static.vm_guid
    LEFT JOIN cluster
        ON cluster.cluster_id = vm_static.cluster_id
    WHERE quota_limitation.virtual_cpu IS NOT NULL
        AND quota_limitation.mem_size_mb IS NOT NULL
    GROUP BY quota_limitation.quota_id,
        quota_limitation.cluster_id,
        cluster_name,
        quota_limitation.virtual_cpu,
        quota_limitation.mem_size_mb,
        vm_static.quota_id,
        cluster.cluster_id,
        vm_static.cluster_id,
        quota_limitation.id;
END;$FUNCTION$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS all_storage_usage_rs CASCADE;
CREATE TYPE all_storage_usage_rs AS (
    quota_storage_id uuid,
    quota_id uuid,
    storage_id uuid,
    storage_name VARCHAR(250),
    storage_size_gb BIGINT,
    storage_size_gb_usage FLOAT
    );

CREATE OR REPLACE FUNCTION calculateAllStorageUsage()
RETURNS SETOF all_storage_usage_rs STABLE
AS $FUNCTION$
BEGIN
-- Summarize size of all disks that are active.
    RETURN QUERY

    SELECT
        quota_limitation.id AS quota_storage_id,
        quota_limitation.quota_id AS quota_id,
        quota_limitation.storage_id AS storage_id,
        storage_domain_static.storage_name,
        quota_limitation.storage_size_gb,
        cast(COALESCE(sum(size * cast(active AS INT) + disk_image_dynamic.actual_size * cast((NOT active) AS INT)) / 1073741824, 0) AS FLOAT) AS storage_usage
        -- 1073741824 is 1024^3 (for GB)
    FROM quota_limitation
    LEFT JOIN image_storage_domain_map
        ON quota_limitation.quota_id = image_storage_domain_map.quota_id
    LEFT JOIN images
        ON images.image_guid = image_storage_domain_map.image_id
    LEFT JOIN disk_image_dynamic
        ON images.image_guid = disk_image_dynamic.image_id
    LEFT JOIN storage_domain_static
        ON image_storage_domain_map.storage_domain_id = storage_domain_static.id
    WHERE quota_limitation.storage_size_gb IS NOT NULL
    GROUP BY
        quota_limitation.quota_id,
        storage_id,
        quota_limitation.id,
        storage_domain_static.storage_name,
        quota_limitation.storage_size_gb;
END;$FUNCTION$
LANGUAGE plpgsql;

-- Summarize the storage usage for all the disks in the quota
-- We calculate the actual size for the read only disks such as snapshots and template disks,
-- and also the virtual size of the active disks, since their potential size is their virtual size.
-- If v_storage_id is null, then return only the global usage of the quota, other wise return only the summarize in the specific storage.
CREATE OR REPLACE FUNCTION CalculateStorageUsage(v_quota_id UUID, v_storage_id UUID)
RETURNS double precision STABLE
AS $FUNCTION$
DECLARE
  v_retVal double precision;
BEGIN
  SELECT COALESCE(sum(CASE active WHEN TRUE THEN size ELSE actual_size END) / (1024 * 1024 * 1024),0)
  INTO v_retVal
  FROM images_storage_domain_view
  WHERE quota_id = v_quota_id
    AND (v_storage_id IS NULL
        OR v_storage_id = storage_id);
  RETURN v_retVal;
END; $FUNCTION$
LANGUAGE plpgsql;

-- This function turns a string of IP addresses to an array of IP
-- addreses, in order to correct sorting.
CREATE OR REPLACE FUNCTION fn_get_comparable_ip_list(TEXT)
RETURNS inet [] IMMUTABLE STRICT
AS $PROCEDURE$
BEGIN
    CASE
        WHEN ($1 IS NULL)
        OR ($1 ~ E'^\\s*$')
            THEN RETURN NULL;
        ELSE
            RETURN regexp_split_to_array(trim(both FROM $1), E'\\s+')::inet [];
    END CASE ;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Return csv list of dedicated hosts guids
CREATE OR REPLACE FUNCTION fn_get_dedicated_hosts_ids_by_vm_id(v_vm_id UUID)
RETURNS TEXT STABLE
AS $FUNCTION$
BEGIN
    RETURN array_to_string(array_agg(vds_id), ',')
    FROM vm_host_pinning_map
    WHERE vm_id = v_vm_id;
END;$FUNCTION$
LANGUAGE plpgsql;

-- Computes number of vcpus for vm_static
CREATE OR REPLACE FUNCTION fn_get_num_of_vcpus(vm_static)
RETURNS INT IMMUTABLE
AS $PROCEDURE$

BEGIN
    RETURN $1. num_of_sockets * $1. cpu_per_socket * $1. threads_per_cpu;
END;$PROCEDURE$

LANGUAGE plpgsql;

