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
                                                      v_status VARCHAR(50),
                                                      v_user_name VARCHAR(255))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_georep_session(session_id, master_volume_id, session_key, slave_host_name,
    slave_host_uuid, slave_volume_name, slave_volume_id, status, user_name)
    VALUES (v_session_id, v_master_volume_id, v_session_key, v_slave_host_name,
    v_slave_host_uuid, v_slave_volume_name, v_slave_volume_id, v_status, v_user_name);
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
                                                            v_data_pending BIGINT,
                                                            v_entry_pending BIGINT,
                                                            v_meta_pending BIGINT,
                                                            v_failures BIGINT,
                                                            v_last_synced_at TIMESTAMP,
                                                            v_checkpoint_time TIMESTAMP,
                                                            v_checkpoint_completed_time TIMESTAMP,
                                                            v_is_checkpoint_completed BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_georep_session_details(session_id, master_brick_id, slave_host_name,
    slave_host_uuid, status, checkpoint_status, crawl_status, data_pending, entry_pending,
    meta_pending, failures, last_synced_at, checkpoint_time, checkpoint_completed_time,
    is_checkpoint_completed)
    VALUES (v_session_id, v_master_brick_id, v_slave_host_name,
    v_slave_host_uuid, v_status, v_checkpoint_status, v_crawl_status, v_data_pending, v_entry_pending,
    v_meta_pending, v_failures, v_last_synced_at, v_checkpoint_time, v_checkpoint_completed_time,
    v_is_checkpoint_completed);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterGeoRepSessionDetail(v_session_id UUID,
                                                            v_master_brick_id UUID,
                                                            v_slave_host_name VARCHAR(50),
                                                            v_slave_host_uuid UUID,
                                                            v_status VARCHAR(20),
                                                            v_checkpoint_status VARCHAR(20),
                                                            v_crawl_status VARCHAR(20),
                                                            v_data_pending BIGINT,
                                                            v_entry_pending BIGINT,
                                                            v_meta_pending BIGINT,
                                                            v_failures BIGINT,
                                                            v_last_synced_at TIMESTAMP,
                                                            v_checkpoint_time TIMESTAMP,
                                                            v_checkpoint_completed_time TIMESTAMP,
                                                            v_is_checkpoint_completed BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_georep_session_details
    SET slave_host_name = v_slave_host_name,
    slave_host_uuid = v_slave_host_uuid,
    status = v_status,
    checkpoint_status = v_checkpoint_status,
    crawl_status = v_crawl_status,
    data_pending = v_data_pending,
    entry_pending = v_entry_pending,
    meta_pending = v_meta_pending,
    failures = v_failures,
    last_synced_at = v_last_synced_at,
    checkpoint_time = v_checkpoint_time,
    checkpoint_completed_time = v_checkpoint_completed_time,
    is_checkpoint_completed = v_is_checkpoint_completed,
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
                                                                       v_slave_host_uuid UUID,
                                                                       v_slave_volume_name VARCHAR(150))
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view
    WHERE master_volume_id = v_master_volume_id
    AND slave_host_uuid = v_slave_host_uuid
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
    RETURN QUERY SELECT * FROM  gluster_georep_session_details
    WHERE session_id = v_session_id order by slave_host_name asc;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterGeoRepSessionDetailsForBrick(v_session_id UUID,
                                                                  v_master_brick_id UUID)
RETURNS SETOF gluster_georep_session_details STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT * FROM  gluster_georep_session_details
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

Create or replace FUNCTION GetGlusterGeoRepSessionUnSetConfig(v_session_id UUID)
RETURNS SETOF gluster_config_master STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_config_master
    WHERE gluster_config_master.config_feature = 'geo_replication' AND gluster_config_master.config_key NOT IN
    (SELECT config_key from gluster_georep_config WHERE gluster_georep_config.session_id = v_session_id);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterGeoRepSessionConfigByKey(v_session_id UUID,
                                                              v_config_key VARCHAR(50))
RETURNS SETOF gluster_geo_rep_config_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_geo_rep_config_view
    WHERE session_id = v_session_id
    AND config_key = v_config_key;
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

Create or replace FUNCTION DeleteGlusterGeoRepSession(v_session_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
   DELETE FROM gluster_georep_session
   WHERE session_id=v_session_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGeoRepSessionBySlaveVolume(v_slave_volume_id UUID)
RETURNS SETOF gluster_georep_sessions_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_georep_sessions_view
    WHERE slave_volume_id = v_slave_volume_id;
END; $procedure$
LANGUAGE plpgsql;
