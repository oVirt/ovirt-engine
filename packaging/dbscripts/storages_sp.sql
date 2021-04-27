

----------------------------------------------------------------
-- [storage_pool] Table
--
CREATE OR REPLACE FUNCTION Insertstorage_pool (
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_id UUID,
    v_name VARCHAR(40),
    v_status INT,
    v_is_local BOOLEAN,
    v_master_domain_version INT,
    v_spm_vds_id UUID,
    v_compatibility_version VARCHAR(40),
    v_quota_enforcement_type INT,
    v_managed BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_pool (
        description,
        free_text_comment,
        id,
        name,
        status,
        is_local,
        master_domain_version,
        spm_vds_id,
        compatibility_version,
        quota_enforcement_type,
        managed
        )
    VALUES (
        v_description,
        v_free_text_comment,
        v_id,
        v_name,
        v_status,
        v_is_local,
        v_master_domain_version,
        v_spm_vds_id,
        v_compatibility_version,
        v_quota_enforcement_type,
        v_managed
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatestorage_pool (
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_id UUID,
    v_name VARCHAR(40),
    v_status INT,
    v_is_local BOOLEAN,
    v_storage_pool_format_type VARCHAR(50),
    v_master_domain_version INT,
    v_spm_vds_id UUID,
    v_compatibility_version VARCHAR(40),
    v_quota_enforcement_type INT,
    v_managed BOOLEAN
    )
RETURNS VOID
    --The [storage_pool] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE storage_pool
    SET description = v_description,
        free_text_comment = v_free_text_comment,
        name = v_name,
        is_local = v_is_local,
        status = v_status,
        storage_pool_format_type = v_storage_pool_format_type,
        master_domain_version = v_master_domain_version,
        spm_vds_id = v_spm_vds_id,
        compatibility_version = v_compatibility_version,
        _update_date = LOCALTIMESTAMP,
        quota_enforcement_type = v_quota_enforcement_type,
        managed = v_managed
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatestorage_pool_partial (
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_id UUID,
    v_name VARCHAR(40),
    v_is_local BOOLEAN,
    v_storage_pool_format_type VARCHAR(50),
    v_compatibility_version VARCHAR(40),
    v_quota_enforcement_type INT
    )
RETURNS VOID
    --The [storage_pool] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE storage_pool
    SET description = v_description,
        free_text_comment = v_free_text_comment,
        name = v_name,
        is_local = v_is_local,
        storage_pool_format_type = v_storage_pool_format_type,
        compatibility_version = v_compatibility_version,
        _update_date = LOCALTIMESTAMP,
        quota_enforcement_type = v_quota_enforcement_type
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatestorage_pool_status (
    v_id UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_pool
    SET status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION IncreaseStoragePoolMasterVersion (v_id UUID)
RETURNS INT AS $PROCEDURE$
DECLARE v_master_domain_version INT;

BEGIN
    UPDATE storage_pool
    SET master_domain_version = master_domain_version + 1
    WHERE id = v_id RETURNING master_domain_version
    INTO v_master_domain_version;

    RETURN v_master_domain_version;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletestorage_pool (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    SELECT vm_guid
    INTO v_val
    FROM vm_static
    WHERE vm_guid IN (
            SELECT vm_guid
            FROM vms
            WHERE storage_pool_id = v_id
            )
    FOR

    UPDATE;

    DELETE
    FROM snapshots
    WHERE vm_id IN (
            SELECT vm_guid
            FROM vms
            WHERE storage_pool_id = v_id
            );

    DELETE
    FROM vm_static
    WHERE vm_guid IN (
            SELECT vm_guid
            FROM vms
            WHERE storage_pool_id = v_id
            );

    -- Delete vm pools as empty pools are not supported
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    SELECT vm_pool_id
    INTO v_val
    FROM vm_pools
    WHERE vm_pool_id IN (
            SELECT vm_pool_id
            FROM vm_pools_view
            WHERE storage_pool_id = v_id
            )
    FOR

    UPDATE;

    DELETE
    FROM vm_pools
    WHERE vm_pool_id IN (
            SELECT vm_pool_id
            FROM vm_pools_view
            WHERE storage_pool_id = v_id
            );

    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM storage_pool
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM storage_pool
    WHERE id = v_id;

    -- delete StoragePool permissions --
    PERFORM DeletePermissionsByEntityId(v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromstorage_pool (
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool
    WHERE (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_storage_pool_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllByStatus (v_status INT)
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool
    WHERE status = v_status;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_poolByid (
    v_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool
    WHERE id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_storage_pool_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_poolByName (
    v_name VARCHAR(40),
    v_is_case_sensitive BOOLEAN
    )
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool
    WHERE name = v_name
        OR (
            NOT v_is_case_sensitive
            AND name ilike v_name
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_poolsByStorageDomainId (v_storage_domain_id UUID)
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT storage_pool.*
    FROM storage_pool
    INNER JOIN storage_pool_iso_map
        ON storage_pool.id = storage_pool_iso_map.storage_pool_id
    WHERE storage_pool_iso_map.storage_id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmAndTemplatesIdsByStorageDomainId (
    v_storage_domain_id UUID,
    v_include_shareable BOOLEAN,
    v_active_only BOOLEAN
    )
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT vd.vm_id
    FROM vm_device vd
    INNER JOIN images_storage_domain_view i
        ON i.image_group_id = vd.device_id
    WHERE i.storage_id = v_storage_domain_id
        AND i.active = v_active_only
        AND i.shareable = v_include_shareable;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_poolsByVdsId (v_vdsId UUID)
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
DECLARE v_clusterId UUID;

BEGIN
    SELECT cluster_id
    INTO v_clusterId
    FROM Vds_static
    WHERE vds_id = v_vdsId;

    RETURN QUERY

    SELECT *
    FROM storage_pool
    WHERE storage_pool.id IN (
            SELECT storage_pool_id
            FROM cluster
            WHERE cluster_id = v_clusterId
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_poolsByClusterId (v_clusterId UUID)
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool
    WHERE storage_pool.id IN (
            SELECT storage_pool_id
            FROM cluster
            WHERE cluster_id = v_clusterId
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [storage_domains_ovf_info] Table
--
CREATE OR REPLACE FUNCTION LoadStorageDomainInfoByDomainId (v_storage_domain_id UUID)
RETURNS SETOF storage_domains_ovf_info STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains_ovf_info ovf
    WHERE ovf.storage_domain_id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LoadStorageDomainInfoByDiskId (v_disk_id UUID)
RETURNS SETOF storage_domains_ovf_info STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains_ovf_info ovf
    WHERE ovf.ovf_disk_id = v_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertStorageDomainOvfInfo (
    v_storage_domain_id UUID,
    v_status INT,
    v_ovf_disk_id UUID,
    v_stored_ovfs_ids TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_domains_ovf_info (
        storage_domain_id,
        status,
        ovf_disk_id,
        stored_ovfs_ids
        )
    VALUES (
        v_storage_domain_id,
        v_status,
        v_ovf_disk_id,
        v_stored_ovfs_ids
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LoadStorageDomainsForOvfIds (v_ovfs_ids TEXT)
RETURNS SETOF UUID AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT ovf.storage_domain_id
    FROM storage_domains_ovf_info ovf
    WHERE string_to_array(ovf.stored_ovfs_ids, ',') && string_to_array(v_ovfs_ids, ',');
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStorageDomainOvfInfo (
    v_storage_domain_id UUID,
    v_status INT,
    v_ovf_disk_id UUID,
    v_stored_ovfs_ids TEXT,
    v_last_updated TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_domains_ovf_info
    SET status = v_status,
        storage_domain_id = v_storage_domain_id,
        ovf_disk_id = v_ovf_disk_id,
        stored_ovfs_ids = v_stored_ovfs_ids,
        last_updated = v_last_updated
    WHERE ovf_disk_id = v_ovf_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteStorageDomainOvfInfo (v_ovf_disk_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM storage_domains_ovf_info
    WHERE ovf_disk_id = v_ovf_disk_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateOvfUpdatedInfo (
    v_storage_domains_ids VARCHAR(5000),
    v_status INT,
    v_except_status INT
    )
RETURNS VOID AS $PROCEDURE$
DECLARE curs_storages_ids CURSOR
FOR
SELECT *
FROM fnSplitterUuid(v_storage_domains_ids);

id UUID;

BEGIN
    OPEN curs_storages_ids;

    LOOP

        FETCH curs_storages_ids
        INTO id;

        IF NOT FOUND THEN
            EXIT;
        END IF;
        UPDATE storage_domains_ovf_info
        SET status = v_status
        WHERE storage_domain_id = id
            AND status != v_except_status;
    END LOOP;

    CLOSE curs_storages_ids;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- ----------------------------------------------------------------
-- [storage_domain_static] Table
--
--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.
CREATE OR REPLACE FUNCTION Insertstorage_domain_static (
    v_id UUID,
    v_storage VARCHAR(250),
    v_storage_name VARCHAR(250),
    v_storage_description VARCHAR(4000),
    v_storage_comment TEXT,
    v_storage_type INT,
    v_storage_domain_type INT,
    v_storage_domain_format_type VARCHAR(50),
    v_last_time_used_as_master BIGINT,
    v_wipe_after_delete BOOLEAN,
    v_discard_after_delete BOOLEAN,
    v_first_metadata_device VARCHAR(100),
    v_vg_metadata_device VARCHAR(100),
    v_warning_low_space_indicator INT,
    v_critical_space_action_blocker INT,
    v_warning_low_confirmed_space_indicator INT,
    v_backup BOOLEAN,
    v_block_size INTEGER
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_domain_static (
        id,
        storage,
        storage_name,
        storage_description,
        storage_comment,
        storage_type,
        storage_domain_type,
        storage_domain_format_type,
        last_time_used_as_master,
        wipe_after_delete,
        discard_after_delete,
        first_metadata_device,
        vg_metadata_device,
        warning_low_space_indicator,
        critical_space_action_blocker,
        warning_low_confirmed_space_indicator,
        backup,
        block_size
        )
    VALUES (
        v_id,
        v_storage,
        v_storage_name,
        v_storage_description,
        v_storage_comment,
        v_storage_type,
        v_storage_domain_type,
        v_storage_domain_format_type,
        v_last_time_used_as_master,
        v_wipe_after_delete,
        v_discard_after_delete,
        v_first_metadata_device,
        v_vg_metadata_device,
        v_warning_low_space_indicator,
        v_critical_space_action_blocker,
        v_warning_low_confirmed_space_indicator,
        v_backup,
        v_block_size
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_List_By_ImageId (v_image_id UUID)
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE id IN (
            SELECT storage_domain_id
            FROM image_storage_domain_map
            WHERE image_id = v_image_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatestorage_domain_static (
    v_id UUID,
    v_storage VARCHAR(250),
    v_storage_name VARCHAR(250),
    v_storage_description VARCHAR(4000),
    v_storage_comment TEXT,
    v_storage_type INT,
    v_storage_domain_type INT,
    v_storage_domain_format_type INT,
    v_last_time_used_as_master BIGINT,
    v_wipe_after_delete BOOLEAN,
    v_discard_after_delete BOOLEAN,
    v_first_metadata_device VARCHAR(100),
    v_vg_metadata_device VARCHAR(100),
    v_warning_low_space_indicator INT,
    v_critical_space_action_blocker INT,
    v_warning_low_confirmed_space_indicator INT,
    v_backup BOOLEAN,
    v_block_size INTEGER
    )
RETURNS VOID
    --The [storage_domain_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE storage_domain_static
    SET storage = v_storage,
        storage_name = v_storage_name,
        storage_type = v_storage_type,
        storage_domain_type = v_storage_domain_type,
        _update_date = LOCALTIMESTAMP,
        storage_domain_format_type = v_storage_domain_format_type,
        last_time_used_as_master = v_last_time_used_as_master,
        wipe_after_delete = v_wipe_after_delete,
        discard_after_delete = v_discard_after_delete,
        first_metadata_device=v_first_metadata_device,
        vg_metadata_device=v_vg_metadata_device,
        storage_description = v_storage_description,
        storage_comment = v_storage_comment,
        warning_low_space_indicator = v_warning_low_space_indicator,
        critical_space_action_blocker = v_critical_space_action_blocker,
        warning_low_confirmed_space_indicator = v_warning_low_confirmed_space_indicator,
        backup = v_backup,
        block_size = v_block_size
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletestorage_domain_static (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM storage_domain_static
    WHERE id = v_id
    FOR UPDATE;

    DELETE
    FROM storage_domain_static
    WHERE id = v_id;

    -- delete Storage permissions --
    PERFORM DeletePermissionsByEntityId(v_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromstorage_domain_static ()
RETURNS SETOF storage_domain_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domain_static;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domain_staticByid (v_id UUID)
RETURNS SETOF storage_domain_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domain_static
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domain_staticByName (v_name VARCHAR(250))
RETURNS SETOF storage_domain_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domain_static
    WHERE storage_name = v_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domain_staticByNameFiltered (
    v_name VARCHAR(250),
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF storage_domain_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domain_static sds
    WHERE storage_name = v_name
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_storage_domain_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = sds.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domain_staticBystorage_pool_id (v_storage_pool_id UUID)
RETURNS SETOF storage_domain_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT storage_domain_static.*
    FROM storage_domain_static
    INNER JOIN storage_pool_iso_map
        ON storage_pool_iso_map.storage_id = storage_domain_static.id
    WHERE storage_pool_iso_map.storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS GetStorageDomainIdsByStoragePoolIdAndStatus_rs CASCADE;
CREATE TYPE GetStorageDomainIdsByStoragePoolIdAndStatus_rs AS (storage_id UUID);

CREATE OR REPLACE FUNCTION GetStorageDomainIdsByStoragePoolIdAndStatus (
    v_storage_pool_id UUID,
    v_status INT
    )
RETURNS SETOF GetStorageDomainIdsByStoragePoolIdAndStatus_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT storage_id
    FROM storage_pool_iso_map
    INNER JOIN storage_domain_static
        ON storage_pool_iso_map.storage_id = storage_domain_static.id
    WHERE storage_pool_id = v_storage_pool_id
        AND status = v_status
        AND storage_domain_static.storage_type != 9
        AND storage_domain_static.storage_type != 10; -- filter Cinder and Managed block storage domains
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_By_id (
    v_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF storage_domains_without_storage_pools STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains_without_storage_pools
    WHERE id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_storage_domain_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_by_storage_pool_id_with_permitted_action (
    v_user_id UUID,
    v_action_group_id INT,
    v_storage_pool_id UUID
    )
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage_pool_id = v_storage_pool_id
        AND (
            SELECT get_entity_permissions(v_user_id, v_action_group_id, id, 11)
            ) IS NOT NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_By_id_and_by_storage_pool_id (
    v_id UUID,
    v_storage_pool_id UUID
    )
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE id = v_id
        AND storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_By_storagePoolId (
    v_storage_pool_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage_pool_id = v_storage_pool_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_storage_domain_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domain_by_type_storagePoolId_and_status (
    v_storage_domain_type INT,
    v_storage_pool_id UUID,
    v_status INT
    )
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage_pool_id = v_storage_pool_id
        AND storage_domain_type = v_storage_domain_type
        AND (
            v_status IS NULL
            OR status = v_status
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_By_connection (v_connection VARCHAR)
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage IN (
            SELECT id
            FROM storage_server_connections
            WHERE connection = v_connection
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromStorageDomainsByConnectionId (v_connection_id VARCHAR)
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage = v_connection_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromstorage_domains (
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT ON (id) *
    FROM storage_domains
    WHERE (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_storage_domain_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Remove_Entities_From_storage_domain (v_storage_domain_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_ids UUID[];
BEGIN
    BEGIN
        -- Creating a temporary table which will give all the images and the disks which resids on only the specified storage domain. (copied template disks on multiple storage domains will not be part of this table)
        CREATE TEMPORARY TABLE STORAGE_DOMAIN_MAP_TABLE ON COMMIT DROP AS

        SELECT image_guid AS image_id,
            disk_id
        FROM images_storage_domain_view
        WHERE storage_id = v_storage_domain_id

        EXCEPT

        SELECT image_guid AS image_id,
            disk_id
        FROM images_storage_domain_view
        WHERE storage_id != v_storage_domain_id;

        exception when others then

        TRUNCATE TABLE STORAGE_DOMAIN_MAP_TABLE;

        INSERT INTO STORAGE_DOMAIN_MAP_TABLE
        SELECT image_guid AS image_id,
            disk_id
        FROM images_storage_domain_view
        WHERE storage_id = v_storage_domain_id

        EXCEPT

        SELECT image_guid AS image_id,
            disk_id
        FROM images_storage_domain_view
        WHERE storage_id != v_storage_domain_id;
    END;

    BEGIN
        -- All the VMs/Templates which have disks both on the specified domain and other domains.
        CREATE TEMPORARY TABLE ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE ON COMMIT DROP AS

        SELECT DISTINCT vm_static.vm_guid
        FROM vm_static
        INNER JOIN (
            -- Join vm_static only with VMs and Templates that have images on the storage domain v_storage_domain_id
            SELECT vm_static.vm_guid
            FROM vm_static
            INNER JOIN vm_device vd
                ON vd.vm_id = vm_static.vm_guid
            INNER JOIN images i
                ON i.image_group_id = vd.device_id
            INNER JOIN STORAGE_DOMAIN_MAP_TABLE
                ON i.image_guid = STORAGE_DOMAIN_MAP_TABLE.image_id
            ) vm_guids_with_disks_on_storage_domain
            ON vm_static.vm_guid = vm_guids_with_disks_on_storage_domain.vm_guid
        -- With all the VMs which have images on the storage domain, get all of their images and check if there is an image on another storage domain.
        INNER JOIN vm_device vd
            ON vd.vm_id = vm_static.vm_guid
        INNER JOIN images i
            ON i.image_group_id = vd.device_id
        INNER JOIN image_storage_domain_map
            ON i.image_guid = image_storage_domain_map.image_id
        WHERE image_storage_domain_map.storage_domain_id != v_storage_domain_id;

        exception when others then

        TRUNCATE TABLE ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE;

        INSERT INTO ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE
        SELECT DISTINCT vm_static.vm_guid
        FROM vm_static
        INNER JOIN (
            SELECT vm_static.vm_guid
            FROM vm_static
            INNER JOIN vm_device vd
                ON vd.vm_id = vm_static.vm_guid
            INNER JOIN images i
                ON i.image_group_id = vd.device_id
            INNER JOIN STORAGE_DOMAIN_MAP_TABLE
                ON i.image_guid = STORAGE_DOMAIN_MAP_TABLE.image_id
            ) vm_guids_with_disks_on_storage_domain
            ON vm_static.vm_guid = vm_guids_with_disks_on_storage_domain.vm_guid
        INNER JOIN vm_device vd
            ON vd.vm_id = vm_static.vm_guid
        INNER JOIN images i
            ON i.image_group_id = vd.device_id
        INNER JOIN image_storage_domain_map
            ON i.image_guid = image_storage_domain_map.image_id
        WHERE image_storage_domain_map.storage_domain_id != v_storage_domain_id;

        -- Add VMs with direct luns as part of entity ids with disks on other storage domains.
        INSERT INTO ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE
        SELECT DISTINCT vm_static.vm_guid
        FROM vm_static
        INNER JOIN vm_device vd
            ON vd.vm_id = vm_static.vm_guid
        INNER JOIN disk_lun_map dlm
            ON dlm.disk_id = vd.device_id;
    END;

    BEGIN
        -- Templates with any images residing on only the specified storage domain
        CREATE TEMPORARY TABLE TEMPLATES_IDS_TEMPORARY_TABLE ON COMMIT DROP AS

        SELECT vm_device.vm_id AS vm_guid
        FROM images_storage_domain_view
        INNER JOIN vm_device
            ON vm_device.device_id = images_storage_domain_view.disk_id
        INNER JOIN STORAGE_DOMAIN_MAP_TABLE
            ON STORAGE_DOMAIN_MAP_TABLE.image_id = images_storage_domain_view.image_guid
        WHERE entity_type = 'TEMPLATE'
            AND storage_id = v_storage_domain_id
            AND vm_device.vm_id NOT IN (
                SELECT vm_guid
                FROM ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE
                );

        exception when others then

        TRUNCATE TABLE TEMPLATES_IDS_TEMPORARY_TABLE;

        INSERT INTO TEMPLATES_IDS_TEMPORARY_TABLE
        SELECT vm_device.vm_id AS vm_guid
        FROM images_storage_domain_view
        INNER JOIN vm_device
            ON vm_device.device_id = images_storage_domain_view.disk_id
        INNER JOIN STORAGE_DOMAIN_MAP_TABLE
            ON STORAGE_DOMAIN_MAP_TABLE.image_id = images_storage_domain_view.image_guid
        WHERE entity_type = 'TEMPLATE'
            AND storage_id = v_storage_domain_id
            AND vm_device.vm_id NOT IN (
                SELECT vm_guid
                FROM ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE
                );
    END;

    -- Add also Template Versions based on the selected templates
    INSERT INTO TEMPLATES_IDS_TEMPORARY_TABLE
    SELECT vm_guid
    FROM vm_static
    WHERE vmt_guid IN (
            SELECT vm_guid
            FROM TEMPLATES_IDS_TEMPORARY_TABLE
            )
        AND entity_type = 'TEMPLATE';

    BEGIN
        -- Vms which resides on the storage domain
        CREATE TEMPORARY TABLE VM_IDS_TEMPORARY_TABLE ON COMMIT DROP AS

        SELECT vm_id,
            vm_images_view.entity_type AS entity_type
        FROM vm_images_view
        INNER JOIN vm_device
            ON vm_device.device_id = vm_images_view.disk_id
        WHERE v_storage_domain_id IN (
                SELECT *
                FROM fnsplitteruuid(storage_id)
                )
            AND vm_id NOT IN (
                SELECT vm_guid
                FROM ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE
                );

        exception when others then

        TRUNCATE TABLE VM_IDS_TEMPORARY_TABLE;

        INSERT INTO VM_IDS_TEMPORARY_TABLE
        SELECT vm_id,
            vm_images_view.entity_type AS entity_type
        FROM vm_images_view
        INNER JOIN vm_device
            ON vm_device.device_id = vm_images_view.disk_id
        WHERE v_storage_domain_id IN (
                SELECT *
                FROM fnsplitteruuid(storage_id)
                )
            AND vm_id NOT IN (
                SELECT vm_guid
                FROM ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE
                );
    END;

    v_ids := array_agg(vm_id::UUID) AS vm_guid FROM VM_IDS_TEMPORARY_TABLE WHERE entity_type <> 'TEMPLATE';
    PERFORM DeletePermissionsByEntityIds(v_ids);

    DELETE
    FROM snapshots
    WHERE vm_id IN (
            SELECT vm_id AS vm_guid
            FROM VM_IDS_TEMPORARY_TABLE
            );

    DELETE
    FROM image_storage_domain_map
    WHERE storage_domain_id = v_storage_domain_id;

    DELETE
    FROM images
    WHERE image_guid IN (
            SELECT image_id
            FROM STORAGE_DOMAIN_MAP_TABLE
            );

    DELETE
    FROM vm_interface
    WHERE vm_guid IN (
            SELECT vm_guid
            FROM TEMPLATES_IDS_TEMPORARY_TABLE
            );

    v_ids := array_agg(vm_guid::UUID) FROM TEMPLATES_IDS_TEMPORARY_TABLE;
    PERFORM DeletePermissionsByEntityIds(v_ids);

    DELETE
    FROM vm_static
    WHERE vm_guid IN (
            SELECT vm_id AS vm_guid
            FROM VM_IDS_TEMPORARY_TABLE
            WHERE entity_type <> 'TEMPLATE'
            );

    -- Delete devices which are related to VMs/Templates with Multiple Storage Domain (VMs/Templates which has not removed)
    DELETE
    FROM vm_device
    WHERE device_id IN (
            SELECT disk_id
            FROM STORAGE_DOMAIN_MAP_TABLE
            );

    -- Delete pools and snapshots of pools based on templates from the storage domain to be removed
    DELETE
    FROM snapshots
    WHERE vm_id IN (
            SELECT vm_guid
            FROM vm_static
            WHERE vmt_guid IN (
                    SELECT vm_guid
                    FROM TEMPLATES_IDS_TEMPORARY_TABLE
                    )
            );

    DELETE
    FROM vm_static
    WHERE vmt_guid IN (
            SELECT vm_guid
            FROM TEMPLATES_IDS_TEMPORARY_TABLE
            );

    DELETE
    FROM vm_static
    WHERE vm_guid IN (
            SELECT vm_guid
            FROM TEMPLATES_IDS_TEMPORARY_TABLE
            );

    -- Deletes the disks which the only storage domain they are reside on, is the storage domain.
    DELETE
    FROM base_disks
    WHERE disk_id IN (
            SELECT disk_id
            FROM STORAGE_DOMAIN_MAP_TABLE
            );

    -- Deletes the disks's permissions which the only storage domain they are reside on, is the storage domain.
    v_ids := array_agg(disk_id::UUID) FROM STORAGE_DOMAIN_MAP_TABLE;
    PERFORM DeletePermissionsByEntityIds(v_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Force_Delete_storage_domain (v_storage_domain_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM Remove_Entities_From_storage_domain(v_storage_domain_id);

    PERFORM DeletePermissionsByEntityId(v_storage_domain_id);

    DELETE
    FROM storage_domain_dynamic
    WHERE id = v_storage_domain_id;

    DELETE
    FROM storage_domain_static
    WHERE id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_List_By_storageDomainId (v_storage_domain_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage_domains.id = v_storage_domain_id
    AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_storage_pool_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_storage_domain_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

--This SP returns all data centers containing cluster with permissions to run the given action by user
CREATE OR REPLACE FUNCTION fn_perms_get_storage_pools_with_permitted_action_on_clusters (
    v_user_id UUID,
    v_action_group_id INT,
    v_supports_virt_service boolean,
    v_supports_gluster_service boolean
    )
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT sp.*
    FROM storage_pool sp
    WHERE sp.id IN (
            SELECT vg.storage_pool_id
            FROM cluster vg
            WHERE (
                    SELECT get_entity_permissions(v_user_id, v_action_group_id, vg.cluster_id, 9)
                    ) IS NOT NULL
                AND (
                    (
                        v_supports_virt_service = TRUE
                        AND vg.virt_service = TRUE
                        )
                    OR (
                        v_supports_gluster_service = TRUE
                        AND vg.gluster_service = TRUE
                        )
                    )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domains_By_storage_pool_id_and_connection (
    v_storage_pool_id UUID,
    v_connection VARCHAR
    )
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage_pool_id = v_storage_pool_id
        AND storage IN (
            SELECT id
            FROM storage_server_connections
            WHERE connection = v_connection
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetFailingStorage_domains ()
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE recoverable
        AND status = 4;--inactive
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetFailingVdss ()
RETURNS SETOF vds STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds
    WHERE recoverable
        AND status = 10;--non operational
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStoragePoolsByClusterService (
    v_supports_virt_service BOOLEAN,
    v_supports_gluster_service BOOLEAN
    )
RETURNS SETOF storage_pool STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT sp.*
    FROM storage_pool SP
    WHERE EXISTS (
            SELECT 1
            FROM cluster vg
            WHERE (
                    (
                        v_supports_virt_service = TRUE
                        AND vg.virt_service = TRUE
                        )
                    OR (
                        v_supports_gluster_service = TRUE
                        AND vg.gluster_service = TRUE
                        )
                    )
                AND vg.storage_pool_id = sp.id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageServerConnectionsForDomain (v_storage_domain_id UUID)
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE EXISTS (
            SELECT 1
            FROM storage_domain_static
            WHERE storage_domain_static.id = v_storage_domain_id
                AND storage_domain_static.storage_type IN (
                    1,
                    4,
                    6,
                    7
                    ) -- file storage domains - nfs,posix,local,gluster
                AND storage_server_connections.id = storage_domain_static.storage

            UNION ALL

            SELECT 1
            FROM storage_domain_static
            INNER JOIN luns
                ON storage_domain_static.storage = luns.volume_group_id
            INNER JOIN lun_storage_server_connection_map
                ON luns.lun_id = lun_storage_server_connection_map.lun_id
                    AND storage_server_connections.id = lun_storage_server_connection_map.storage_server_connection
            WHERE storage_domain_static.id = v_storage_domain_id
                AND storage_domain_static.storage_type = 3 -- storage type = iscsi
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDcIdByExternalNetworkId (v_external_id TEXT)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT storage_pool_id
    FROM network
    WHERE provider_network_external_id = v_external_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- This SP returns the number of images in the specified storage domain
CREATE OR REPLACE FUNCTION GetNumberOfImagesInStorageDomain (v_storage_domain_id UUID)
RETURNS SETOF BIGINT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT COUNT(*)
    FROM image_storage_domain_map
    WHERE storage_domain_id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostedEngineStorageDomainIds()
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT id
    FROM hosted_engine_storage_domains_ids_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageDomainByGlusterVolumeId (v_gluster_vol_id UUID)
RETURNS SETOF storage_domains STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domains
    WHERE storage IN (
            SELECT id
            FROM storage_server_connections
            WHERE gluster_volume_id = v_gluster_vol_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- ----------------------------------------------------------------
-- [cinder_storage] Table
--
--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.
CREATE OR REPLACE FUNCTION InsertCinderStorage (
    v_storage_domain_id UUID,
    v_driver_options JSONB,
    v_driver_sensitive_options TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO cinder_storage (
        storage_domain_id,
        driver_options,
        driver_sensitive_options
        )
    VALUES (
        v_storage_domain_id,
        v_driver_options,
        v_driver_sensitive_options
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCinderStorage (
    v_storage_domain_id UUID,
    v_driver_options JSONB,
    v_driver_sensitive_options TEXT
    )
RETURNS VOID
    AS $PROCEDURE$
BEGIN
    UPDATE cinder_storage
    SET driver_options = v_driver_options,
        driver_sensitive_options = v_driver_sensitive_options
    WHERE storage_domain_id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteCinderStorage (v_storage_domain_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM cinder_storage
    WHERE storage_domain_id = v_storage_domain_id
    FOR UPDATE;

    DELETE
    FROM cinder_storage
    WHERE id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCinderStorage (v_storage_domain_id UUID)
RETURNS SETOF cinder_storage STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cinder_storage
    WHERE storage_domain_id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCinderStorageByDrivers (
    v_driver_options JSONB)
RETURNS SETOF cinder_storage STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cinder_storage
    WHERE driver_options = v_driver_options;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- ----------------------------------------------------------------
-- [external_leases] Table
--
CREATE OR REPLACE FUNCTION InsertExternalLease (
    v_storage_domain_id UUID,
    v_lease_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO external_leases (
        storage_domain_id,
        lease_id
        )
    VALUES (
        v_storage_domain_id,
        v_lease_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateExternalLease (
    v_lease_id UUID,
    v_storage_domain_id UUID
    )
RETURNS VOID
    AS $PROCEDURE$
BEGIN
    UPDATE external_leases
    SET storage_domain_id = v_storage_domain_id
    WHERE lease_id = v_lease_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteExternalLease (v_lease_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    SELECT lease_id
    INTO v_val
    FROM external_leases
    WHERE lease_id = v_lease_id
    FOR UPDATE;

    DELETE
    FROM external_leases
    WHERE lease_id = v_lease_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetExternalLease (v_lease_id UUID)
RETURNS SETOF external_leases STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM external_leases
    WHERE lease_id = v_lease_id;
END;$PROCEDURE$
LANGUAGE plpgsql;
