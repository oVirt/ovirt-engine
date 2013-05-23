

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
    v_auth_password TEXT)
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
        auth_password)
    VALUES(
        v_id,
        v_name,
        v_description,
        v_url,
        v_provider_type,
        v_auth_required,
        v_auth_username,
        v_auth_password);
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
    v_auth_password TEXT)
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
           _update_date = NOW()
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





Create or replace FUNCTION GetAllFromProviders() RETURNS SETOF providers
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetAllFromProvidersByType(v_provider_type varchar(32)) RETURNS SETOF providers
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers
    WHERE provider_type = v_provider_type;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetProviderByProviderId(v_id UUID)
RETURNS SETOF providers
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetProviderByName(v_name VARCHAR)
RETURNS SETOF providers
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   providers
    WHERE  name = v_name;
END; $procedure$
LANGUAGE plpgsql;

