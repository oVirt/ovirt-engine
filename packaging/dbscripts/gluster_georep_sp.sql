/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Geo-replication
 related tables:
      - gluster_georep_session
      - gluster_georep_config
      - gluster_georep_session_details
----------------------------------------------------------------*/

Create or replace FUNCTION InsertGlusterGeoRepSession(v_session_id UUID,
                                                      v_master_volume_id UUID,
                                                      v_session_key VARCHAR(150),
                                                      v_slave_host_name VARCHAR(50),
                                                      v_slave_host_uuid UUID,
                                                      v_slave_volume_name VARCHAR(50),
                                                      v_slave_volume_id UUID,
                                                      v_status VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_georep_session(session_id, master_volume_id, session_key, slave_host_name,
    slave_host_uuid, slave_volume_name, slave_volume_id, status)
    VALUES (v_session_id, v_master_volume_id, v_session_key, v_slave_host_name,
    v_slave_host_uuid, v_slave_volume_name, v_slave_volume_id, v_status);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION InsertGlusterGeoRepSessionConfig(v_session_id UUID,
                                                            v_config_key VARCHAR(50),
                                                            v_config_value VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_georep_config(session_id, config_key, config_value)
    VALUES (v_session_id, v_config_key, v_config_value);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION InsertGlusterGeoRepSessionDetail(v_session_id UUID,
                                                            v_master_brick_id UUID,
                                                            v_slave_host_name VARCHAR(50),
                                                            v_slave_host_uuid UUID,
                                                            v_status VARCHAR(20),
                                                            v_checkpoint_status VARCHAR(20),
                                                            v_crawl_status VARCHAR(20),
                                                            v_files_synced BIGINT,
                                                            v_files_pending BIGINT,
                                                            v_bytes_pending BIGINT,
                                                            v_deletes_pending BIGINT,
                                                            v_files_skipped BIGINT)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_georep_session_details(session_id, master_brick_id, slave_host_name,
    slave_host_uuid, status, checkpoint_status, crawl_status, files_synced, files_pending,
    bytes_pending, deletes_pending, files_skipped)
    VALUES (v_session_id, v_master_brick_id, v_slave_host_name,
    v_slave_host_uuid, v_status, v_checkpoint_status, v_crawl_status, v_files_synced, v_files_pending,
    v_bytes_pending, v_deletes_pending, v_files_skipped);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterGeoRepSessionDetail(v_session_id UUID,
                                                            v_master_brick_id UUID,
                                                            v_slave_host_name VARCHAR(50),
                                                            v_slave_host_uuid UUID,
                                                            v_status VARCHAR(20),
                                                            v_checkpoint_status VARCHAR(20),
                                                            v_crawl_status VARCHAR(20),
                                                            v_files_synced BIGINT,
                                                            v_files_pending BIGINT,
                                                            v_bytes_pending BIGINT,
                                                            v_deletes_pending BIGINT,
                                                            v_files_skipped BIGINT)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_georep_session_details
    SET slave_host_name = v_slave_host_name,
    slave_host_uuid = v_slave_host_uuid,
    status = v_status,
    checkpoint_status = v_checkpoint_status,
    crawl_status = v_crawl_status,
    files_synced = v_files_synced,
    files_pending = v_files_pending,
    bytes_pending = v_bytes_pending,
    deletes_pending = v_deletes_pending,
    files_skipped = v_files_skipped,
    _update_date = LOCALTIMESTAMP
    WHERE session_id = v_session_id AND master_brick_id = v_master_brick_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterGeoRepSessionConfig(v_session_id UUID,
                                                            v_config_key VARCHAR(50),
                                                            v_config_value VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_georep_config
    SET config_value = v_config_value,
    _update_date = LOCALTIMESTAMP
    WHERE session_id = v_session_id AND config_key = v_config_key;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterGeoRepSessionById(v_session_id UUID)
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view
    WHERE session_id = v_session_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterGeoRepSessionsByVolumeId(v_master_volume_id UUID)
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view
    WHERE master_volume_id = v_master_volume_id order by slave_volume_name asc;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterGeoRepSessionsByClusterId(v_cluster_id UUID)
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view
    WHERE cluster_id = v_cluster_id order by slave_volume_name asc;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterGeoRepSessionByKey(v_session_key VARCHAR(150))
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view
    WHERE session_key = v_session_key;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterGeoRepSessionBySlaveHostAndVolume(v_master_volume_id UUID,
                                                                       v_slave_host_name VARCHAR(150),
                                                                       v_slave_volume_name VARCHAR(150))
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view
    WHERE master_volume_id = v_master_volume_id
    AND slave_host_name = v_slave_host_name
    AND slave_volume_name = v_slave_volume_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterGeoRepSessionStatus(v_session_id UUID,
                                                            v_status VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_georep_session
    SET status = v_status,
    _update_date = LOCALTIMESTAMP
    WHERE session_id = v_session_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterGeoRepSession(v_session_id UUID,
                                                      v_status VARCHAR(50),
                                                      v_slave_host_uuid UUID,
                                                      v_slave_volume_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_georep_session
    SET status = v_status,
    slave_host_uuid = v_slave_host_uuid,
    slave_volume_id = v_slave_volume_id,
    _update_date = LOCALTIMESTAMP
    WHERE session_id = v_session_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterGeoRepSessionDetails(v_session_id UUID)
RETURNS SETOF gluster_georep_session_details STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT session_id, master_brick_id, slave_host_uuid,
    slave_host_name, status, checkpoint_status, crawl_status, files_synced, files_pending,
    bytes_pending, deletes_pending, files_skipped, _update_date
    FROM  gluster_georep_session_details
    WHERE session_id = v_session_id order by slave_host_name asc;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterGeoRepSessionDetailsForBrick(v_session_id UUID,
                                                                  v_master_brick_id UUID)
RETURNS SETOF gluster_georep_session_details STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT session_id, master_brick_id, slave_host_uuid,
    slave_host_name, status, checkpoint_status, crawl_status, files_synced, files_pending,
    bytes_pending, deletes_pending, files_skipped, _update_date
    FROM  gluster_georep_session_details
    WHERE session_id = v_session_id AND master_brick_id = v_master_brick_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterGeoRepSessionConfig(v_session_id UUID)
RETURNS SETOF gluster_geo_rep_config_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_geo_rep_config_view
    WHERE session_id = v_session_id order by config_key asc;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllGlusterGeoRepSessions()
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view;
END; $procedure$
LANGUAGE plpgsql;

