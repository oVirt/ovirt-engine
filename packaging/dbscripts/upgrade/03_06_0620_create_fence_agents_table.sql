-- create fence_agents table
CREATE TABLE fence_agents
(
    id UUID NOT NULL CONSTRAINT fence_agent_pk PRIMARY KEY,
    vds_id UUID NOT NULL CONSTRAINT fence_agent_host_id_fk REFERENCES vds_static(vds_id) ON DELETE CASCADE,
    agent_order INTEGER NOT NULL,
    ip VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    agent_user VARCHAR(255) NOT NULL,
    agent_password text NOT NULL,
    port INTEGER,
    options VARCHAR(255) NOT NULL DEFAULT ''
);

-- create index for vds id
CREATE INDEX idx_fence_agents_vds_id ON fence_agents(vds_id);

--Upgrade old PM settings
DROP TYPE IF EXISTS pm_rs CASCADE;
CREATE TYPE pm_rs AS (
    vds_id uuid, ip varchar(255), pm_type varchar(255), pm_user varchar(50), pm_password text, pm_port integer, pm_options varchar(4000),
    pm_secondary_ip varchar(255), pm_secondary_type varchar(255), pm_secondary_user varchar(50), pm_secondary_password text, pm_secondary_port integer,
    pm_secondary_options varchar(4000), pm_secondary_concurrent boolean);


CREATE OR replace FUNCTION __temp_update_pm_settings()
returns void
AS $procedure$
DECLARE
    v_order integer;
    v_record pm_rs;
    v_pm_cursor CURSOR FOR select vds_id, ip, pm_type, pm_user, pm_password, pm_port, pm_options,
                                  pm_secondary_ip, pm_secondary_type, pm_secondary_user, pm_secondary_password,
                                  pm_secondary_port, pm_secondary_options, pm_secondary_concurrent from vds_static
                                  where ip IS NOT NULL;
BEGIN
    OPEN v_pm_cursor;
    FETCH v_pm_cursor into v_record;
    WHILE FOUND LOOP
        INSERT INTO fence_agents (id, vds_id, agent_order, ip, type, agent_user, agent_password, port, options)
           VALUES(uuid_generate_v1(), v_record.vds_id, 1, v_record.ip, v_record.pm_type, v_record.pm_user, v_record.pm_password,
                  v_record.pm_port, v_record.pm_options);
        IF (v_record.pm_secondary_ip IS NOT NULL) THEN
            IF ( v_record.pm_secondary_concurrent) THEN
                v_order:=1;
            ELSE
                v_order:=2;
            END IF;
            INSERT INTO fence_agents (id, vds_id, agent_order, ip, type, agent_user, agent_password, port, options)
               VALUES(uuid_generate_v1(), v_record.vds_id, v_order, v_record.pm_secondary_ip, v_record.pm_secondary_type, v_record.pm_secondary_user,
                      v_record.pm_secondary_password, v_record.pm_secondary_port, v_record.pm_secondary_options);
        END IF;
        FETCH v_pm_cursor into v_record;
    END LOOP;
    CLOSE v_pm_cursor;
    RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_update_pm_settings();
DROP FUNCTION __temp_update_pm_settings();

select fn_db_drop_column ('vds_static', 'ip');
select fn_db_drop_column ('vds_static', 'pm_type');
select fn_db_drop_column ('vds_static', 'pm_user');
select fn_db_drop_column ('vds_static', 'pm_password');
select fn_db_drop_column ('vds_static', 'pm_port');
select fn_db_drop_column ('vds_static', 'pm_options');
select fn_db_drop_column ('vds_static', 'pm_secondary_ip');
select fn_db_drop_column ('vds_static', 'pm_secondary_type');
select fn_db_drop_column ('vds_static', 'pm_secondary_user');
select fn_db_drop_column ('vds_static', 'pm_secondary_password');
select fn_db_drop_column ('vds_static', 'pm_secondary_port');
select fn_db_drop_column ('vds_static', 'pm_secondary_options');
select fn_db_drop_column ('vds_static', 'pm_secondary_concurrent');

