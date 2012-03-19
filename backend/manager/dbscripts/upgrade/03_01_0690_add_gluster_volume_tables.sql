Create or replace FUNCTION fn_db_add_gluster_tables_03_01_0690() returns void
AS $procedure$
BEGIN

-- Add gluster_volumes table which reflects the meta data of a Gluster Volume
if (not exists (select 1 from INFORMATION_SCHEMA.TABLES where table_name='gluster_volumes')) then
  begin
    CREATE TABLE gluster_volumes
    (
     id UUID NOT NULL,
     cluster_id UUID NOT NULL references vds_groups(vds_group_id) on delete cascade,
     vol_name VARCHAR(1000) NOT NULL,
     vol_type VARCHAR(32) NOT NULL,
     transport_type VARCHAR(32) NOT NULL,
     status VARCHAR(32) NOT NULL,
     replica_count INTEGER NOT NULL default 0,
     stripe_count INTEGER NOT NULL default 0,
     _create_date TIMESTAMP WITH TIME ZONE NOT NULL default LOCALTIMESTAMP,
     _update_date TIMESTAMP WITH TIME ZONE,
     CONSTRAINT pk_gluster_volumes PRIMARY KEY(id)
    ) WITH OIDS;
  end;
end if;

 DROP INDEX if exists IDX_gluster_volumes_cluster_id;
 CREATE INDEX IDX_gluster_volumes_cluster_id ON gluster_volumes(cluster_id);
 DROP INDEX if exists IDX_gluster_volumes_name_unique;
 CREATE INDEX IDX_gluster_volumes_name_unique ON gluster_volumes(cluster_id, vol_name);

-- Add gluster_volume_bricks table which maps volumes with their bricks
if (not exists (select 1 from INFORMATION_SCHEMA.TABLES where table_name='gluster_volume_bricks')) then
  begin
    CREATE TABLE gluster_volume_bricks
    (
        volume_id UUID NOT NULL references gluster_volumes(id) on delete cascade,
        server_id UUID NOT NULL references vds_static(vds_id) on delete cascade,
        brick_dir VARCHAR(4096) NOT NULL,
        status VARCHAR(32) NOT NULL,
         _create_date TIMESTAMP WITH TIME ZONE NOT NULL default LOCALTIMESTAMP,
         _update_date TIMESTAMP WITH TIME ZONE,
        CONSTRAINT pk_gluster_volume_bricks PRIMARY KEY(volume_id, server_id, brick_dir)
    ) WITH OIDS;
  end;
end if;

-- Create partial index for fetching bricks of a volume
 DROP INDEX if exists IDX_gluster_volume_bricks_volume_id;
 CREATE INDEX IDX_gluster_volume_bricks_volume_id ON gluster_volume_bricks(volume_id);

-- Add gluster_volume_options table which stores volume options for all volumes
if (not exists (select 1 from INFORMATION_SCHEMA.TABLES where table_name='gluster_volume_options')) then
  begin
    CREATE TABLE gluster_volume_options
    (
        volume_id UUID NOT NULL references gluster_volumes(id) on delete cascade,
        option_key VARCHAR(8192) NOT NULL,
        option_val VARCHAR(8192) NOT NULL,
        CONSTRAINT pk_gluster_volume_options PRIMARY KEY(volume_id, option_key)
    ) WITH OIDS;
  end;
end if;

-- Create partial index for fetching options of a volume
 DROP INDEX if exists IDX_gluster_volume_options_volume_id;
 CREATE INDEX IDX_gluster_volume_options_volume_id ON gluster_volume_options(volume_id);

-- Add gluster_volume_access_protocols table which stores access protocols for all volumes
if (not exists (select 1 from INFORMATION_SCHEMA.TABLES where table_name='gluster_volume_access_protocols')) then
  begin
    CREATE TABLE gluster_volume_access_protocols
    (
        volume_id UUID NOT NULL references gluster_volumes(id) on delete cascade,
        access_protocol VARCHAR(32) NOT NULL,
        CONSTRAINT pk_gluster_volume_access_protocols PRIMARY KEY(volume_id, access_protocol)
    ) WITH OIDS;
  end;
end if;

-- Create partial index for fetching access protocols of a volume
 DROP INDEX if exists IDX_gluster_volume_access_protocols_volume_id;
 CREATE INDEX IDX_gluster_volume_access_protocols_volume_id ON gluster_volume_access_protocols(volume_id);

END; $procedure$
LANGUAGE plpgsql;

select fn_db_add_gluster_tables_03_01_0690();
drop function fn_db_add_gluster_tables_03_01_0690();
