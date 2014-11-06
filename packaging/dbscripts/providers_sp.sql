

----------------------------------------------------------------
-- [providers] Table
--




Create or replace FUNCTION InsertProvider(
    v_id UUID,
    v_name VARCHAR(128),
    v_description VARCHAR(4000),
    v_url VARCHAR(512),
    v_provider_type VARCHAR(32),
    v_auth_required BOOLEAN,
    v_auth_username VARCHAR(64),
    v_auth_password TEXT,
    v_custom_properties TEXT,
    v_tenant_name VARCHAR DEFAULT NULL,
    v_plugin_type VARCHAR DEFAULT NULL,
    v_agent_configuration TEXT DEFAULT NULL,
    v_auth_url TEXT DEFAULT NULL)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO providers(
        id,
        name,
        description,
        url,
        provider_type,
        auth_required,
        auth_username,
        auth_password,
        custom_properties,
        tenant_name,
        plugin_type,
        agent_configuration,
        auth_url)
    VALUES(
        v_id,
        v_name,
        v_description,
        v_url,
        v_provider_type,
        v_auth_required,
        v_auth_username,
        v_auth_password,
        v_custom_properties,
        v_tenant_name,
        v_plugin_type,
        v_agent_configuration,
        v_auth_url);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateProvider(
    v_id UUID,
    v_name VARCHAR(128),
    v_description VARCHAR(4000),
    v_url VARCHAR(512),
    v_provider_type VARCHAR(32),
    v_auth_required BOOLEAN,
    v_auth_username VARCHAR(64),
    v_auth_password TEXT,
    v_custom_properties TEXT,
    v_tenant_name VARCHAR DEFAULT NULL,
    v_plugin_type VARCHAR DEFAULT NULL,
    v_agent_configuration TEXT DEFAULT NULL,
    v_auth_url TEXT DEFAULT NULL)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE providers
    SET    name = v_name,
           description = v_description,
           url = v_url,
           provider_type = v_provider_type,
           auth_required = v_auth_required,
           auth_username = v_auth_username,
           auth_password = v_auth_password,
           custom_properties = v_custom_properties,
           tenant_name = v_tenant_name,
           plugin_type = v_plugin_type,
           _update_date = NOW(),
           agent_configuration = v_agent_configuration,
           auth_url = v_auth_url
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION DeleteProvider(v_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   providers
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromProviders() RETURNS SETOF providers STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetAllFromProvidersByType(v_provider_type varchar(32)) RETURNS SETOF providers STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers
    WHERE provider_type = v_provider_type;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetProviderByProviderId(v_id UUID)
RETURNS SETOF providers STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetProviderByName(v_name VARCHAR)
RETURNS SETOF providers STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers
    WHERE  name = v_name;
END; $procedure$
LANGUAGE plpgsql;

