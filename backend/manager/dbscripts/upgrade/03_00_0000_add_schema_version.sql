create or replace function fn_create_schema_version ()
returns void
AS $procedure$
begin
if not exists (select 1 from information_schema.tables where table_name = 'schema_version') then
  CREATE SEQUENCE schema_version_seq INCREMENT BY 1 START WITH 1;
  CREATE TABLE schema_version
  (
    id INTEGER DEFAULT NEXTVAL('schema_version_seq') NOT NULL,
    "version" varchar(10) NOT NULL,
    script varchar(255) NOT NULL,
    checksum varchar(128),
    installed_by varchar(30) NOT NULL,
    started_at timestamp  DEFAULT now(),
    ended_at timestamp ,
    state character varying(15) NOT NULL,
    "current" boolean NOT NULL,
    CONSTRAINT schema_version_primary_key PRIMARY KEY (id)
  );

  insert into schema_version(version,script,checksum,installed_by,ended_at,state,current)
  values ('03000000','upgrade/03_00_0000_add_schema_version.sql','0','postgres',now(),'INSTALLED',true);
end if;

END; $procedure$
LANGUAGE plpgsql;

select fn_create_schema_version();
DROP FUNCTION fn_create_schema_version();
