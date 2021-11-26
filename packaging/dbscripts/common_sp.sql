

--------------------------------------------------
-- DB helper functions
--------------------------------------------------
-- Creates a column in the given table (if not exists)
CREATE OR REPLACE FUNCTION fn_db_add_column (
    v_table VARCHAR(128),
    v_column VARCHAR(128),
    v_column_def TEXT
    )
RETURNS void AS $PROCEDURE$
DECLARE v_sql TEXT;

BEGIN
        v_sql := 'ALTER TABLE ' || v_table || ' ADD COLUMN IF NOT EXISTS ' || v_column || ' ' || v_column_def;
        EXECUTE v_sql;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- delete a column from a table and all its dependencied
CREATE OR REPLACE FUNCTION fn_db_drop_column (
    v_table VARCHAR(128),
    v_column VARCHAR(128)
    )
RETURNS void AS $PROCEDURE$
DECLARE v_sql TEXT;

BEGIN
        v_sql := 'ALTER TABLE ' || v_table || ' DROP COLUMN IF EXISTS ' || v_column;
        EXECUTE v_sql;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Changes a column data type (if value conversion is supported)
CREATE OR REPLACE FUNCTION fn_db_change_column_type (
    v_table VARCHAR(128),
    v_column VARCHAR(128),
    v_type VARCHAR(128),
    v_new_type VARCHAR(128)
    )
RETURNS void AS $PROCEDURE$
DECLARE v_sql TEXT;

BEGIN
    v_sql := 'ALTER TABLE ' || v_table || ' ALTER COLUMN ' || v_column || ' TYPE ' || v_new_type;
    EXECUTE v_sql;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Changes a column to allow/disallow NULL values
CREATE OR REPLACE FUNCTION fn_db_change_column_null (
    v_table VARCHAR(128),
    v_column VARCHAR(128),
    v_allow_null BOOLEAN
    )
RETURNS void AS $PROCEDURE$
DECLARE v_sql TEXT;

BEGIN
    IF (v_allow_null) THEN
        v_sql := 'ALTER TABLE ' || v_table || ' ALTER COLUMN ' || v_column || ' DROP NOT NULL';
    ELSE
        v_sql := 'ALTER TABLE ' || v_table || ' ALTER COLUMN ' || v_column || ' SET NOT NULL';
    END IF;
    EXECUTE v_sql;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- rename a column for a given table
CREATE OR REPLACE FUNCTION fn_db_rename_column (
    v_table VARCHAR(128),
    v_column VARCHAR(128),
    v_new_name VARCHAR(128)
    )
RETURNS void AS $PROCEDURE$
DECLARE v_sql TEXT;

BEGIN
    v_sql := 'ALTER TABLE ' || v_table || ' RENAME COLUMN ' || v_column || ' TO ' || v_new_name;
    EXECUTE v_sql;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- rename a table
CREATE OR REPLACE FUNCTION fn_db_rename_table (
    v_table VARCHAR(128),
    v_new_name VARCHAR(128)
    )
RETURNS void AS $PROCEDURE$
DECLARE v_sql TEXT;

BEGIN
        v_sql := 'ALTER TABLE ' || v_table || ' RENAME TO ' || v_new_name;
        EXECUTE v_sql;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Adds a value to vdc_options (if not exists)
CREATE OR REPLACE FUNCTION fn_db_add_config_value (
    v_option_name VARCHAR(100),
    v_option_value TEXT,
    v_version VARCHAR(40)
    )
RETURNS void AS $PROCEDURE$
BEGIN
    IF (
            NOT EXISTS (
                SELECT 1
                FROM vdc_options
                WHERE option_name ilike v_option_name
                    AND version = v_version
                )
            ) THEN
        BEGIN
            INSERT INTO vdc_options (
                option_name,
                option_value,
                version,
                default_value
                )
            VALUES (
                v_option_name,
                v_option_value,
                v_version,
                v_option_value
                );
        END;
    ELSE
        BEGIN
            -- We need to set default value to not have it empty after upgrade from previous versions
            UPDATE vdc_options SET
                default_value = v_option_value
            WHERE option_name ilike v_option_name
                AND version = v_version
                AND default_value IS NULL;
            END;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Deletes a key from vdc_options if exists, for all its versions
CREATE OR REPLACE FUNCTION fn_db_delete_config_value_all_versions (v_option_name VARCHAR(100))
RETURNS void AS $PROCEDURE$
BEGIN
    IF (
            EXISTS (
                SELECT 1
                FROM vdc_options
                WHERE option_name ilike v_option_name
                )
            ) THEN
        BEGIN
            DELETE
            FROM vdc_options
            WHERE option_name ilike v_option_name;
        END;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Deletes a key from vdc_options (if exists)
CREATE OR REPLACE FUNCTION fn_db_delete_config_value (
    v_option_name VARCHAR(100),
    v_version TEXT
    )
RETURNS void AS $PROCEDURE$
BEGIN
    IF (
            EXISTS (
                SELECT 1
                FROM vdc_options
                WHERE option_name ilike v_option_name
                    AND version IN (
                        SELECT ID
                        FROM fnSplitter(v_version)
                        )
                )
            ) THEN
        BEGIN
            DELETE
            FROM vdc_options
            WHERE option_name ilike v_option_name
                AND version IN (
                    SELECT ID
                    FROM fnSplitter(v_version)
                    );
        END;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Deletes a key from vdc_options by version/versions(comma separated)
CREATE OR REPLACE FUNCTION fn_db_delete_config_for_version (v_version TEXT)
RETURNS void AS $PROCEDURE$
BEGIN
    DELETE
    FROM vdc_options
    WHERE version IN (
            SELECT ID
            FROM fnSplitter(v_version)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Updates a value in vdc_options (if exists)
CREATE OR REPLACE FUNCTION fn_db_update_config_value (
    v_option_name VARCHAR(100),
    v_option_value TEXT,
    v_version VARCHAR(40)
    )
RETURNS void AS $PROCEDURE$
BEGIN
    IF (
            EXISTS (
                SELECT 1
                FROM vdc_options
                WHERE option_name ilike v_option_name
                    AND version = v_version
                )
            ) THEN
        BEGIN
            UPDATE vdc_options
            SET option_value = v_option_value,
                default_value = v_option_value
            WHERE option_name ilike v_option_name
                AND version = v_version;
        END;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Updates a value in vdc_options (if exists) if default value wasn't changed
CREATE OR REPLACE FUNCTION fn_db_update_default_config_value (
    v_option_name VARCHAR(100),
    v_default_option_value TEXT,
    v_option_value TEXT,
    v_version VARCHAR(40),
    v_ignore_default_value_case boolean
    )
RETURNS void AS $PROCEDURE$
BEGIN
    IF (
            EXISTS (
                SELECT 1
                FROM vdc_options
                WHERE option_name ilike v_option_name
                    AND version = v_version
                )
            ) THEN
    BEGIN
        IF (v_ignore_default_value_case) THEN
            UPDATE vdc_options
            SET option_value = v_option_value
            WHERE option_name ilike v_option_name
                AND option_value ilike v_default_option_value
                AND version = v_version;
        ELSE
            UPDATE vdc_options
            SET option_value = v_option_value
            WHERE option_name ilike v_option_name
                AND option_value = v_default_option_value
                AND version = v_version;
        END IF;

        -- We need to update default value regardless of user changes
        UPDATE vdc_options
        SET default_value = v_option_value
        WHERE option_name ilike v_option_name
            AND version = v_version;
    END;
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

    --renames an existing config key name, custome option_value modifications are preserved
    CREATE
        OR replace FUNCTION fn_db_rename_config_key (
        v_old_option_name VARCHAR(100),
        v_new_option_name VARCHAR(100),
        v_version VARCHAR(40)
        )
    RETURNS void AS $PROCEDURE$

    DECLARE v_current_option_value TEXT;

    BEGIN
        IF (
                EXISTS (
                    SELECT 1
                    FROM vdc_options
                    WHERE option_name ilike v_old_option_name
                        AND version = v_version
                    )
                ) THEN
            v_current_option_value:= option_value
            FROM vdc_options
            WHERE option_name ilike v_old_option_name
                AND version = v_version;

            UPDATE vdc_options
            SET option_name = v_new_option_name,
                option_value = v_current_option_value
                WHERE option_name ilike v_old_option_name
                    AND version = v_version;
        END IF;

    END;$PROCEDURE$
    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION fn_db_create_constraint (
        v_table VARCHAR(128),
        v_constraint VARCHAR(128),
        v_constraint_sql TEXT
        )
    RETURNS void AS $PROCEDURE$

    BEGIN
        IF NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname ilike v_constraint
                ) THEN
            EXECUTE 'ALTER TABLE ' || v_table || ' ADD CONSTRAINT ' || v_constraint || ' ' || v_constraint_sql;
        END IF;

    END;$PROCEDURE$
    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION fn_db_drop_constraint (
        v_table VARCHAR(128),
        v_constraint VARCHAR(128)
        )
    RETURNS void AS $PROCEDURE$

    BEGIN
            EXECUTE 'ALTER TABLE ' || v_table || ' DROP CONSTRAINT IF EXISTS ' || v_constraint || ' CASCADE';

    END;$PROCEDURE$
    LANGUAGE plpgsql;

    --------------------------------------------------
    -- End of DB helper functions
    --------------------------------------------------
    CREATE
        OR replace FUNCTION CheckDBConnection ()
    RETURNS SETOF INT IMMUTABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT 1;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION generate_drop_all_functions_syntax ()
    RETURNS SETOF TEXT STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT 'drop function if exists ' || ns.nspname || '.' || proname || '(' || oidvectortypes(proargtypes) || ') cascade;'
        FROM pg_proc
        INNER JOIN pg_namespace ns
            ON (pg_proc.pronamespace = ns.oid)
        WHERE
            ns.nspname = 'public'
            AND (
                 probin IS  NULL
                 OR
                 -- prevent dropping installed extension functions
                 probin NOT IN  (SELECT '$libdir/' || extname from pg_extension)
            )
        ORDER BY proname;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION generate_drop_all_views_syntax ()
    RETURNS SETOF TEXT STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT 'DROP VIEW if exists ' || table_name || ' CASCADE;'
        FROM information_schema.VIEWS
        WHERE table_schema = 'public'
        AND table_name NOT ILIKE 'pg_%'
        ORDER BY table_name;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION generate_drop_all_tables_syntax ()
    RETURNS SETOF TEXT STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT 'DROP TABLE if exists ' || table_name || ' CASCADE;'
        FROM information_schema.tables
        WHERE table_schema = 'public'
            AND table_type = 'BASE TABLE'
        ORDER BY table_name;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION generate_drop_all_seq_syntax ()
    RETURNS SETOF TEXT STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT 'DROP SEQUENCE if exists ' || sequence_name || ' CASCADE;'
        FROM information_schema.sequences
        WHERE sequence_schema = 'public'
        ORDER BY sequence_name;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION generate_drop_all_user_types_syntax ()
    RETURNS SETOF TEXT STABLE AS $PROCEDURE$

    BEGIN
        RETURN QUERY

        SELECT 'DROP TYPE if exists ' || c.relname::information_schema.sql_identifier || ' CASCADE;'
        FROM pg_namespace n,
            pg_class c,
            pg_type t
        WHERE n.oid = c.relnamespace
            AND t.typrelid = c.oid
            AND c.relkind = 'c'::"char"
            AND n.nspname = 'public'
        ORDER BY c.relname::information_schema.sql_identifier;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR replace FUNCTION fn_get_column_size (
        v_table VARCHAR(64),
        v_column VARCHAR(64)
        )
    RETURNS INT STABLE AS $PROCEDURE$

    DECLARE retvalue INT;

    BEGIN
        retvalue := character_maximum_length
        FROM information_schema.columns
        WHERE table_schema = 'public'
            AND table_name ilike v_table
            AND column_name ilike v_column
            AND table_schema = 'public'
            AND udt_name IN (
                'char',
                'varchar'
                );

        RETURN retvalue;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION attach_user_to_role (
        v_user_name VARCHAR(255),
        v_domain VARCHAR(255),
        v_namespace VARCHAR(255),
        v_domain_entry_id TEXT,
        v_role_name VARCHAR(255)
        )
    RETURNS void AS $BODY$

    DECLARE selected_user_id uuid;

    input_role_id uuid;

    BEGIN
        SELECT roles.id
        INTO input_role_id
        FROM roles
        WHERE roles.name = v_role_name;

        -- The external identifier is the user identifier converted to an array of
        -- bytes:
        INSERT INTO users (
            user_id,
            external_id,
            namespace,
            name,
            domain,
            username,
            last_admin_check_status
            )
        SELECT uuid_generate_v1(),
            v_domain_entry_id,
            v_namespace,
            v_user_name,
            v_domain,
            v_user_name,
            true
        WHERE NOT EXISTS (
                SELECT 1
                FROM users
                WHERE domain = v_domain
                    AND external_id = v_domain_entry_id
                );

        SELECT user_id
        FROM users
        WHERE domain = v_domain
            AND external_id = v_domain_entry_id
        INTO selected_user_id;

        PERFORM InsertPermission(selected_user_id, uuid_generate_v1(), input_role_id, getGlobalIds('system'), 1);

    END;$BODY$

    LANGUAGE plpgsql;

    CREATE
        OR REPLACE FUNCTION attach_group_to_role (
        v_group_name VARCHAR(255),
        v_role_name VARCHAR(255)
        )
    RETURNS void AS $BODY$
    DECLARE selected_group_id uuid;
    input_role_id uuid;
    BEGIN
       SELECT roles.id
       INTO input_role_id
       FROM roles
       WHERE roles.name = v_role_name;
       -- The external identifier is the user identifier converted to an array of
       -- bytes:
       INSERT INTO ad_groups (
           id,
           name,
           external_id
           )
       SELECT uuid_generate_v1(),
           v_group_name,
           uuid_generate_v1()
       WHERE NOT EXISTS (
               SELECT 1
               FROM ad_groups
               WHERE name = v_group_name
               );
       SELECT id
       FROM ad_groups
       WHERE name = v_group_name
       INTO selected_group_id;
       IF NOT EXISTS (
                   SELECT 1
                   FROM permissions
                   WHERE ad_element_id = selected_group_id
                     AND role_id = input_role_id
                   ) THEN
                         PERFORM InsertPermission(
                             selected_group_id,
                             uuid_generate_v1(),
                             input_role_id,
                             getGlobalIds('system'),
                             1);
       END IF;
    END;$BODY$
    LANGUAGE plpgsql;

    -- a method for adding an action group to a role if doesn't exist
    CREATE
        OR REPLACE FUNCTION fn_db_add_action_group_to_role (
        v_role_id UUID,
        v_action_group_id INT
        )
    RETURNS VOID AS $PROCEDURE$

    BEGIN
        INSERT INTO roles_groups (
            role_id,
            action_group_id
            )
        SELECT v_role_id,
            v_action_group_id
        WHERE NOT EXISTS (
                SELECT 1
                FROM roles_groups
                WHERE role_id = v_role_id
                    AND action_group_id = v_action_group_id
                );

        RETURN;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    -- This function splits a config value: given a config value with one row for 'general', it creates new options
    -- with the old value, for each version, except the v_update_from_version version and beyond, which gets the input value
    CREATE
        OR REPLACE FUNCTION fn_db_split_config_value (
        v_option_name VARCHAR,
        v_old_option_value VARCHAR,
        v_new_option_value VARCHAR,
        v_update_from_version VARCHAR
        )
    RETURNS void AS $BODY$

    DECLARE v_old_value VARCHAR(4000);

    v_cur CURSOR
    FOR

    SELECT DISTINCT version
    FROM vdc_options
    WHERE version <> 'general'
    ORDER BY version;

    v_version VARCHAR(40);

    v_index INT;

    v_count INT;

    v_total_count INT;

    v_version_count INT;

    BEGIN
        v_total_count := count(version)
        FROM vdc_options
        WHERE option_name = v_option_name;

        v_old_value := option_value
        FROM vdc_options
        WHERE option_name = v_option_name
            AND version = 'general';

        v_version_count := count(DISTINCT version)
        FROM vdc_options
        WHERE version <> 'general';

        IF (v_total_count <= v_version_count) THEN
        BEGIN
            IF (v_old_value IS NULL) THEN
                v_old_value := v_old_option_value;
        END IF;
        v_count := count(DISTINCT version)
        FROM vdc_options
        WHERE version <> 'general';

        v_index := 1;

        OPEN v_cur;

        LOOP

            FETCH v_cur
            INTO v_version;

            EXIT when NOT found;

            -- We shouldn't update if already exists
            IF (
                    NOT EXISTS (
                        SELECT 1
                        FROM vdc_options
                        WHERE option_name = v_option_name
                            AND version = v_version
                        )
                    ) THEN
                -- Might not work well for versions such as 3.10, but we currently don't have any
                IF (v_version >= v_update_from_version) THEN
                    INSERT INTO vdc_options (
                        option_name,
                        option_value,
                        version,
                        default_value
                        )
                    VALUES (
                        v_option_name,
                        v_new_option_value,
                        v_version,
                        v_new_option_value
                        );
                ELSE
                    INSERT INTO vdc_options (
                        option_name,
                        option_value,
                        version,
                        default_value
                        )
                    VALUES (
                        v_option_name,
                        v_old_value,
                        v_version,
                        v_old_value
                        );
                END IF;

            END IF;
            v_index := v_index + 1;
        END LOOP;

    CLOSE v_cur;

    DELETE
    FROM vdc_options
    WHERE option_name = v_option_name
        AND version = 'general';
    END;
END IF;

END;$BODY$
    LANGUAGE plpgsql;

-- Function: fn_db_grant_action_group_to_all_roles(integer)
-- This function adds the input v_action_group_id to all the existing roles (both pre-defined and custom), besides the
-- input roles to filter.
CREATE OR REPLACE FUNCTION fn_db_grant_action_group_to_all_roles_filter (
    v_action_group_id INT,
    uuid []
    )
RETURNS void AS $BODY$

DECLARE v_role_id_to_filter alias
FOR $2;

BEGIN
    INSERT INTO roles_groups (
        role_id,
        action_group_id
        )
    SELECT DISTINCT role_id,
        v_action_group_id
    FROM roles_groups rg
    WHERE NOT ARRAY [role_id] <@ v_role_id_to_filter
        AND NOT EXISTS (
            SELECT 1
            FROM roles_groups
            WHERE role_id = rg.role_id
                AND action_group_id = v_action_group_id
            );
END;$BODY$

LANGUAGE plpgsql;

-- The following function accepts a table or view object
-- Values of columns not matching the ones stored for this object in object_column_white_list table
-- will be masked with an empty value.
CREATE OR REPLACE FUNCTION fn_db_mask_object (v_object regclass)
RETURNS setof record AS $BODY$

DECLARE v_sql TEXT;

v_table record;

v_table_name TEXT;

temprec record;

BEGIN
    -- get full table/view name from v_object (i.e <namespace>.<name>)
    SELECT c.relname,
        n.nspname
    INTO v_table
    FROM pg_class c
    INNER JOIN pg_namespace n
        ON c.relnamespace = n.oid
    WHERE c.oid = v_object;

    -- try to get filtered query syntax from previous execution
    IF EXISTS (
            SELECT 1
            FROM object_column_white_list_sql
            WHERE object_name = v_table.relname
            ) THEN
        SELECT sql
        INTO v_sql
        FROM object_column_white_list_sql
        WHERE object_name = v_table.relname;
    ELSE
        v_table_name := quote_ident(v_table.nspname) || '.' || quote_ident(v_table.relname);
        -- compose sql statement while skipping values for columns not defined in object_column_white_list for this table.
    FOR temprec IN
    SELECT a.attname,
        t.typname
    FROM pg_attribute a
    INNER JOIN pg_type t
        ON a.atttypid = t.oid
    WHERE a.attrelid = v_object
        AND a.attnum > 0
        AND NOT a.attisdropped
    ORDER BY a.attnum LOOP v_sql := coalesce(v_sql || ', ', 'SELECT ');

        IF EXISTS (
            SELECT 1
            FROM object_column_white_list
            WHERE object_name = v_table.relname
                AND column_name = temprec.attname
            ) THEN v_sql := v_sql || quote_ident(temprec.attname);
        ELSE
            v_sql := v_sql || 'NULL::' || quote_ident(temprec.typname) || ' as ' || quote_ident(temprec.attname);
        END IF;

    END LOOP;

    v_sql := v_sql || ' FROM ' || v_table_name;

    v_sql := 'SELECT x::' || v_table_name || ' as rec FROM (' || v_sql || ') as x';

    -- save generated query for further use
    INSERT INTO object_column_white_list_sql (
        object_name,
        sql
        )
    VALUES (
        v_table.relname,
        v_sql
        );
    END IF;

    RETURN QUERY

    EXECUTE v_sql;
END;$BODY$
LANGUAGE plpgsql;

-- Adds a table/view new added column to the white list
CREATE OR REPLACE FUNCTION fn_db_add_column_to_object_white_list (
    v_object_name VARCHAR(128),
    v_column_name VARCHAR(128)
    )
RETURNS void AS $PROCEDURE$
BEGIN
    IF (
            NOT EXISTS (
                SELECT 1
                FROM object_column_white_list
                WHERE object_name = v_object_name
                    AND column_name = v_column_name
                )
            ) THEN
    BEGIN
        -- verify that there is such object in db
        IF EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema = 'public'
                    AND table_name = v_object_name
                    AND column_name = v_column_name
                ) THEN
            INSERT INTO object_column_white_list (
                object_name,
                column_name
                )
            VALUES (
                v_object_name,
                v_column_name
                );
        END IF;

    END;
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

    -- Checks if a table given by its name exists in DB
    CREATE
        OR REPLACE FUNCTION fn_db_is_table_exists (v_table VARCHAR(64))
    RETURNS boolean STABLE AS $PROCEDURE$

    DECLARE retvalue boolean;

    BEGIN
        retvalue := EXISTS (
                SELECT *
                FROM information_schema.tables
                WHERE table_schema = 'public'
                    AND table_name ILIKE v_table
                );

        RETURN retvalue;
    END;$PROCEDURE$

    LANGUAGE plpgsql;

    -- Creates an index on an existing table, if there is no WHERE condition, the last argument should be empty ('')
    -- Example : Table T with columns a,b and c
    -- fn_db_create_index('T_INDEX', 'T', 'a,b', '', true); ==> Creates a unique index named T_INDEX on table T (a,b)
    CREATE
        OR replace FUNCTION fn_db_create_index (
        v_index_name VARCHAR(128),
        v_table_name VARCHAR(128),
        v_column_names TEXT,
        v_where_predicate TEXT,
        v_unique boolean
        )
    RETURNS void AS $PROCEDURE$

    DECLARE v_sql TEXT;
            unique_modifier varchar(6);

    BEGIN
        unique_modifier = CASE WHEN v_unique THEN 'UNIQUE'
        ELSE ''
        END;

        v_sql := 'DROP INDEX ' || ' IF EXISTS ' || v_index_name || '; CREATE ' || unique_modifier || ' INDEX ' || v_index_name || ' ON ' || v_table_name || '(' || v_column_names || ')';

        IF v_where_predicate = '' THEN v_sql := v_sql || ';';ELSE
            v_sql := v_sql || ' WHERE ' || v_where_predicate || ';';
        END IF;

    EXECUTE v_sql;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR replace FUNCTION fn_db_drop_index (
        v_index_name VARCHAR(128)
        )
    RETURNS void AS $PROCEDURE$

    DECLARE v_sql TEXT;

    BEGIN
        v_sql := 'DROP INDEX ' || ' IF EXISTS ' || v_index_name || ';' ;
        EXECUTE v_sql;
END;$PROCEDURE$
LANGUAGE plpgsql;


-- Unlocks a specific disk
CREATE OR REPLACE FUNCTION fn_db_unlock_disk (v_id UUID)
RETURNS void AS $PROCEDURE$
DECLARE OK INT;

LOCKED INT;

BEGIN
    OK:= 1;

    LOCKED:= 2;

    UPDATE images
    SET imagestatus = OK
    WHERE imagestatus = LOCKED
        AND image_group_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Unlocks a specific snapshot
CREATE OR REPLACE FUNCTION fn_db_unlock_snapshot (v_id UUID)
RETURNS void AS $PROCEDURE$
DECLARE OK VARCHAR;

LOCKED VARCHAR;

BEGIN
    OK:= 'OK';

    LOCKED:= 'LOCKED';

    UPDATE snapshots
    SET status = OK
    WHERE status = LOCKED
        AND snapshot_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Unlocks all VM/Template disks
CREATE OR REPLACE FUNCTION fn_db_unlock_entity (
    v_object_type VARCHAR(10),
    v_name VARCHAR(255),
    v_recursive boolean
    )
RETURNS void AS $PROCEDURE$
DECLARE DOWN INT;

OK INT;

LOCKED INT;

TEMPLATE_OK INT;

TEMPLATE_LOCKED INT;

IMAGE_LOCKED INT;

SNAPSHOT_OK VARCHAR;

SNAPSHOT_LOCKED VARCHAR;

v_id UUID;

BEGIN
    DOWN:= 0;

    OK:= 1;

    LOCKED:= 2;

    TEMPLATE_OK:= 0;

    TEMPLATE_LOCKED:= 1;

    IMAGE_LOCKED:= 15;

    SNAPSHOT_OK:= 'OK';

    SNAPSHOT_LOCKED:= 'LOCKED';

    v_id := vm_guid
    FROM vm_static
    WHERE vm_name = v_name
        AND entity_type ilike v_object_type;

    -- set VM status to DOWN
    IF (v_object_type = 'vm') THEN
        UPDATE vm_dynamic
        SET status = DOWN
        WHERE status = IMAGE_LOCKED
            AND vm_guid = v_id;
            -- set Template status to OK
    ELSE IF (v_object_type = 'template') THEN
        UPDATE vm_static
        SET template_status = TEMPLATE_OK
        WHERE template_status = TEMPLATE_LOCKED
            AND vm_guid = v_id;
    END IF;

END IF;
 --unlock images and snapshots  if recursive flag is set
IF (v_recursive) THEN
    UPDATE images
    SET imagestatus = OK
    WHERE imagestatus = LOCKED
        AND image_group_id IN (
            SELECT device_id
            FROM vm_device
            WHERE vm_id = v_id
                AND is_plugged
            );

    UPDATE snapshots
    SET status = SNAPSHOT_OK
    WHERE status ilike SNAPSHOT_LOCKED
        AND vm_id = v_id;
END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

-- Unlocks all locked entities
CREATE OR REPLACE FUNCTION fn_db_unlock_all ()
RETURNS void AS $PROCEDURE$
DECLARE DOWN INT;

OK INT;

LOCKED INT;

TEMPLATE_OK INT;

TEMPLATE_LOCKED INT;

IMAGE_LOCKED INT;

SNAPSHOT_OK VARCHAR;

SNAPSHOT_LOCKED VARCHAR;

BEGIN
    DOWN:= 0;

    OK:= 1;

    LOCKED:= 2;

    TEMPLATE_OK:= 0;

    TEMPLATE_LOCKED:= 1;

    IMAGE_LOCKED:= 15;

    SNAPSHOT_OK:= 'OK';

    SNAPSHOT_LOCKED:= 'LOCKED';

    UPDATE vm_static
    SET template_status = TEMPLATE_OK
    WHERE template_status = TEMPLATE_LOCKED;

    UPDATE vm_dynamic
    SET status = DOWN
    WHERE status = IMAGE_LOCKED;

    UPDATE images
    SET imagestatus = OK
    WHERE imagestatus = LOCKED;

    UPDATE snapshots
    SET status = SNAPSHOT_OK
    WHERE status ilike SNAPSHOT_LOCKED;
END;$PROCEDURE$
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
        dc_name VARCHAR,
        spm_host_id uuid,
        spm_host_name VARCHAR,
        task_count INT
        );

CREATE OR REPLACE FUNCTION fn_db_get_async_tasks ()
RETURNS SETOF async_tasks_info_rs STABLE AS $PROCEDURE$
DECLARE v_record async_tasks_info_rs;

-- selects storage_pool_id uuid found in async_tasks
v_tasks_cursor CURSOR
FOR

SELECT DISTINCT storage_pool_id
FROM async_tasks;

BEGIN
    OPEN v_tasks_cursor;

    FETCH v_tasks_cursor
    INTO v_record.dc_id;

    WHILE FOUND
    LOOP
        -- get dc_name and SPM Host id
        v_record.dc_name := name FROM storage_pool WHERE id = v_record.dc_id;
        v_record.spm_host_id := spm_vds_id
        FROM storage_pool
        WHERE id = v_record.dc_id;

        -- get Host name if we have non NULL SPM Host
        IF (v_record.spm_host_id IS NOT NULL) THEN
            v_record.spm_host_name := vds_name
            FROM vds_static
            WHERE vds_id = v_record.spm_host_id;
        ELSE
            v_record.spm_host_name:= '';
        END IF;
        -- get tasks count for this DC
        v_record.task_count := count(*)
        FROM async_tasks
        WHERE position(cast(v_record.dc_id AS VARCHAR) IN action_parameters) > 0;

        -- return the record
        RETURN NEXT v_record;

        FETCH v_tasks_cursor
    INTO v_record.dc_id;

    END LOOP;

CLOSE v_tasks_cursor;

-- return full set of generated records
RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Remove a value from a CSV string in vdc_options
CREATE OR REPLACE FUNCTION fn_db_remove_csv_config_value (
    v_option_name VARCHAR(100),
    v_value VARCHAR(4000),
    v_version VARCHAR(40)
    )
RETURNS void AS $PROCEDURE$
DECLARE v VARCHAR [];

e VARCHAR;

v_result VARCHAR;

v_sep VARCHAR(1);

BEGIN
    v_result := '';

    v_sep := '';

    v := string_to_array(option_value, ',')
    FROM vdc_options
    WHERE option_name = v_option_name
        AND version = v_version;
    FOR

    e IN

    SELECT unnest(v) LOOP

    IF (e != v_value) THEN v_result := v_result || v_sep || e;
        v_sep := ',';
    END IF;

END
LOOP;

    UPDATE vdc_options
    SET option_value = v_result
    WHERE option_name = v_option_name
        AND version = v_version;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- sets the v_val value for v_option_name in vdc_options for all versions before and up to v_version including v_version
-- Remove a UUID value from a CSV string input, returns null on empty string
CREATE OR REPLACE FUNCTION fn_db_remove_uuid_from_csv (
    v_csv_text TEXT,
    v_uuid uuid
    )
RETURNS TEXT STABLE AS $PROCEDURE$
DECLARE v uuid [];

e uuid;

v_result TEXT;

v_sep VARCHAR(1);

BEGIN
    v_result := '';

    v_sep := '';

    v := string_to_array(v_csv_text, ',');
    FOR

    e IN

    SELECT unnest(v) LOOP

    IF (e != v_uuid) THEN v_result := v_result || v_sep || e;
        v_sep := ',';
    END IF;

    END LOOP;

    IF (v_result = '') THEN v_result := NULL;
    END IF;
    RETURN v_result;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_db_get_versions()
RETURNS VARCHAR []  AS $PROCEDURE$

BEGIN

RETURN
ARRAY ['4.2', '4.3', '4.4', '4.5', '4.6', '4.7'];

END;$PROCEDURE$
LANGUAGE plpgsql;

-- please note that versions must be insync with  org.ovirt.engine.core.compat.Version
CREATE OR REPLACE FUNCTION fn_db_add_config_value_for_versions_up_to (
    v_option_name VARCHAR(100),
    v_val VARCHAR(4000),
    v_version VARCHAR(40)
    )
RETURNS void AS $PROCEDURE$
DECLARE i INT;

arr VARCHAR [] := fn_db_get_versions();

BEGIN
    FOR i IN array_lower(arr, 1)..array_upper(arr, 1) LOOP PERFORM fn_db_add_config_value(v_option_name, v_val, arr [i]);
        EXIT WHEN arr [i] = v_version;
    END LOOP;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_db_update_config_value_for_versions_from_up_to (
    v_option_name VARCHAR(100),
    v_val VARCHAR(4000),
    v_from_version VARCHAR(40),
    v_to_version VARCHAR(40)
    )
RETURNS void AS $PROCEDURE$
DECLARE i INT;

arr VARCHAR [] := fn_db_get_versions();

BEGIN
    found := false;
    FOR i IN array_lower(arr, 1)..array_upper(arr, 1) LOOP
	IF  arr [i] != v_from_version THEN
	    IF NOT found THEN
	        CONTINUE;
	    END IF;
        END IF;
	found := true;
	PERFORM fn_db_update_config_value(v_option_name, v_val, arr [i]);
        EXIT WHEN arr [i] = v_to_version;
    END LOOP;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_db_varchar_to_jsonb(v_text VARCHAR, v_default_value JSONB)
RETURNS JSONB IMMUTABLE AS $PROCEDURE$
BEGIN
    RETURN v_text::jsonb;
    EXCEPTION
        WHEN SQLSTATE '22P02' THEN -- '22P02' stands for 'invalid_text_representation', 'invalid input syntax for type json' in this case
            RETURN v_default_value;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- If value in v_table.v_column is jsonb compatible it's left untouched, otherwise it's replaced by v_default_value
-- Helper function for VARCHAR|TEXT -> JSONB column type migration
CREATE OR REPLACE FUNCTION fn_db_update_column_to_jsonb_compatible_values(
    v_table VARCHAR,
    v_column VARCHAR,
    v_default_value JSONB
    )
RETURNS VOID AS $PROCEDURE$
DECLARE
    default_value_string VARCHAR;
BEGIN
    IF (
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
                AND table_name = v_table
                AND column_name = v_column
                AND (data_type = 'character varying' OR data_type = 'text')
        )
    ) THEN
        BEGIN
            default_value_string := CASE
                WHEN v_default_value IS NULL THEN 'NULL'
                ELSE '''' || v_default_value::VARCHAR || ''''
            END;
            EXECUTE 'UPDATE ' || v_table || ' SET ' || v_column || ' = (SELECT fn_db_varchar_to_jsonb(' || v_column || ', ' || default_value_string || '))';
        END;
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Turns all given table columns to default to NULL an adjust existing values

CREATE OR REPLACE FUNCTION fn_db_change_table_string_columns_to_empty_string (
    v_table VARCHAR(128),
    v_column VARCHAR[]
    )
RETURNS void AS $PROCEDURE$
DECLARE
    v_sql TEXT;
    v_num integer := array_length(v_column, 1);
    v_index integer := 1;

BEGIN
    WHILE v_index <= v_num
    LOOP
        IF (
            EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema = 'public'
                    AND table_name ilike v_table
                    AND column_name ilike v_column[v_index]
               )
            ) THEN
            BEGIN
                v_sql := 'UPDATE ' || v_table || ' SET ' || v_column[v_index] || ' = '''' WHERE ' || v_column[v_index] || ' IS NULL' ;
                EXECUTE v_sql;
                v_sql := 'ALTER TABLE ' || v_table || ' ALTER COLUMN ' || v_column[v_index] || ' SET DEFAULT ''''';
                EXECUTE v_sql;
            END;
        ELSE
            RAISE EXCEPTION 'No column named % exists in table %', v_column[v_index] , v_table;
        END IF;
    v_index = v_index + 1;
    END LOOP;
END;$PROCEDURE$
LANGUAGE plpgsql;
