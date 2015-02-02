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
        else
            RAISE EXCEPTION 'Table % or Column % does not exist.', v_table, v_column;
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
        else
            RAISE EXCEPTION 'Table % or Column % does not exist.', v_table, v_column;
	end if;
END; $procedure$
LANGUAGE plpgsql;

-- rename a column for a given table
Create or replace FUNCTION fn_db_rename_column(v_table varchar(128), v_column varchar(128), v_new_name varchar(128))
returns void
AS $procedure$
declare
v_sql text;

begin
	if (exists (select 1 from information_schema.columns where table_name ilike v_table and column_name ilike v_column)) then
	    begin
		v_sql := 'ALTER TABLE ' || v_table || ' RENAME COLUMN ' || v_column || ' TO ' || v_new_name;
		EXECUTE v_sql;
            end;
        else
            RAISE EXCEPTION 'Table % or Column % does not exist.', v_table, v_column;
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

-- Deletes a key from vdc_options if exists, for all its versions
create or replace FUNCTION fn_db_delete_config_value_all_versions(v_option_name varchar(100))
returns void
AS $procedure$
begin
    if (exists (select 1 from vdc_options where option_name ilike v_option_name)) then
        begin
            delete from vdc_options where option_name ilike v_option_name;
        end;
    end if;
END; $procedure$
LANGUAGE plpgsql;

-- Deletes a key from vdc_options (if exists)
create or replace FUNCTION fn_db_delete_config_value(v_option_name varchar(100), v_version text)
returns void
AS $procedure$
begin
    if (exists (select 1 from vdc_options where option_name ilike v_option_name and version in (select ID from fnSplitter(v_version)))) then
        begin
            delete from vdc_options where option_name ilike v_option_name and version in (select ID from fnSplitter(v_version));
        end;
    end if;
END; $procedure$
LANGUAGE plpgsql;

-- Deletes a key from vdc_options by version/versions(comma separated)
create or replace FUNCTION fn_db_delete_config_for_version(v_version text)
returns void
AS $procedure$
BEGIN
     delete from vdc_options where version in (select ID from fnSplitter(v_version));
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

--renames an existing config key name, custome option_value modifications are preserved
create or replace FUNCTION fn_db_rename_config_key(v_old_option_name varchar(100),v_new_option_name varchar(100),v_version varchar(40))
returns void
AS $procedure$
DECLARE
    v_current_option_value varchar(4000);
begin
    if (exists (select 1 from vdc_options where option_name ilike v_old_option_name and version = v_version)) then
       v_current_option_value:=option_value from vdc_options where option_name ilike v_old_option_name and version = v_version;
       update vdc_options set option_name = v_new_option_name, option_value = v_current_option_value
           where  option_name ilike v_old_option_name and version = v_version;
    end if;
END; $procedure$
LANGUAGE plpgsql;


create or replace function fn_db_create_constraint (
    v_table varchar(128), v_constraint varchar(128), v_constraint_sql text)
returns void
AS $procedure$
begin
    if  NOT EXISTS (SELECT 1 from pg_constraint where conname ilike v_constraint) then
        execute 'ALTER TABLE ' || v_table ||  ' ADD CONSTRAINT ' || v_constraint || ' ' || v_constraint_sql;
    end if;
END; $procedure$
LANGUAGE plpgsql;

create or replace function fn_db_drop_constraint (
    v_table varchar(128), v_constraint varchar(128))
returns void
AS $procedure$
begin
    if  EXISTS (SELECT 1 from pg_constraint where conname ilike v_constraint) then
        execute 'ALTER TABLE ' || v_table ||  ' DROP CONSTRAINT ' || v_constraint || ' CASCADE';
    end if;
END; $procedure$
LANGUAGE plpgsql;

--------------------------------------------------
-- End of DB helper functions
--------------------------------------------------

Create or replace FUNCTION CheckDBConnection() RETURNS SETOF integer IMMUTABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT 1;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION generate_drop_all_functions_syntax() RETURNS SETOF text STABLE
   AS $procedure$
BEGIN
RETURN QUERY select 'drop function if exists ' || ns.nspname || '.' || proname || '(' || oidvectortypes(proargtypes) || ') cascade;' from pg_proc inner join pg_namespace ns on (pg_proc.pronamespace=ns.oid) where ns.nspname = 'public' order by proname;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION generate_drop_all_views_syntax() RETURNS SETOF text STABLE
   AS $procedure$
BEGIN
RETURN QUERY select 'DROP VIEW if exists ' || table_name || ' CASCADE;' from information_schema.views where table_schema = 'public' order by table_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION generate_drop_all_tables_syntax() RETURNS SETOF text STABLE
   AS $procedure$
BEGIN
RETURN QUERY select 'DROP TABLE if exists ' || table_name || ' CASCADE;' from information_schema.tables where table_schema = 'public' and table_type = 'BASE TABLE' order by table_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION generate_drop_all_seq_syntax() RETURNS SETOF text STABLE
   AS $procedure$
BEGIN
RETURN QUERY select 'DROP SEQUENCE if exists ' || sequence_name || ' CASCADE;' from information_schema.sequences  where sequence_schema = 'public' order by sequence_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION generate_drop_all_user_types_syntax() RETURNS SETOF text STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT 'DROP TYPE if exists ' || c.relname::information_schema.sql_identifier || ' CASCADE;'
   FROM pg_namespace n, pg_class c, pg_type t
   WHERE n.oid = c.relnamespace and t.typrelid = c.oid and c.relkind = 'c'::"char" and
   n.nspname = 'public'
   ORDER BY  c.relname::information_schema.sql_identifier;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION fn_get_column_size( v_table varchar(64), v_column varchar(64)) returns integer STABLE
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

CREATE OR REPLACE FUNCTION attach_user_to_role (
    v_domain_entry_id text,
    v_user_name VARCHAR(255),
    v_domain VARCHAR(255),
    v_role_name VARCHAR(255)
)
RETURNS void AS
$BODY$
DECLARE
   gen_user_id uuid;
   input_role_id uuid;
BEGIN
   select uuid_generate_v1() into gen_user_id;
   select roles.id into input_role_id from roles where roles.name = v_role_name;
   -- The external identifier is the user identifier converted to an array of
   -- bytes:
   insert into users(user_id,external_id,name,domain,username,groups,active,last_admin_check_status) select gen_user_id, v_domain_entry_id, v_user_name, v_domain, v_user_name,'',true,true where not exists (select gen_user_id,name,domain,username,groups,active from users where external_id = v_domain_entry_id);
   insert into permissions(id,role_id,ad_element_id,object_id,object_type_id) select uuid_generate_v1(),  input_role_id, gen_user_id, getGlobalIds('system'), 1 where not exists(select role_id,ad_element_id,object_id,object_type_id from permissions where role_id = input_role_id and ad_element_id = gen_user_id and object_id= getGlobalIds('system') and object_type_id = 1);
END; $BODY$
LANGUAGE plpgsql;

-- a method for adding an action group to a role if doesn't exist
CREATE OR REPLACE FUNCTION fn_db_add_action_group_to_role(v_role_id UUID, v_action_group_id INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
       INSERT INTO roles_groups(role_id,action_group_id)
       SELECT v_role_id, v_action_group_id
       WHERE NOT EXISTS (SELECT 1
                         FROM roles_groups
                         WHERE role_id = v_role_id
                         AND action_group_id = v_action_group_id);
RETURN;
END; $procedure$
LANGUAGE plpgsql;


-- This function splits a config value: given a config value with one row for 'general', it creates new options
-- with the old value, for each version, except the v_update_from_version version and beyond, which gets the input value
CREATE OR REPLACE FUNCTION fn_db_split_config_value(v_option_name character varying, v_old_option_value character varying, v_new_option_value character varying, v_update_from_version character varying)
  RETURNS void AS
$BODY$
declare
v_old_value varchar(4000);
v_cur cursor for select distinct version from vdc_options where version <> 'general' order by version;
v_version varchar(40);
v_index integer;
v_count integer;
v_total_count integer;
v_version_count integer;
begin
    v_total_count := count(version) from vdc_options where option_name = v_option_name;
    v_old_value := option_value from vdc_options where option_name = v_option_name and version = 'general';
    v_version_count := count(distinct version) from vdc_options where version <> 'general';
    if (v_total_count <= v_version_count) then
        begin
            if (v_old_value IS NULL) then
                v_old_value := v_old_option_value;
            end if;
            v_count := count(distinct version) from vdc_options where version <> 'general';
            v_index := 1;
        open v_cur;
        loop
            fetch v_cur into v_version;
            exit when not found;
            -- We shouldn't update if already exists
            if (not exists (select 1 from vdc_options where option_name = v_option_name and version = v_version)) then
                -- Might not work well for versions such as 3.10, but we currently don't have any
                if (v_version >= v_update_from_version) then
                    insert into vdc_options (option_name, option_value, version) values (v_option_name, v_new_option_value, v_version);
                else
                    insert into vdc_options (option_name, option_value, version) values (v_option_name, v_old_value, v_version);
                end if;
            end if;
            v_index := v_index +1;
        end loop;
        close v_cur;
        delete from vdc_options where option_name = v_option_name and version = 'general';
        end;
    end if;
END; $BODY$
LANGUAGE plpgsql;

-- Function: fn_db_grant_action_group_to_all_roles(integer)
-- This function adds the input v_action_group_id to all the existing roles (both pre-defined and custom), besides the
-- input roles to filter.
CREATE OR REPLACE FUNCTION fn_db_grant_action_group_to_all_roles_filter(v_action_group_id integer, uuid[])
  RETURNS void AS
$BODY$
declare
v_role_id_to_filter alias for $2;
begin
    insert into roles_groups (role_id, action_group_id)
    select distinct role_id, v_action_group_id
    from roles_groups rg
    where not ARRAY [role_id] <@ v_role_id_to_filter and not exists (select 1 from roles_groups where role_id = rg.role_id and action_group_id = v_action_group_id);
END; $BODY$
LANGUAGE plpgsql;

-- The following function accepts a table or view object
-- Values of columns not matching the ones stored for this object in object_column_white_list table
-- will be masked with an empty value.
CREATE OR REPLACE FUNCTION fn_db_mask_object(v_object regclass) RETURNS setof record as
$BODY$
DECLARE
    v_sql TEXT;
    v_table record;
    v_table_name TEXT;
    temprec record;
BEGIN
    -- get full table/view name from v_object (i.e <namespace>.<name>)
    select c.relname, n.nspname INTO v_table
        FROM pg_class c join pg_namespace n on c.relnamespace = n.oid WHERE c.oid = v_object;
    -- try to get filtered query syntax from previous execution
    if exists (select 1 from object_column_white_list_sql where object_name = v_table.relname) then
	select sql into v_sql from object_column_white_list_sql where object_name = v_table.relname;
    else
        v_table_name := quote_ident( v_table.nspname ) || '.' || quote_ident( v_table.relname );
        -- compose sql statement while skipping values for columns not defined in object_column_white_list for this table.
        for temprec in select a.attname, t.typname
                       FROM pg_attribute a join pg_type t on a.atttypid = t.oid
                       WHERE a.attrelid = v_object AND a.attnum > 0 AND NOT a.attisdropped ORDER BY a.attnum
        loop
            v_sql := coalesce( v_sql || ', ', 'SELECT ' );
            if exists(select 1 from object_column_white_list
               where object_name = v_table.relname and column_name = temprec.attname) then
               v_sql := v_sql || quote_ident( temprec.attname );
            ELSE
               v_sql := v_sql || 'NULL::' || quote_ident( temprec.typname ) || ' as ' || quote_ident( temprec.attname );
            END IF;
        END LOOP;
        v_sql := v_sql || ' FROM ' || v_table_name;
        v_sql := 'SELECT x::' || v_table_name || ' as rec FROM (' || v_sql || ') as x';
        -- save generated query for further use
        insert into object_column_white_list_sql(object_name,sql) values (v_table.relname, v_sql);
    end if;
    RETURN QUERY EXECUTE v_sql;
END; $BODY$
LANGUAGE plpgsql;

-- Adds a table/view new added column to the white list
create or replace FUNCTION fn_db_add_column_to_object_white_list(v_object_name varchar(128), v_column_name varchar(128))
returns void
AS $procedure$
begin
    if (not exists (select 1 from object_column_white_list
                    where object_name = v_object_name and column_name = v_column_name)) then
        begin
            -- verify that there is such object in db
            if exists (select 1 from information_schema.columns
                       where table_name = v_object_name and column_name = v_column_name) then
                insert into object_column_white_list (object_name, column_name) values (v_object_name, v_column_name);
            end if;
        end;
    end if;
END; $procedure$
LANGUAGE plpgsql;

-- Checks if a table given by its name exists in DB
CREATE OR REPLACE FUNCTION fn_db_is_table_exists (v_table varchar(64)) returns boolean STABLE
   AS $procedure$
   declare
   retvalue  boolean;
BEGIN
   retvalue := EXISTS (
        SELECT * FROM information_schema.tables WHERE table_schema = 'public' AND table_name ILIKE v_table
   );
   return retvalue;
END; $procedure$
LANGUAGE plpgsql;


-- Creates an index on an existing table, if there is no WHERE condition, the last argument should be empty ('')
-- Example : Table T with columns a,b and c
-- fn_db_create_index('T_INDEX', 'T', 'a,b', ''); ==> Creates an index named T_INDEX on table T (a,b)
create or replace FUNCTION fn_db_create_index(v_index_name varchar(128), v_table_name varchar(128), v_column_names text, v_where_predicate text)
returns void
AS $procedure$
DECLARE
    v_sql TEXT;
BEGIN
    v_sql := 'DROP INDEX ' || ' IF EXISTS ' || v_index_name || '; CREATE INDEX ' || v_index_name || ' ON ' || v_table_name || '(' || v_column_names || ')';
    IF v_where_predicate = '' THEN
        v_sql := v_sql || ';';
    ELSE
        v_sql := v_sql || ' WHERE ' || v_where_predicate || ';';
    END IF;
    EXECUTE v_sql;
END; $procedure$
LANGUAGE plpgsql;

-- Unlocks a specific disk
create or replace FUNCTION fn_db_unlock_disk(v_id UUID)
returns void
AS $procedure$
declare
    OK integer;
    LOCKED integer;
begin
    OK:=1;
    LOCKED:=2;
    update images set imagestatus = OK where imagestatus = LOCKED and
    image_group_id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Unlocks a specific snapshot
create or replace FUNCTION fn_db_unlock_snapshot(v_id UUID)
returns void
AS $procedure$
declare
    OK varchar;
    LOCKED varchar;
begin
    OK:='OK';
    LOCKED:='LOCKED';
    update snapshots set status = OK where status = LOCKED and snapshot_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


-- Unlocks all VM/Template disks
create or replace FUNCTION fn_db_unlock_entity(v_object_type varchar(10), v_name varchar(255), v_recursive boolean)
returns void
AS $procedure$
declare
    DOWN integer;
    OK integer;
    LOCKED integer;
    TEMPLATE_OK integer;
    TEMPLATE_LOCKED integer;
    IMAGE_LOCKED integer;
    SNAPSHOT_OK varchar;
    SNAPSHOT_LOCKED varchar;
    v_id UUID;
begin
    DOWN:=0;
    OK:=1;
    LOCKED:=2;
    TEMPLATE_OK:=0;
    TEMPLATE_LOCKED:=1;
    IMAGE_LOCKED:=15;
    SNAPSHOT_OK:='OK';
    SNAPSHOT_LOCKED:='LOCKED';
    v_id := vm_guid from vm_static where vm_name = v_name and entity_type ilike v_object_type;
    -- set VM status to DOWN
    if (v_object_type = 'vm') then
        update vm_dynamic set status = DOWN where status = IMAGE_LOCKED and vm_guid  = v_id;
    -- set Template status to OK
    else
        if (v_object_type = 'template') then
            update vm_static set template_status = TEMPLATE_OK where template_status = TEMPLATE_LOCKED and vm_guid  = v_id;
        end if;
    end if;
    --unlock images and snapshots  if recursive flag is set
    if (v_recursive) then
        update images set imagestatus = OK where imagestatus = LOCKED and
        image_group_id in (select device_id from vm_device where vm_id = v_id and is_plugged);

        update snapshots set status = SNAPSHOT_OK where status ilike SNAPSHOT_LOCKED and vm_id = v_id;
    end if;
END; $procedure$
LANGUAGE plpgsql;

/* Displays DC id , DC name, SPM Host id , SPM Host name and number of async tasks awaiting.

1) create a record type with DC name, DC id, SPM host id, SPM host name, count

2) get all distinct DC ids from async_tasks table

3) Run a cursor for each result in 2)

   a) get DC name
   b) get SPM Host id & name if available
   c) get count of tasks

   return current record

4) return set of generated records
*/

DROP TYPE IF EXISTS async_tasks_info_rs CASCADE;
CREATE TYPE async_tasks_info_rs AS (
        dc_id uuid,
        dc_name character varying,
        spm_host_id uuid,
        spm_host_name character varying,
        task_count integer
);


create or replace FUNCTION fn_db_get_async_tasks()
returns SETOF async_tasks_info_rs STABLE
AS $procedure$
DECLARE
    v_record async_tasks_info_rs;

    -- selects storage_pool_id uuid found in async_tasks
    v_tasks_cursor cursor for select distinct storage_pool_id from async_tasks;
begin

    OPEN v_tasks_cursor;
    FETCH v_tasks_cursor into v_record.dc_id;
    WHILE FOUND LOOP
        -- get dc_name and SPM Host id
        v_record.dc_name := name from storage_pool where id = v_record.dc_id;
        v_record.spm_host_id :=
            spm_vds_id from storage_pool where id = v_record.dc_id;
        -- get Host name if we have non NULL SPM Host
        if (v_record.spm_host_id IS NOT NULL) then
            v_record.spm_host_name :=
                vds_name from vds_static where vds_id = v_record.spm_host_id;
        else
            v_record.spm_host_name:='';
        end if;
        -- get tasks count for this DC
        v_record.task_count := count(*) from async_tasks
            where position (cast(v_record.dc_id as varchar) in action_parameters) > 0;
        -- return the record
        RETURN NEXT v_record;
        FETCH v_tasks_cursor into v_record.dc_id;
    END LOOP;
    CLOSE v_tasks_cursor;
    -- return full set of generated records
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

-- Remove a value from a CSV string in vdc_options
Create or replace FUNCTION fn_db_remove_csv_config_value(v_option_name varchar(100), v_value varchar(4000), v_version varchar(40))
returns void
AS $procedure$
DECLARE
v varchar[];
e varchar;
v_result varchar;
v_sep varchar(1);
BEGIN
v_result := '';
v_sep := '';
    v := string_to_array(option_value, ',') from vdc_options where option_name = v_option_name and version = v_version;
    FOR e in select unnest(v)
    LOOP
        IF (e != v_value) THEN
            v_result := v_result || v_sep || e;
            v_sep := ',';
        END IF;
    END LOOP;
    UPDATE vdc_options set option_value = v_result where option_name = v_option_name and version = v_version;
END; $procedure$
LANGUAGE plpgsql;

