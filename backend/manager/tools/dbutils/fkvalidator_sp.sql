-- Database FK validation
DROP TYPE IF EXISTS fk_info_rs CASCADE;
CREATE TYPE fk_info_rs AS
    (table_name varchar, table_col varchar, fk_table_name varchar, fk_col varchar );
CREATE OR REPLACE FUNCTION fn_db_validate_fks(v_fix_it boolean)
returns void
AS $procedure$
DECLARE
    v_sql text;
    v_msg text;
    v_rowcount integer;
    v_record fk_info_rs%ROWTYPE;
    v_cur CURSOR FOR
    SELECT
        c.relname as table_name,
        substring(substring ((select pg_get_constraintdef(r.oid)) from '[a-zA-Z0-9_\-][(][a-zA-Z0-9_\-]+[)]') from 2) as table_col,
        c2.relname AS fk_table_name,
        substring ((select pg_get_constraintdef(r.oid)) from ' [(][a-zA-Z0-9_\-]+[)] ') as fk_col
    FROM pg_class c, pg_class c2, pg_constraint r
    WHERE c.relname in (select table_name from information_schema.tables
    where table_schema not in ('pg_catalog','information_schema') and table_type = 'BASE TABLE') AND
        r.confrelid = c.oid AND
        r.contype = 'f' AND
        c2.oid = r.conrelid AND
        pg_get_constraintdef(r.oid) not ilike '%ON DELETE SET %'
    ORDER BY  table_name;

BEGIN
    OPEN v_cur;
    LOOP
        FETCH v_cur INTO v_record;
        EXIT WHEN NOT FOUND;
        IF (v_fix_it) THEN
            v_sql := 'delete from ' || v_record.fk_table_name ||
                      ' where ' || v_record.fk_col || ' not in (select ' ||
                      v_record.table_col || ' from ' || v_record.table_name || ');';
            v_msg := 'Fixing ' ||  v_record.fk_table_name || v_record.fk_col;
        ELSE
            v_sql := 'select ' || v_record.fk_col || ' from ' || v_record.fk_table_name ||
                      ' where ' || v_record.fk_col || ' not in (select ' ||
                      v_record.table_col || ' from ' || v_record.table_name || ');';
            v_msg := 'Constraint violation found in  ' ||  v_record.fk_table_name || v_record.fk_col;
        END IF;
        EXECUTE v_sql;
        GET DIAGNOSTICS v_rowcount = ROW_COUNT;
        IF (v_rowcount > 0) THEN
            RAISE NOTICE '% ... (% record/s)',  v_msg, v_rowcount;
        END IF;

    END LOOP;
    CLOSE v_cur;
END; $procedure$
LANGUAGE plpgsql;
