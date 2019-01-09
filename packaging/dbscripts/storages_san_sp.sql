

----------------------------------------------------------------
-- [LUNs] Table
--
CREATE OR REPLACE FUNCTION InsertLUNs (
    v_LUN_id VARCHAR(255),
    v_physical_volume_id VARCHAR(50),
    v_volume_group_id VARCHAR(50),
    v_serial VARCHAR(4000),
    v_lun_mapping INT,
    v_vendor_id VARCHAR(50),
    v_product_id VARCHAR(50),
    v_device_size INT,
    v_discard_max_size BIGINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO LUNs (
        LUN_id,
        physical_volume_id,
        volume_group_id,
        serial,
        lun_mapping,
        vendor_id,
        product_id,
        device_size,
        discard_max_size
        )
    VALUES (
        v_LUN_id,
        v_physical_volume_id,
        v_volume_group_id,
        v_serial,
        v_lun_mapping,
        v_vendor_id,
        v_product_id,
        v_device_size,
        v_discard_max_size
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateLUNs (
    v_LUN_id VARCHAR(255),
    v_physical_volume_id VARCHAR(50),
    v_volume_group_id VARCHAR(50),
    v_serial VARCHAR(4000),
    v_lun_mapping INT,
    v_vendor_id VARCHAR(50),
    v_product_id VARCHAR(50),
    v_device_size INT,
    v_discard_max_size BIGINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE LUNs
    SET LUN_id = v_LUN_id,
        physical_volume_id = v_physical_volume_id,
        volume_group_id = v_volume_group_id,
        serial = v_serial,
        lun_mapping = v_lun_mapping,
        vendor_id = v_vendor_id,
        product_id = v_product_id,
        device_size = v_device_size,
        discard_max_size = v_discard_max_size
    WHERE LUN_id = v_LUN_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteLUN (v_LUN_id VARCHAR(255))
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM LUNs
    WHERE LUN_id = v_LUN_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromLUNs ()
RETURNS SETOF luns_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM luns_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLUNsBystorage_server_connection (v_storage_server_connection VARCHAR(50))
RETURNS SETOF luns_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT luns_view.*
    FROM luns_view
    INNER JOIN LUN_storage_server_connection_map
        ON LUN_storage_server_connection_map.LUN_id = luns_view.LUN_id
    WHERE LUN_storage_server_connection_map.storage_server_connection = v_storage_server_connection;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLUNsByVolumeGroupId (v_volume_group_id VARCHAR(50))
RETURNS SETOF luns_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM luns_view
    WHERE volume_group_id = v_volume_group_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLUNByLUNId (v_LUN_id VARCHAR(255))
RETURNS SETOF luns_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM luns_view
    WHERE LUN_id = v_LUN_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [storage_domain_dynamic] Table
--
--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.
CREATE OR REPLACE FUNCTION Insertstorage_domain_dynamic (
    v_available_disk_size INT,
    v_id UUID,
    v_used_disk_size INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_domain_dynamic (
        available_disk_size,
        id,
        used_disk_size
        )
    VALUES (
        v_available_disk_size,
        v_id,
        v_used_disk_size
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatestorage_domain_dynamic (
    v_available_disk_size INT,
    v_id UUID,
    v_used_disk_size INT
    )
RETURNS VOID
    --The [storage_domain_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE storage_domain_dynamic
    SET available_disk_size = v_available_disk_size,
        used_disk_size = v_used_disk_size,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStorageDomainConfirmedSize (
    v_confirmed_available_disk_size INT,
    v_vdo_savings INT,
    v_id UUID
    )
RETURNS VOID
    --The [storage_domain_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE storage_domain_dynamic
    SET confirmed_available_disk_size = v_confirmed_available_disk_size,
        vdo_savings = v_vdo_savings,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStorageDomainExternalStatus (
    v_storage_id UUID,
    v_external_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_domain_dynamic
    SET external_status = v_external_status
    WHERE id = v_storage_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletestorage_domain_dynamic (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM storage_domain_dynamic
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromstorage_domain_dynamic ()
RETURNS SETOF storage_domain_dynamic STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domain_dynamic;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_domain_dynamicByid (v_id UUID)
RETURNS SETOF storage_domain_dynamic STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_domain_dynamic
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [storage_domain_dynamic] table doesn't have at least one foreign key column or the foreign keys are also primary keys.
----------------------------------------------------------------
-- [storage_pool_iso_map] Table
--
CREATE OR REPLACE FUNCTION Insertstorage_pool_iso_map (
    v_storage_id UUID,
    v_storage_pool_id UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_pool_iso_map (
        storage_id,
        storage_pool_id,
        status
        )
    VALUES (
        v_storage_id,
        v_storage_pool_id,
        v_status
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletestorage_pool_iso_map (
    v_storage_id UUID,
    v_storage_pool_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM storage_pool_iso_map
    WHERE storage_id = v_storage_id
        AND storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromstorage_pool_iso_map ()
RETURNS SETOF storage_pool_iso_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool_iso_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_pool_iso_mapBystorage_idAndBystorage_pool_id (
    v_storage_id UUID,
    v_storage_pool_id UUID
    )
RETURNS SETOF storage_pool_iso_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool_iso_map
    WHERE storage_id = v_storage_id
        AND storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_pool_iso_mapsBystorage_id (v_storage_id UUID)
RETURNS SETOF storage_pool_iso_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_pool_iso_map
    WHERE storage_id = v_storage_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_pool_iso_mapsByBystorage_pool_id (
    v_storage_id UUID,
    v_storage_pool_id UUID
    )
RETURNS SETOF storage_pool_iso_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT storage_pool_iso_map.*
    FROM storage_pool_iso_map
    INNER JOIN storage_domain_static
        ON storage_pool_iso_map.storage_id = storage_domain_static.id
    WHERE storage_pool_id = v_storage_pool_id
        AND storage_domain_static.storage_type != 9
        AND storage_domain_static.storage_type != 10; -- filter Cinder and Managed block storage domains
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatestorage_pool_iso_map_status (
    v_storage_id UUID,
    v_storage_pool_id UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_pool_iso_map
    SET status = v_status
    WHERE storage_pool_id = v_storage_pool_id
        AND storage_id = v_storage_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [storage_pool_iso_map] table doesn't have at least one foreign key column or the foreign keys are also primary keys.
----------------------------------------------------------------
-- [storage_server_connections] Table
--
--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.
CREATE OR REPLACE FUNCTION Insertstorage_server_connections (
    v_connection VARCHAR(250),
    v_id VARCHAR(50),
    v_iqn VARCHAR(128),
    v_port VARCHAR(50),
    v_portal VARCHAR(50),
    v_password TEXT,
    v_storage_type INT,
    v_user_name TEXT,
    v_mount_options VARCHAR(500),
    v_vfs_type VARCHAR(128),
    v_nfs_version VARCHAR(4),
    v_nfs_timeo SMALLINT,
    v_nfs_retrans SMALLINT,
    v_gluster_volume_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_server_connections (
        connection,
        id,
        iqn,
        port,
        portal,
        password,
        storage_type,
        user_name,
        mount_options,
        vfs_type,
        nfs_version,
        nfs_timeo,
        nfs_retrans,
        gluster_volume_id
        )
    VALUES (
        v_connection,
        v_id,
        v_iqn,
        v_port,
        v_portal,
        v_password,
        v_storage_type,
        v_user_name,
        v_mount_options,
        v_vfs_type,
        v_nfs_version,
        v_nfs_timeo,
        v_nfs_retrans,
        v_gluster_volume_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatestorage_server_connections (
    v_connection VARCHAR(250),
    v_id VARCHAR(50),
    v_iqn VARCHAR(128),
    v_password TEXT,
    v_storage_type INT,
    v_port VARCHAR(50),
    v_portal VARCHAR(50),
    v_user_name TEXT,
    v_mount_options VARCHAR(500),
    v_vfs_type VARCHAR(128),
    v_nfs_version VARCHAR(4),
    v_nfs_timeo SMALLINT,
    v_nfs_retrans SMALLINT,
    v_gluster_volume_id UUID
    )
RETURNS VOID
    --The [storage_server_connections] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE storage_server_connections
    SET connection = v_connection,
        iqn = v_iqn,
        password = v_password,
        port = v_port,
        portal = v_portal,
        storage_type = v_storage_type,
        user_name = v_user_name,
        mount_options = v_mount_options,
        vfs_type = v_vfs_type,
        nfs_version = v_nfs_version,
        nfs_timeo = v_nfs_timeo,
        nfs_retrans = v_nfs_retrans,
        gluster_volume_id = v_gluster_volume_id
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletestorage_server_connections (v_id VARCHAR(50))
RETURNS VOID AS $PROCEDURE$
DECLARE v_val VARCHAR(50);

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM storage_server_connections
    WHERE id = v_id
    FOR UPDATE;

    DELETE
    FROM storage_server_connections
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByid (v_id VARCHAR(50))
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByConnection (v_connection VARCHAR(250))
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE connection = v_connection;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByIqn (v_iqn VARCHAR(128))
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE iqn = v_iqn
        OR iqn IS NULL
        AND v_iqn IS NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByIqnAndConnection (
    v_iqn VARCHAR(128),
    v_connection VARCHAR(250)
    )
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE iqn = v_iqn
        AND (
            connection = v_connection
            OR connection IS NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByKey (
    v_iqn VARCHAR(128),
    v_connection VARCHAR(250),
    v_port VARCHAR(50),
    v_portal VARCHAR(50),
    v_username VARCHAR(50)
    )
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE (
            iqn = v_iqn
            OR (
                iqn IS NULL
                AND v_iqn IS NULL
                )
            )
        AND (connection = v_connection)
        AND (
            port = v_port
            OR (
                port IS NULL
                AND v_port IS NULL
                )
            )
        AND (
            portal = v_portal
            OR (
                portal IS NULL
                AND v_portal IS NULL
                )
            )
        AND (
            user_name = v_username
            OR (
                user_name IS NULL
                AND v_username IS NULL
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByStorageType (v_storage_type INT)
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE storage_type = v_storage_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllstorage_server_connections ()
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageServerConnectionsByIds (v_ids TEXT)
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE id = ANY (string_to_array(v_ids, ',')::VARCHAR []);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByVolumeGroupId (v_volume_group_id VARCHAR(50))
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT storage_server_connections.*
    FROM LUN_storage_server_connection_map LUN_storage_server_connection_map
    INNER JOIN LUNs
        ON LUN_storage_server_connection_map.LUN_id = LUNs.LUN_id
    INNER JOIN storage_domain_static
        ON LUNs.volume_group_id = storage_domain_static.storage
    INNER JOIN storage_server_connections
        ON LUN_storage_server_connection_map.storage_server_connection = storage_server_connections.id
    WHERE (storage_domain_static.storage = v_volume_group_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageConnectionsByStorageTypeAndStatus (
    v_storage_pool_id UUID,
    v_storage_type INT,
    v_statuses VARCHAR(20)
    )
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
DECLARE statuses INT [];

BEGIN
    statuses := string_to_array(v_statuses, ',')::INT [];

    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE (
            v_storage_type IS NULL
            OR storage_server_connections.storage_type = v_storage_type
            )
        AND (
            id IN (
                SELECT storage
                FROM storage_domains
                WHERE storage_domains.storage_pool_id = v_storage_pool_id
                    AND storage_domains.status = ANY (statuses)
                )
            OR (
                id IN (
                    SELECT lun_storage_server_connection_map.storage_server_connection
                    FROM lun_storage_server_connection_map
                    INNER JOIN luns
                        ON lun_storage_server_connection_map.lun_id = luns.lun_id
                    INNER JOIN storage_domains
                        ON luns.volume_group_id = storage_domains.storage
                    WHERE (
                            storage_domains.storage_pool_id = v_storage_pool_id
                            AND storage_domains.status = ANY (statuses)
                            )
                    )
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getstorage_server_connectionsByLunId (v_lunId VARCHAR(50))
RETURNS SETOF storage_server_connections STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT storage_server_connections.*
    FROM storage_server_connections storage_server_connections
    INNER JOIN lun_storage_server_connection_map
        ON lun_storage_server_connection_map.storage_server_connection = storage_server_connections.id
    WHERE (lun_storage_server_connection_map.lun_id = v_lunId);
END;$PROCEDURE$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [storage_server_connections] table doesn't have at least one foreign key column or the foreign keys are also primary keys.
----------------------------------------------------------------
-- [LUN_storage_server_connection_map] Table
--
CREATE OR REPLACE FUNCTION InsertLUN_storage_server_connection_map (
    v_LUN_id VARCHAR(255),
    v_storage_server_connection VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO LUN_storage_server_connection_map (
        LUN_id,
        storage_server_connection
        )
    VALUES (
        v_LUN_id,
        v_storage_server_connection
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateLUN_storage_server_connection_map (
    v_LUN_id VARCHAR(255),
    v_storage_server_connection VARCHAR(50)
    )
RETURNS VOID
    --The [LUN_storage_server_connection_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteLUN_storage_server_connection_map (
    v_LUN_id VARCHAR(255),
    v_storage_server_connection VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM LUN_storage_server_connection_map
    WHERE LUN_id = v_LUN_id
        AND storage_server_connection = v_storage_server_connection;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromLUN_storage_server_connection_map ()
RETURNS SETOF LUN_storage_server_connection_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM LUN_storage_server_connection_map lUN_storage_server_connection_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLUN_storage_server_connection_mapByLUN (
    v_LUN_id VARCHAR(255)
    )
RETURNS SETOF LUN_storage_server_connection_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM LUN_storage_server_connection_map lUN_storage_server_connection_map
    WHERE LUN_id = v_LUN_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetLUN_storage_server_connection_mapByLUNBystorage_server_conn (
    v_LUN_id VARCHAR(255),
    v_storage_server_connection VARCHAR(50)
    )
RETURNS SETOF LUN_storage_server_connection_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM LUN_storage_server_connection_map lUN_storage_server_connection_map
    WHERE LUN_id = v_LUN_id
        AND storage_server_connection = v_storage_server_connection;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertStorageServerConnectionExtension (
    v_id UUID,
    v_vds_id UUID,
    v_iqn VARCHAR(128),
    v_user_name TEXT,
    v_password TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_server_connection_extension (
        id,
        vds_id,
        iqn,
        user_name,
        password
        )
    VALUES (
        v_id,
        v_vds_id,
        v_iqn,
        v_user_name,
        v_password
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStorageServerConnectionExtension (
    v_id UUID,
    v_vds_id UUID,
    v_iqn VARCHAR(128),
    v_user_name TEXT,
    v_password TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_server_connection_extension
    SET vds_id = v_vds_id,
        iqn = v_iqn,
        user_name = v_user_name,
        password = v_password
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteStorageServerConnectionExtension (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM storage_server_connection_extension
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageServerConnectionExtensionById (v_id UUID)
RETURNS SETOF storage_server_connection_extension STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connection_extension
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageServerConnectionExtensionsByHostId (v_vds_id UUID)
RETURNS SETOF storage_server_connection_extension STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connection_extension
    WHERE vds_id = v_vds_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageServerConnectionExtensionsByHostIdAndTarget (
    v_vds_id UUID,
    v_iqn VARCHAR(128)
    )
RETURNS SETOF storage_server_connection_extension STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connection_extension
    WHERE vds_id = v_vds_id
        AND iqn = v_iqn;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromStorageServerConnectionExtensions ()
RETURNS SETOF storage_server_connection_extension STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connection_extension;
END;$PROCEDURE$
LANGUAGE plpgsql;


