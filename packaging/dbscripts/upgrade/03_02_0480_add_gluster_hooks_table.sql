Create or replace FUNCTION __temp_fn_db_add_gluster_hooks_tables()
RETURNS void
AS $function$
BEGIN
    -- Add gluster_hooks table
    CREATE TABLE gluster_hooks
    (
         id UUID NOT NULL,
         cluster_id UUID NOT NULL REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE,
         gluster_command VARCHAR(128) NOT NULL,
         stage VARCHAR(50) NOT NULL,
         name VARCHAR(256) NOT NULL,
         hook_status VARCHAR(50),
         content_type VARCHAR(50),
         checksum VARCHAR(256),
         content text,
         conflict_status INTEGER NOT NULL DEFAULT 0,
         _create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
         _update_date TIMESTAMP WITH TIME ZONE,
         CONSTRAINT pk_gluster_hooks PRIMARY KEY(id)
    ) WITH OIDS;
    CREATE INDEX IDX_gluster_hooks_cluster_id ON gluster_hooks(cluster_id);
    CREATE UNIQUE INDEX IDX_gluster_hooks_unique ON gluster_hooks(cluster_id, gluster_command, stage, name);

    -- Add gluster_server_hooks table
    CREATE TABLE gluster_server_hooks
    (
         hook_id UUID NOT NULL REFERENCES gluster_hooks(id) ON DELETE CASCADE,
         server_id UUID NOT NULL REFERENCES vds_static(vds_id) ON DELETE CASCADE,
         hook_status VARCHAR(50),
         content_type VARCHAR(50),
         checksum VARCHAR(256),
         _create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
         _update_date TIMESTAMP WITH TIME ZONE
    ) WITH OIDS;
    CREATE UNIQUE INDEX IDX_gluster_server_hooks_unique ON gluster_server_hooks(hook_id, server_id);

END; $function$
LANGUAGE plpgsql;

select __temp_fn_db_add_gluster_hooks_tables();
drop function __temp_fn_db_add_gluster_hooks_tables();
