

----------------------------------------------------------------
-- [providers] Table
--
CREATE OR REPLACE FUNCTION InsertProvider (
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
    v_additional_properties TEXT DEFAULT NULL,
    v_auth_url TEXT DEFAULT NULL,
    v_read_only BOOLEAN DEFAULT FALSE,
    v_is_unmanaged BOOLEAN DEFAULT FALSE,
    v_auto_sync BOOLEAN DEFAULT FALSE,
    v_user_domain_name VARCHAR(128) DEFAULT NULL,
    v_project_name VARCHAR(128) DEFAULT NULL,
    v_project_domain_name VARCHAR(128) DEFAULT NULL
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO providers (
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
        additional_properties,
        auth_url,
        read_only,
        is_unmanaged,
        auto_sync,
        user_domain_name,
        project_name,
        project_domain_name
        )
    VALUES (
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
        v_additional_properties,
        v_auth_url,
        v_read_only,
        v_is_unmanaged,
        v_auto_sync,
        v_user_domain_name,
        v_project_name,
        v_project_domain_name
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateProvider (
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
    v_additional_properties TEXT DEFAULT NULL,
    v_auth_url TEXT DEFAULT NULL,
    v_read_only BOOLEAN DEFAULT FALSE,
    v_is_unmanaged BOOLEAN DEFAULT FALSE,
    v_auto_sync BOOLEAN DEFAULT FALSE,
    v_user_domain_name VARCHAR(128) DEFAULT NULL,
    v_project_name VARCHAR(128) DEFAULT NULL,
    v_project_domain_name VARCHAR(128) DEFAULT NULL
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE providers
    SET name = v_name,
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
        additional_properties = v_additional_properties,
        auth_url = v_auth_url,
        read_only = v_read_only,
        is_unmanaged = v_is_unmanaged,
        auto_sync = v_auto_sync,
        user_domain_name = v_user_domain_name,
        project_name = v_project_name,
        project_domain_name = v_project_domain_name
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteProvider (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM providers
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromProviders ()
RETURNS SETOF providers STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM providers;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromProvidersByTypes(v_provider_types varchar[])
RETURNS SETOF providers AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM providers
    WHERE provider_type = ANY(v_provider_types);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetProviderByProviderId (v_id UUID)
RETURNS SETOF providers STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM providers
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetProviderByName (v_name VARCHAR)
RETURNS SETOF providers STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM providers
    WHERE name = v_name;
END;$PROCEDURE$
LANGUAGE plpgsql;


