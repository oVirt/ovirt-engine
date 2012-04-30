Create or replace FUNCTION __temp_fn_db_add_transport_types_table() returns void
AS $procedure$
BEGIN

ALTER TABLE gluster_volumes DROP COLUMN transport_type;

-- Add gluster_volume_transport_types table which stores transport types for all volumes
if (not exists (select 1 from INFORMATION_SCHEMA.TABLES where table_name='gluster_volume_transport_types')) then
  begin
    CREATE TABLE gluster_volume_transport_types
    (
        volume_id UUID NOT NULL references gluster_volumes(id) on delete cascade,
        transport_type VARCHAR(32) NOT NULL,
        CONSTRAINT pk_gluster_volume_transport_types PRIMARY KEY(volume_id, transport_type)
    ) WITH OIDS;
  end;
end if;

-- Create partial index for fetching access protocols of a volume
 DROP INDEX if exists IDX_gluster_volume_transport_types_volume_id;
 CREATE INDEX IDX_gluster_volume_transport_types_volume_id ON gluster_volume_transport_types(volume_id);

END; $procedure$
LANGUAGE plpgsql;

select __temp_fn_db_add_transport_types_table();
drop function __temp_fn_db_add_transport_types_table();
