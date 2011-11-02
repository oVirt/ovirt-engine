--------------------------------------------------
-- DB helper functions
--------------------------------------------------

-- Creates a column in the given table (if not exists)
Create or replace FUNCTION fn_db_add_column(v_table varchar(128), v_column varchar(128), v_column_def text)
returns void
AS $procedure$
declare
v_sql text;

begin
	if (not exists (select 1 from information_schema.columns where table_name ilike v_table and column_name ilike v_column)) then
	    begin
		v_sql := 'ALTER TABLE ' || v_table || ' ADD COLUMN ' || v_column || ' ' || v_column_def;
		EXECUTE v_sql;
            end;
	end if;
END; $procedure$
LANGUAGE plpgsql;

-- delete a column from a table and all its dependencied
Create or replace FUNCTION fn_db_drop_column(v_table varchar(128), v_column varchar(128))
returns void
AS $procedure$
declare
v_sql text;
begin
        if (exists (select 1 from information_schema.columns where table_name ilike v_table and column_name ilike v_column)) then
            begin
                v_sql := 'ALTER TABLE ' || v_table || ' DROP COLUMN ' || v_column;
                EXECUTE v_sql;
            end;
        end if;
end;$procedure$
LANGUAGE plpgsql;

-- Changes a column data type (if value conversion is supported)
Create or replace FUNCTION fn_db_change_column_type(v_table varchar(128), v_column varchar(128),
                                                    v_type varchar(128), v_new_type varchar(128))
returns void
AS $procedure$
declare
v_sql text;

begin
	if (exists (select 1 from information_schema.columns where table_name ilike v_table and column_name ilike v_column and (udt_name ilike v_type or data_type ilike v_type))) then
	    begin
		v_sql := 'ALTER TABLE ' || v_table || ' ALTER COLUMN ' || v_column || ' TYPE ' || v_new_type;
		EXECUTE v_sql;
            end;
	end if;
END; $procedure$
LANGUAGE plpgsql;

-- Adds a value to vdc_options (if not exists)
create or replace FUNCTION fn_db_add_config_value(v_option_name varchar(100), v_option_value varchar(4000),
                                                  v_version varchar(40))
returns void
AS $procedure$
begin
    if (not exists (select 1 from vdc_options where option_name ilike v_option_name and version = v_version)) then
        begin
            insert into vdc_options (option_name, option_value, version) values (v_option_name, v_option_value, v_version);
        end;
    end if;
END; $procedure$
LANGUAGE plpgsql;

-- Deletes a key from vdc_options (if exists)
create or replace FUNCTION fn_db_delete_config_value(v_option_name varchar(100), v_version varchar(40))
returns void
AS $procedure$
begin
    if (exists (select 1 from vdc_options where option_name ilike v_option_name and version = v_version)) then
        begin
            delete from vdc_options where option_name ilike v_option_name and version = v_version;
        end;
    end if;
END; $procedure$
LANGUAGE plpgsql;


-- Updates a value in vdc_options (if exists)
create or replace FUNCTION fn_db_update_config_value(v_option_name varchar(100), v_option_value varchar(4000),
                                                  v_version varchar(40))
returns void
AS $procedure$
begin
    if (exists (select 1 from vdc_options where option_name ilike v_option_name and version = v_version)) then
        begin
            update  vdc_options set option_value = v_option_value
            where option_name ilike v_option_name and version = v_version;
        end;
    end if;
END; $procedure$
LANGUAGE plpgsql;

-- Updates a value in vdc_options (if exists) if default value wasn't changed
create or replace FUNCTION fn_db_update_default_config_value(v_option_name varchar(100),v_default_option_value varchar(4000),v_option_value varchar(4000),v_version varchar(40),v_ignore_default_value_case boolean)
returns void
AS $procedure$
begin
    if (exists (select 1 from vdc_options where option_name ilike v_option_name and version = v_version)) then
        begin
            if (v_ignore_default_value_case)
            then
               update  vdc_options set option_value = v_option_value
               where option_name ilike v_option_name and option_value ilike v_default_option_value and version = v_version;
            else
               update  vdc_options set option_value = v_option_value
               where option_name ilike v_option_name and option_value = v_default_option_value and version = v_version;
            end if;
        end;
    end if;
END; $procedure$
LANGUAGE plpgsql;

--------------------------------------------------
-- End of DB helper functions
--------------------------------------------------

CREATE OR REPLACE FUNCTION isloggingenabled(errorcode text)
  RETURNS boolean AS
$BODY$
-- determine if logging errors is enabled.
-- define in your postgresql.conf:
-- custom_variable_classes = 'engine'
-- set engine.logging = 'true';
-- or this could be in a config table

-- NOTE: We should look at checking error codes as not all are suitable for exceptions some are just notice or info
declare 
    result boolean := false;
    prop text;
begin
    -- check for log setting in postgresql.conf
    select current_setting('engine.logging') into prop;
    if prop = 'true' then
result = true;
    end if;
    return result;
exception
    when others then
result = true;  -- default to log if not specified 
return result;
end;
$BODY$
  LANGUAGE 'plpgsql';





-- ---------------------------------------------------------------------- 
-- License Usage                                                          
-- ---------------------------------------------------------------------- 



Create or replace FUNCTION engine_record_license_usage(v_dt TIMESTAMP WITH TIME ZONE,
	v_lic_desktops INTEGER, 
	v_used_desktops INTEGER, 
	v_lic_sockets INTEGER , 
	v_used_sockets INTEGER)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_current_lic_desktops  INTEGER;
   v_current_used_desktops  INTEGER; 
   v_current_lic_sockets  INTEGER;
   v_current_used_sockets  INTEGER; 
   v_month  INTEGER;
   v_year  CHAR(4);
   v_quarter  CHAR(7);
   v_now  TIMESTAMP WITH TIME ZONE;
   SWV_dt TIMESTAMP WITH TIME ZONE;
   SWV_used_desktops INTEGER;
   SWV_used_sockets INTEGER;
   v_update_info  BOOLEAN;
BEGIN
   SWV_dt := v_dt;
   SWV_used_desktops := v_used_desktops;
   SWV_used_sockets := v_used_sockets;
   if (SWV_dt is NULL) then
      SWV_dt := LOCALTIMESTAMP;
   end if;
   v_month := EXTRACT(MONTH FROM SWV_dt);
   v_year := cast(EXTRACT(YEAR FROM SWV_dt) as CHAR(30));
   if (v_month > 0 and v_month < 4) then
      v_quarter := 'Q1 ' || coalesce(v_year,'');
   else 
      if (v_month > 3 and v_month < 7) then
         v_quarter := 'Q2 ' || coalesce(v_year,'');
      else 
         if (v_month > 6 and v_month < 10) then
            v_quarter := 'Q3 ' || coalesce(v_year,'');
         else
            v_quarter := 'Q4 ' || coalesce(v_year,'');
         end if;
      end if;
   end if;
   if exists(select quarter from engine_license_usage where quarter = v_quarter) then
      select   lic_desktops, max_used_desktops, lic_sockets, max_used_sockets INTO v_current_lic_desktops,v_current_used_desktops,v_current_lic_sockets,v_current_used_sockets from engine_license_usage where quarter = v_quarter;
      v_update_info := 0;
      if (SWV_used_desktops > v_current_used_desktops or
      SWV_used_sockets > v_current_used_sockets or
      v_lic_desktops > v_current_lic_desktops or
      v_lic_sockets > v_current_lic_sockets) then
         v_update_info := 1;
      end if;
      if (SWV_used_desktops <= v_current_used_desktops) then
         SWV_used_desktops := v_current_used_desktops;
      end if;
      if (SWV_used_sockets <= v_current_used_sockets) then
         SWV_used_sockets := v_current_used_sockets;
      end if;
      if (v_update_info = true) then
         update engine_license_usage
         set date = SWV_dt,lic_desktops = v_lic_desktops,lic_sockets = v_lic_sockets,
         max_used_desktops = SWV_used_desktops,max_used_sockets = SWV_used_sockets
         where quarter = v_quarter;
      end if;
   else
insert INTO engine_license_usage  values(SWV_dt,v_quarter,v_lic_desktops,SWV_used_desktops,v_lic_sockets,SWV_used_sockets);
   end if;
	
END; $procedure$
LANGUAGE plpgsql;






CREATE OR REPLACE FUNCTION attach_user_to_su_role()
  RETURNS void AS
$procedure$
   DECLARE
   v_user_entry VARCHAR(255);
   v_user_id  UUID;
   v_name  VARCHAR(255);
   v_domain  VARCHAR(255);
   v_user_name  VARCHAR(255);

   v_document  VARCHAR(64);
   v_index  INTEGER;
BEGIN

   select   option_value INTO v_user_entry from vdc_options where option_name = 'AdUserId';
   select   option_value INTO v_name from vdc_options where option_name = 'AdUserName';
   select   option_value INTO v_domain from vdc_options where option_name = 'DomainName';

   v_index := POSITION(':' IN v_user_entry);
   if ( v_index <> 0 ) then
      v_user_entry := substring( v_user_entry from v_index + 1 );
      v_user_id := CAST( v_user_entry AS uuid );
   end if;

   v_index := POSITION(':' IN v_name);
   if ( v_index <> 0 ) then
      v_name := substring( v_name from v_index + 1 );
   end if;

-- find if name already includes domain (@)
   v_index := POSITION('@' IN v_name);

   if (v_index = 0) then
      v_user_name := coalesce(v_name,'') || '@' || coalesce(v_domain,'');
   else
      v_user_name := v_name;
   end if;


insert into users(user_id,name,domain,username,groups,status) select v_user_id, v_name, v_domain, v_user_name,'',1 where not exists (select user_id,name,domain,username,groups,status from users where user_id = v_user_id and name = v_name and domain = v_domain and username = v_user_name and groups = '' and status = 1);

insert into permissions(id,role_id,ad_element_id,object_id,object_type_id) select uuid_generate_v1(), '00000000-0000-0000-0000-000000000001', v_user_id, getGlobalIds('system'), 1 where not exists(select role_id,ad_element_id,object_id,object_type_id from permissions where role_id = '00000000-0000-0000-0000-000000000001' and ad_element_id = v_user_id and object_id= getGlobalIds('system') and object_type_id = 1);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION CheckDBConnection() RETURNS SETOF integer
   AS $procedure$
BEGIN
    RETURN QUERY SELECT 1;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION generate_drop_all_functions_syntax() RETURNS SETOF text
   AS $procedure$
BEGIN
RETURN QUERY select 'drop function if exists ' || ns.nspname || '.' || proname || '(' || oidvectortypes(proargtypes) || ') cascade;' from pg_proc inner join pg_namespace ns on (pg_proc.pronamespace=ns.oid) where ns.nspname = 'public' and proname not ilike 'uuid%' order by proname;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION generate_drop_all_views_syntax() RETURNS SETOF text
   AS $procedure$
BEGIN
RETURN QUERY select 'DROP VIEW if exists ' || table_name || ' CASCADE;' from information_schema.views where table_schema = 'public' order by table_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION fn_get_column_size( v_table varchar(64), v_column varchar(64)) returns integer
   AS $procedure$
   declare
   retvalue  integer;
BEGIN
   retvalue := character_maximum_length from information_schema.columns
    where
    table_name ilike v_table and column_name ilike v_column and
    table_schema = 'public' and udt_name in ('char','varchar');
   return retvalue;
END; $procedure$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION attach_user_to_su_role(v_user_id VARCHAR(255), v_name VARCHAR(255), v_domain VARCHAR(255))
  RETURNS void AS
$BODY$
   DECLARE
   v_user_name VARCHAR(255);
   v_document  VARCHAR(64);
   v_index  INTEGER;
   input_uuid uuid;
BEGIN
   input_uuid = CAST( v_user_id AS uuid );
-- find if name already includes domain (@)
   v_index := POSITION('@' IN v_name);

   if (v_index = 0) then
      v_user_name := coalesce(v_name,'') || '@' || coalesce(v_domain,'');
   else
      v_user_name := v_name;
   end if;


insert into users(user_id,name,domain,username,groups,status) select input_uuid, v_name, v_domain, v_user_name,'',1 where not exists (select user_id,name,domain,username,groups,status from users where user_id = input_uuid);

insert into permissions(id,role_id,ad_element_id,object_id,object_type_id) select uuid_generate_v1(), '00000000-0000-0000-0000-000000000001', input_uuid, getGlobalIds('system'), 1 where not exists(select role_id,ad_element_id,object_id,object_type_id from permissions where role_id = '00000000-0000-0000-0000-000000000001' and ad_element_id = input_uuid and object_id= getGlobalIds('system') and object_type_id = 1);
END; $BODY$

LANGUAGE plpgsql;
