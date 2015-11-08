

----------------------------------------------------------------------
--  Libvirt Secrets Table
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetLibvirtSecretByLibvirtSecretId (v_secret_id UUID)
RETURNS SETOF libvirt_secrets STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM libvirt_secrets
    WHERE secret_id = v_secret_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertLibvirtSecret (
    v_secret_id UUID,
    v_secret_value TEXT,
    v_secret_usage_type INT,
    v_secret_description TEXT,
    v_provider_id UUID,
    v__create_date TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO libvirt_secrets (
        secret_id,
        secret_value,
        secret_usage_type,
        secret_description,
        provider_id,
        _create_date
        )
    VALUES (
        v_secret_id,
        v_secret_value,
        v_secret_usage_type,
        v_secret_description,
        v_provider_id,
        v__create_date
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateLibvirtSecret (
    v_secret_id UUID,
    v_secret_value TEXT,
    v_secret_usage_type INT,
    v_secret_description TEXT,
    v_provider_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE libvirt_secrets
    SET secret_id = v_secret_id,
        secret_value = v_secret_value,
        secret_usage_type = v_secret_usage_type,
        secret_description = v_secret_description,
        provider_id = v_provider_id,
        _update_date = LOCALTIMESTAMP
    WHERE secret_id = v_secret_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteLibvirtSecret (v_secret_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM libvirt_secrets
    WHERE secret_id = v_secret_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromLibvirtSecrets ()
RETURNS SETOF libvirt_secrets STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM libvirt_secrets;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllLibvirtSecretsByProviderId (v_provider_id UUID)
RETURNS SETOF libvirt_secrets STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM libvirt_secrets
    WHERE provider_id = v_provider_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLibvirtSecretsByPoolIdOnActiveDomains (v_storage_pool_id UUID)
RETURNS SETOF libvirt_secrets STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT libvirt_secrets.*
    FROM libvirt_secrets
    INNER JOIN storage_domain_static
        ON CAST(libvirt_secrets.provider_id AS VARCHAR) = storage_domain_static.storage
    INNER JOIN storage_pool_iso_map
        ON storage_domain_static.id = storage_pool_iso_map.storage_id
    WHERE storage_pool_iso_map.storage_pool_id = v_storage_pool_id
        AND storage_pool_iso_map.status = 3;-- Active
END;$PROCEDURE$
LANGUAGE plpgsql;


