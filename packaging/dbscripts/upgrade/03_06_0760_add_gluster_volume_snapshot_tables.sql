-- Add gluster_volume_snapshots table
CREATE TABLE gluster_volume_snapshots
(
    snapshot_id UUID NOT NULL,
    volume_id UUID NOT NULL,
    snapshot_name VARCHAR(1000) NOT NULL,
    description VARCHAR(1024),
    status VARCHAR(32),
    _create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    _update_date TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_snapshot_id PRIMARY KEY(snapshot_id)
) WITH OIDS;
CREATE UNIQUE INDEX IDX_gluster_volume_snapshots_unique ON gluster_volume_snapshots(volume_id, snapshot_name);

-- Add gluster_volume_snapshot_config table
CREATE TABLE gluster_volume_snapshot_config
(
    cluster_id UUID NOT NULL,
    volume_id UUID,
    param_name VARCHAR(128) NOT NULL,
    param_value VARCHAR(128),
    _create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    _update_date TIMESTAMP WITH TIME ZONE
) WITH OIDS;
CREATE UNIQUE INDEX IDX_gluster_volume_snapshot_config_unique ON gluster_volume_snapshot_config(cluster_id, volume_id, param_name);

SELECT fn_db_create_constraint('gluster_volume_snapshots', 'fk_gluster_volume_snapshots_volume_id', 'FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE');
SELECT fn_db_create_constraint('gluster_volume_snapshot_config', 'fk_gluster_volume_snapshot_config_cluster_id', 'FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE');
SELECT fn_db_create_constraint('gluster_volume_snapshot_config', 'fk_gluster_volume_snapshot_config_volume_id', 'FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE');

