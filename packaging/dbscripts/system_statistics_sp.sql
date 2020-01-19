

DROP TYPE IF EXISTS Getsystem_statistics_rs CASCADE;
CREATE TYPE Getsystem_statistics_rs AS (val INT);

CREATE OR REPLACE FUNCTION Getsystem_statistics (
    v_entity VARCHAR(10), -- /*VM,HOST,USER,SD*/
    v_status VARCHAR(20)
    ) -- comma-separated list of status values
RETURNS Getsystem_statistics_rs STABLE AS $PROCEDURE$
DECLARE v_i Getsystem_statistics_rs;

v_sql VARCHAR(4000);

v_sys_entity VARCHAR(10);

BEGIN
    v_sql := NULL;

    v_sys_entity := upper(v_entity);

    IF v_sys_entity = 'VM' THEN
        v_sql := 'SELECT count(vm_guid) FROM vm_dynamic';
    ELSIF v_sys_entity = 'HOST' THEN
        v_sql := 'SELECT count(vds_id) FROM vds_dynamic';

    ELSIF v_sys_entity = 'USER' THEN
        v_sql := 'SELECT count(user_id) FROM users';

    ELSIF v_sys_entity = 'TSD' THEN
        v_sql := 'SELECT count(id) FROM storage_domain_static';

    ELSIF v_sys_entity = 'ASD' THEN
        v_sql := 'SELECT count(storage_id) FROM storage_pool_iso_map';
    ELSE
        RAISE 'Unknown entity type "%"', v_entity;
    END IF;

    IF v_status != '' THEN
        IF v_sys_entity != 'USER'
            AND v_sys_entity != 'TSD' THEN
            v_sql := coalesce(v_sql, '') || ' where status in (' || coalesce(v_status, '') || ')';
        END IF;
    END IF;
    EXECUTE v_sql
    INTO v_i;

RETURN v_i;END;$PROCEDURE$
LANGUAGE plpgsql;


