----------------------------------------------------------------------
--  Libvirt Secrets Table
----------------------------------------------------------------------

Create or replace FUNCTION GetLibvirtSecretByLibvirtSecretId(v_secret_id UUID)
RETURNS SETOF libvirt_secrets STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM libvirt_secrets
   WHERE secret_id = v_secret_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertLibvirtSecret(v_secret_id UUID,
  v_secret_value TEXT,
  v_secret_usage_type integer,
  v_secret_description TEXT,
  v_provider_id UUID,
  v__create_date TIMESTAMP WITH TIME ZONE)
RETURNS VOID
   AS $procedure$
BEGIN

   INSERT INTO libvirt_secrets(secret_id,
     secret_value,
     secret_usage_type,
     secret_description,
     provider_id,
     _create_date)
       VALUES(v_secret_id,
         v_secret_value,
         v_secret_usage_type,
         v_secret_description,
         v_provider_id,
         v__create_date);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateLibvirtSecret(v_secret_id UUID,
  v_secret_value TEXT,
  v_secret_usage_type integer,
  v_secret_description TEXT,
  v_provider_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   UPDATE libvirt_secrets
   SET secret_id = v_secret_id,
       secret_value = v_secret_value,
       secret_usage_type = v_secret_usage_type,
       secret_description = v_secret_description,
       provider_id = v_provider_id,
       _update_date = LOCALTIMESTAMP
   WHERE secret_id = v_secret_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteLibvirtSecret(v_secret_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

    DELETE FROM libvirt_secrets
    WHERE secret_id = v_secret_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromLibvirtSecrets()
RETURNS SETOF libvirt_secrets STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM libvirt_secrets;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllLibvirtSecretsByProviderId(v_provider_id UUID)
RETURNS SETOF libvirt_secrets STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM libvirt_secrets
   WHERE provider_id = v_provider_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetLibvirtSecretsByPoolIdOnActiveDomains(v_storage_pool_id UUID)
RETURNS SETOF libvirt_secrets STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT libvirt_secrets.*
   FROM libvirt_secrets
   INNER JOIN storage_domain_static
    ON CAST (libvirt_secrets.provider_id as varchar) = storage_domain_static.storage
   INNER JOIN storage_pool_iso_map
    ON storage_domain_static.id = storage_pool_iso_map.storage_id
   WHERE storage_pool_iso_map.storage_pool_id = v_storage_pool_id AND storage_pool_iso_map.status = 3; -- Active
END; $procedure$
LANGUAGE plpgsql;