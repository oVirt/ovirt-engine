

-- The following stored procedures are relevant to oVirt Installer only
CREATE OR REPLACE FUNCTION inst_update_default_storage_pool_type (v_is_local boolean)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_pool
    SET is_local = v_is_local,
        _update_date = LOCALTIMESTAMP
    WHERE storage_pool.name = 'Default'
        AND NOT EXISTS (
            SELECT 1
            FROM storage_domains
            WHERE storage_domains.storage_pool_name = 'Default'
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- This function calls insert_server_connections, insertstorage_domain_static,insertstorage_domain_dynamic
-- Any change to these functions may effect correctness of the installion.
CREATE OR REPLACE FUNCTION inst_add_iso_storage_domain (
    v_storage_domain_id UUID,
    v_name VARCHAR(250),
    v_connection_id uuid,
    v_connection VARCHAR(250),
    v_available INT,
    v_used INT
    )
RETURNS VOID AS $PROCEDURE$
DECLARE

BEGIN
    IF NOT EXISTS (
            SELECT 1
            FROM storage_server_connections
            WHERE connection = v_connection
            ) THEN
        -- Insert storage server connection info
        perform Insertstorage_server_connections(v_connection, cast(v_connection_id AS VARCHAR(250)), NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
        -- Insert storage domain static info
        perform Insertstorage_domain_static(v_storage_domain_id, cast(v_connection_id AS VARCHAR(250)), v_name, v_name, '', 1, 2, '0', 0, FALSE, FALSE, CAST(NULL AS VARCHAR(100)), CAST(NULL AS VARCHAR(100)), CAST(NULL AS INT), CAST(NULL AS INT), FALSE);

    -- Insert storage domain dynamic  info
    perform Insertstorage_domain_dynamic(v_available, v_storage_domain_id, v_used);
    END IF;

    exception when others then RAISE EXCEPTION 'NUM:%, DETAILS:%',
        SQLSTATE,
        SQLERRM;
    END;$PROCEDURE$
LANGUAGE plpgsql;

-- Updates service types(gluster and virt) in cluster table
CREATE OR REPLACE FUNCTION inst_update_service_type (
    v_cluster_id uuid,
    v_virt_service boolean,
    v_gluster_service boolean
    )
RETURNS void AS $PROCEDURE$
BEGIN
    UPDATE cluster
    SET virt_service = v_virt_service,
        gluster_service = v_gluster_service
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Adds a new glance provider, according to the specified arguments
CREATE OR REPLACE FUNCTION inst_add_glance_provider (
    v_provider_id UUID,
    v_provider_name VARCHAR(128),
    v_provider_description VARCHAR(4000),
    v_provider_url VARCHAR(512),
    v_storage_domain_id UUID,
    v_auth_required boolean DEFAULT false,
    v_auth_username VARCHAR(64) DEFAULT NULL,
    v_auth_password TEXT DEFAULT NULL,
    v_auth_url TEXT DEFAULT NULL,
    v_tenant_name VARCHAR(128) DEFAULT NULL
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    -- Adding the Glance provider
    INSERT INTO providers (
        id,
        name,
        description,
        url,
        provider_type,
        auth_required,
        auth_username,
        auth_password,
        auth_url,
        tenant_name
        )
    SELECT v_provider_id,
        v_provider_name,
        v_provider_description,
        v_provider_url,
        'OPENSTACK_IMAGE',
        v_auth_required,
        v_auth_username,
        v_auth_password,
        v_auth_url,
        v_tenant_name
    WHERE NOT EXISTS (
            SELECT id
            FROM providers
            WHERE id = v_provider_id
            );

    -- Adding a proper storage domain static entry
    INSERT INTO storage_domain_static (
        id,
        storage,
        storage_name,
        storage_domain_type,
        storage_type,
        storage_domain_format_type,
        recoverable
        )
    SELECT v_storage_domain_id,
        v_provider_id,
        v_provider_name,
        4,
        8,
        0,
        true
    WHERE NOT EXISTS (
            SELECT id
            FROM storage_domain_static
            WHERE id = v_storage_domain_id
            );

    -- Adding a proper storage domain dynamic entry
    INSERT INTO storage_domain_dynamic (
        id,
        available_disk_size,
        used_disk_size
        )
    SELECT v_storage_domain_id,
        NULL,
        NULL
    WHERE NOT EXISTS (
            SELECT id
            FROM storage_domain_dynamic
            WHERE id = v_storage_domain_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;


