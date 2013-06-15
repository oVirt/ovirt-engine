-- Add a columns to contain the id of the user that connected to
-- the console:

select fn_db_add_column('vm_dynamic', 'console_user_id', 'uuid');

-- Add a flag to the virtual machine indicating that console
-- reconnect is allowed to regular users, without special
-- privileges:

-- Note that using 'fn_db_add_column' here is not enough because
-- the default value for the new column depends on the value of an
-- existing column, and that is not supported in the default value
-- expressions.

create or replace function __temp_Upgrade_AddAllowConsoleReconnect_03_01_1030() returns void
as $function$
begin
  if (not exists (select 1 from information_schema.columns where table_name ilike 'vm_static' and column_name ilike 'allow_console_reconnect')) then
    alter table vm_static add column allow_console_reconnect boolean not null default false;
    update vm_static set allow_console_reconnect = (vm_type = 1);
  end if;
end; $function$
language plpgsql;

select __temp_Upgrade_AddAllowConsoleReconnect_03_01_1030();

drop function __temp_Upgrade_AddAllowConsoleReconnect_03_01_1030();
