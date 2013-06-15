Create or replace FUNCTION __temp_fn_db_add_quota_tables() returns void
AS $procedure$
begin
-- Add quota table which reflects the meta data of the quota table.
if (not exists (select 1 from INFORMATION_SCHEMA.TABLES where table_name='quota')) then
  begin
  CREATE TABLE quota
  (
     id UUID NOT NULL,
     storage_pool_id UUID NOT NULL references storage_pool(id) on delete cascade,
     quota_name VARCHAR(60) NOT NULL,
     description VARCHAR(250),
     _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
     _update_date TIMESTAMP WITH TIME ZONE,
     threshold_vds_group_percentage INTEGER default 80,
     threshold_storage_percentage INTEGER default 80,
     grace_vds_group_percentage INTEGER default 20,
     grace_storage_percentage INTEGER default 20,
     CONSTRAINT pk_quota PRIMARY KEY(id)
  ) WITH OIDS;
 end;
 end if;

-- Add quota_limitation table which reflects the limitations of the quota.
if (not exists (select 1 from INFORMATION_SCHEMA.TABLES where table_name='quota_limitation')) then
  begin
    CREATE TABLE quota_limitation
    (
        id UUID NOT NULL,
        quota_id UUID NOT NULL references quota(id) on delete cascade,
        storage_id UUID NULL references storage_domain_static(id) on delete cascade,
        vds_group_id UUID NULL references vds_groups(vds_group_id) on delete cascade,
        virtual_cpu INTEGER,
        mem_size_mb BIGINT,
        storage_size_gb BIGINT,
        CONSTRAINT pk_quota_limitation PRIMARY KEY(id)
    ) WITH OIDS;
  end;
 end if;

 DROP INDEX if exists IDX_quota_limitation_quota_id;
 CREATE INDEX IDX_quota_limitation_quota_id ON quota_limitation(quota_id);

-- Create partial index for not null values for fetching storage domains related to quota.
 DROP INDEX if exists IDX_quota_limitation_storage_id;
 CREATE INDEX IDX_quota_limitation_storage_id ON quota_limitation(storage_id)
 WHERE storage_id IS NOT null;

-- Create partial index for not null values for fetching vdsgroup related to quota.
 DROP INDEX if exists IDX_quota_limitation_vds_group_id;
 CREATE INDEX IDX_quota_limitation_vds_group_id ON quota_limitation(vds_group_id)
 WHERE vds_group_id IS NOT null;

-- Create partial index for not null values for fetching storagepool related to quota.
 DROP INDEX if exists IDX_storage_pool_id;
 CREATE INDEX IDX_storage_pool_id ON quota(storage_pool_id)
 WHERE storage_pool_id IS NOT null;

 END; $procedure$
 LANGUAGE plpgsql;

select __temp_fn_db_add_quota_tables();
drop function __temp_fn_db_add_quota_tables();
