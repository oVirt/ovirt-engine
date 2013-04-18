Create or replace FUNCTION __temp_add_gluster_server_table()
RETURNS void
AS $function$
BEGIN
    CREATE TABLE gluster_server
    (
      server_id UUID NOT NULL references vds_static(vds_id) ON DELETE CASCADE,
      gluster_server_uuid UUID NOT NULL,
      CONSTRAINT pk_gluster_server PRIMARY KEY(server_id)
    ) WITH OIDS;
    CREATE UNIQUE INDEX IDX_gluster_server_unique ON gluster_server(server_id, gluster_server_uuid);
END; $function$
LANGUAGE plpgsql;

select __temp_add_gluster_server_table();
drop function __temp_add_gluster_server_table();
