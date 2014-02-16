-- Add gluster_volume_details table
CREATE TABLE gluster_volume_details
(
    volume_id UUID NOT NULL REFERENCES gluster_volumes(id) ON DELETE CASCADE,
    total_space BIGINT,
    used_space BIGINT,
    free_space BIGINT,
    _update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT pk_gluster_volume_details PRIMARY KEY(volume_id)
) WITH OIDS;

-- Add gluster_volume_brick_details table
CREATE TABLE gluster_volume_brick_details
(
    brick_id UUID NOT NULL REFERENCES gluster_volume_bricks(id) ON DELETE CASCADE,
    total_space BIGINT,
    used_space BIGINT,
    free_space BIGINT,
    _update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT pk_gluster_volume_brick_details PRIMARY KEY(brick_id)
) WITH OIDS;
