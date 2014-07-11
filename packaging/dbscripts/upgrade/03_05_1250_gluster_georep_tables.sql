-- Add gluster_georep_session table
CREATE TABLE gluster_georep_session
(
    session_id UUID NOT NULL,
    master_volume_id UUID NOT NULL,
    session_key VARCHAR(150) NOT NULL,
    slave_host_uuid UUID,
    slave_host_name VARCHAR(50),
    slave_volume_id UUID,
    slave_volume_name VARCHAR(50),
    status VARCHAR,
    _create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    _update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT pk_gluster_georep_session PRIMARY KEY(session_id)
) WITH OIDS;
CREATE UNIQUE INDEX IDX_gluster_georep_session_unique ON gluster_georep_session(master_volume_id, session_key);

-- Add gluster_georep_config
CREATE TABLE gluster_georep_config
(
    session_id UUID NOT NULL,
    config_key VARCHAR(50),
    config_value VARCHAR(50),
    _update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT pk_gluster_georep_config PRIMARY KEY(session_id, config_key)
) WITH OIDS;

-- Add gluster_georep_session_details table
CREATE TABLE gluster_georep_session_details
(
    session_id UUID NOT NULL,
    master_brick_id UUID NOT NULL,
    slave_host_uuid UUID,
    slave_host_name VARCHAR(50) NOT NULL,
    status VARCHAR(20),
    checkpoint_status VARCHAR(20),
    crawl_status VARCHAR(20),
    files_synced BIGINT,
    files_pending BIGINT,
    bytes_pending BIGINT,
    deletes_pending BIGINT,
    files_skipped BIGINT,
    _update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT pk_gluster_georep_session_details PRIMARY KEY(session_id, master_brick_id)
) WITH OIDS;

ALTER TABLE ONLY gluster_georep_config
    ADD CONSTRAINT fk_gluster_georep_config_session_id FOREIGN KEY (session_id) REFERENCES
    gluster_georep_session(session_id) ON DELETE CASCADE;

ALTER TABLE ONLY gluster_georep_session_details
    ADD CONSTRAINT fk_gluster_georep_details_session_id FOREIGN KEY (session_id) REFERENCES
    gluster_georep_session(session_id) ON DELETE CASCADE;

ALTER TABLE ONLY gluster_georep_session_details
    ADD CONSTRAINT fk_gluster_georep_details_brick_id FOREIGN KEY (master_brick_id) REFERENCES
    gluster_volume_bricks(id) ON DELETE CASCADE;

ALTER TABLE ONLY gluster_georep_session
    ADD CONSTRAINT fk_gluster_georep_session_vol_id FOREIGN KEY (master_volume_id) REFERENCES
    gluster_volumes(id) ON DELETE CASCADE;
