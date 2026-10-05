select fn_db_add_column('storage_server_connections', 'nqn', 'character varying(256)');
select fn_db_add_column('storage_server_connections', 'transport', 'character varying(16)');
select fn_db_add_column('storage_server_connections', 'trsvcid', 'character varying(50)');
select fn_db_add_column('storage_server_connections', 'host_nqn', 'character varying(256)');
select fn_db_add_column('storage_server_connections', 'dhchap_key', 'text');

select fn_db_add_column('vds_dynamic', 'nvme_host_nqn', 'character varying(256)');
select fn_db_create_comment_to_column('vds_dynamic', 'nvme_host_nqn', 'NVMe-oF host NQN of the initiator (from getCapabilities)');

select fn_db_create_comment_to_column('storage_server_connections', 'nqn', 'NVMe-oF target NQN (NVMe Qualified Name)');
select fn_db_create_comment_to_column('storage_server_connections', 'transport', 'NVMe-oF transport type (e.g. tcp)');
select fn_db_create_comment_to_column('storage_server_connections', 'trsvcid', 'NVMe-oF transport service identifier (e.g. TCP port)');
select fn_db_create_comment_to_column('storage_server_connections', 'host_nqn', 'NVMe-oF host NQN used for discovery/connection');
select fn_db_create_comment_to_column('storage_server_connections', 'dhchap_key', 'NVMe-oF DH-HMAC-CHAP key for in-band authentication');

CREATE OR REPLACE FUNCTION GetStorageConnectionsByConnectionPortAndNqn (
    v_nqn VARCHAR(256),
    v_connection VARCHAR(250),
    v_port VARCHAR(50)
    )
RETURNS SETOF storage_server_connections STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM storage_server_connections
    WHERE nqn = v_nqn
        AND connection = v_connection
        AND port = v_port;
END;$FUNCTION$
LANGUAGE plpgsql;
