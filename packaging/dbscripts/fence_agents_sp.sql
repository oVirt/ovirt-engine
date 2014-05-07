Create or replace FUNCTION GetFenceAgentsByVdsId(v_vds_guid UUID) RETURNS SETOF fence_agents STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT fence_agents.*
      FROM fence_agents
      WHERE vds_id = v_vds_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetFenceAgentById(v_guid UUID) RETURNS SETOF fence_agents STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT fence_agents.*
      FROM fence_agents
      WHERE id = v_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteFenceAgent(v_guid UUID) RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM fence_agents
    WHERE id = v_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteFenceAgentsByVdsId(v_vds_guid UUID) RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM fence_agents
    WHERE vds_id = v_vds_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateFenceAgent(v_guid UUID ,
      v_vds_id UUID ,
      v_agent_order INTEGER ,
      v_ip VARCHAR(255) ,
      v_type VARCHAR(255) ,
      v_agent_user VARCHAR(255) ,
      v_agent_password text ,
      v_options VARCHAR(255) ,
      v_port INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE fence_agents
      SET vds_id = v_vds_id, agent_order = v_agent_order, ip = v_ip,
      type = v_type, agent_user = v_agent_user, agent_password = v_agent_password,
      port = v_port, options = v_options
      WHERE id = v_guid;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION InsertFenceAgent(
    v_id UUID,
    v_vds_id UUID ,
    v_agent_order INTEGER ,
    v_ip VARCHAR(255) ,
    v_type VARCHAR(255) ,
    v_agent_user VARCHAR(255) ,
    v_agent_password text ,
    v_options VARCHAR(255) ,
    v_port INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO fence_agents(
        id,
        vds_id,
        agent_order,
        ip,
        type,
        agent_user,
        agent_password,
        options,
        port)
    VALUES (
        v_id,
        v_vds_id,
        v_agent_order,
        v_ip,
        v_type,
        v_agent_user,
        v_agent_password,
        v_options,
        v_port);
END; $procedure$
LANGUAGE plpgsql;
