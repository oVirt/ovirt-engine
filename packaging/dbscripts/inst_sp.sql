

-- The following stored FUNCTIONs are relevant to oVirt Installer only
CREATE OR REPLACE FUNCTION inst_update_default_storage_pool_type (v_is_local boolean)
RETURNS VOID AS $FUNCTION$
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
END;$FUNCTION$
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
RETURNS VOID AS $FUNCTION$
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
    END;$FUNCTION$
LANGUAGE plpgsql;

-- Updates service types(gluster and virt) in cluster table
CREATE OR REPLACE FUNCTION inst_update_service_type (
    v_cluster_id uuid,
    v_virt_service boolean,
    v_gluster_service boolean
    )
RETURNS void AS $FUNCTION$
BEGIN
    UPDATE cluster
    SET virt_service = v_virt_service,
        gluster_service = v_gluster_service
    WHERE cluster_id = v_cluster_id;
END;$FUNCTION$
LANGUAGE plpgsql;


