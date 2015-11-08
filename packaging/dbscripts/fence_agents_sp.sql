

CREATE OR REPLACE FUNCTION GetFenceAgentsByVdsId (v_vds_guid UUID)
RETURNS SETOF fence_agents STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT fence_agents.*
    FROM fence_agents
    WHERE vds_id = v_vds_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetFenceAgentById (v_guid UUID)
RETURNS SETOF fence_agents STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT fence_agents.*
    FROM fence_agents
    WHERE id = v_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteFenceAgent (v_guid UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM fence_agents
    WHERE id = v_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteFenceAgentsByVdsId (v_vds_guid UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM fence_agents
    WHERE vds_id = v_vds_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateFenceAgent (
    v_guid UUID,
    v_vds_id UUID,
    v_agent_order INT,
    v_ip VARCHAR(255),
    v_type VARCHAR(255),
    v_agent_user VARCHAR(255),
    v_agent_password TEXT,
    v_options TEXT,
    v_encrypt_options BOOLEAN,
    v_port INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE fence_agents
    SET vds_id = v_vds_id,
        agent_order = v_agent_order,
        ip = v_ip,
        type = v_type,
        agent_user = v_agent_user,
        agent_password = v_agent_password,
        port = v_port,
        options = v_options,
        encrypt_options = v_encrypt_options
    WHERE id = v_guid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertFenceAgent (
    v_id UUID,
    v_vds_id UUID,
    v_agent_order INT,
    v_ip VARCHAR(255),
    v_type VARCHAR(255),
    v_agent_user VARCHAR(255),
    v_agent_password TEXT,
    v_options TEXT,
    v_encrypt_options BOOLEAN,
    v_port INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO fence_agents (
        id,
        vds_id,
        agent_order,
        ip,
        type,
        agent_user,
        agent_password,
        options,
        encrypt_options,
        port
        )
    VALUES (
        v_id,
        v_vds_id,
        v_agent_order,
        v_ip,
        v_type,
        v_agent_user,
        v_agent_password,
        v_options,
        v_encrypt_options,
        v_port
        );
END;$PROCEDURE$
LANGUAGE plpgsql;


