-- Database FK validation
-- If you add a function here, drop it in fkvalidator_sp_drop.sql
SET client_min_messages=ERROR;
DROP TYPE IF EXISTS fk_info_rs CASCADE;
DROP FUNCTION IF EXISTS fn_db_validate_fks(boolean,boolean);
CREATE TYPE fk_info_rs AS
    (schema_name varchar, table_name varchar, table_col varchar, fk_table_name varchar, fk_col varchar, fk_violation varchar, fk_status integer);
CREATE OR REPLACE FUNCTION fn_db_validate_fks(v_fix_it boolean, v_verbose boolean)
returns SETOF fk_info_rs
AS $procedure$
DECLARE
    v_sql text;
    v_msg text;
    v_output text;
    v_rowcount integer;
    v_record fk_info_rs%ROWTYPE;
    v_cur CURSOR FOR
    SELECT
        n.nspname as schema_name,
        c.relname as table_name,
        substring(substring ((select pg_get_constraintdef(r.oid)) from '[a-zA-Z0-9_, \-][(][a-zA-Z0-9_, \-]+[)]') from 2) as table_col,
        c2.relname AS fk_table_name,
        substring ((select pg_get_constraintdef(r.oid)) from ' [(][a-zA-Z0-9_, \-]+[)] ') as fk_col
    FROM pg_class c, pg_class c2, pg_constraint r, pg_catalog.pg_namespace n
    WHERE c.relname in (select table_name from information_schema.tables
    where table_schema not in ('pg_catalog','information_schema') and table_type = 'BASE TABLE') AND
        r.confrelid = c.oid AND
        r.contype = 'f' AND
        c2.oid = r.conrelid AND
        n.oid = c.relnamespace AND
        pg_get_constraintdef(r.oid) not ilike '%ON DELETE SET %' AND
        -- TODO remove this workaround as soon as no upgrade from 4.3 is supported anymore
        r.conname not in ('fk_vm_interface_vm_static','fk_vm_interface_vm_static_template')
    ORDER BY  table_name;

BEGIN
    OPEN v_cur;
    LOOP
        FETCH v_cur INTO v_record;
        EXIT WHEN NOT FOUND;
        v_record.fk_violation := '';
        v_record.fk_status := 0;
        IF (v_fix_it) THEN
            v_sql := 'delete from ' || v_record.schema_name || '.'  || v_record.fk_table_name ||
                      ' where ' || v_record.fk_col || 'IS NOT NULL and '  || v_record.fk_col || ' not in (select ' ||
                      trim(both '()' from v_record.table_col) || ' from ' || v_record.schema_name || '.'  || v_record.table_name || ');';
            v_msg := 'Fixing violation/s found in ' ||  v_record.fk_table_name ;
        ELSE
            v_sql := 'select ' ||  v_record.fk_col || ' from ' || v_record.schema_name || '.'  || v_record.fk_table_name ||
                      ' where ' || v_record.fk_col || 'IS NOT NULL and ' || v_record.fk_col || ' not in (select ' ||
                      trim(both '()' from v_record.table_col) || ' from ' || v_record.schema_name || '.'  || v_record.table_name || ');';
            v_msg := 'Constraint violation found in  ' ||  v_record.fk_table_name || v_record.fk_col;
        END IF;
        EXECUTE v_sql;
        GET DIAGNOSTICS v_rowcount = ROW_COUNT;
        IF (v_rowcount > 0) THEN
            IF (v_verbose and not v_fix_it) THEN
                v_record.fk_violation := v_msg || E'\nPlease run the following SQL to get the ' || v_rowcount || E' violated record/s: \n' || v_sql || E'\n';
            ELSE
                v_record.fk_violation := v_msg;
            END IF;
            v_record.fk_status := 1;
        END IF;
        RETURN NEXT v_record;

    END LOOP;
    CLOSE v_cur;
END; $procedure$
LANGUAGE plpgsql;
