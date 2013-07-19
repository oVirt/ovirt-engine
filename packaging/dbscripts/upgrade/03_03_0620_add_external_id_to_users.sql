-- Add a column to the users table to store the identifier assigned by
-- the external directory. This new comlumn is initialized with a copy
-- of the identifier of the user, as before this change we used the
-- external identifier also as internal identifier.

create or replace function __temp_add_external_id_to_users() returns void
as $function$
begin
  if (not exists (select 1 from information_schema.columns where table_name ilike 'users' and column_name ilike 'external_id')) then
    alter table users add column external_id bytea not null default '';
    update users set external_id = decode(replace(user_id::text, '-', ''), 'hex');
    perform fn_db_create_constraint('users', 'users_domain_external_id_unique', 'unique (domain, external_id)');
  end if;
end; $function$
language plpgsql;

select __temp_add_external_id_to_users();

drop function __temp_add_external_id_to_users();
