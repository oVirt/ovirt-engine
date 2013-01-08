create function __temp_drop_old_uuid_functions()
RETURNS void
as $procedure$
begin
    -- removing first the extension from pg9.x installations
    if exists (select 1 from information_schema.views where table_name = 'pg_available_extensions') then
      -- The - is special character , so need to put -- in the name
      EXECUTE 'DROP EXTENSION IF EXISTS uuid--ossp CASCADE;';
    end if;

    --Drops all old UUID functions since we have an internal implementation
    drop function if exists  uuid_nil();
    drop function if exists  uuid_ns_dns();
    drop function if exists  uuid_ns_url();
    drop function if exists  uuid_ns_oid();
    drop function if exists  uuid_ns_x500();
    drop function if exists  uuid_generate_v1();
    drop function if exists  uuid_generate_v1mc();
    drop function if exists  uuid_generate_v3(namespace uuid, name text);
    drop function if exists  uuid_generate_v4();
    drop function if exists  uuid_generate_v5(namespace uuid, name text);
end; $procedure$
LANGUAGE plpgsql;

select __temp_drop_old_uuid_functions();
drop function __temp_drop_old_uuid_functions();
