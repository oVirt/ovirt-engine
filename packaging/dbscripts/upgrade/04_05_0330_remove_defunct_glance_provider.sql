DROP FUNCTION IF EXISTS inst_add_glance_provider(
  v_provider_id uuid,
  v_provider_name varchar(128),
  v_provider_description varchar(4000),
  v_provider_url varchar(512),
  v_storage_domain_id uuid,
  v_auth_required boolean,
  v_auth_username varchar(64),
  v_auth_password text,
  v_auth_url text,
  v_tenant_name varchar(128)
);

DO $$
  DECLARE
    v_provider_id uuid := 'ceab03af-7220-4d42-8f5c-9b557f5d29af';
    v_sd_id       uuid := '072fbaa1-08f3-4a40-9f34-a5ca22dd1d74';
  BEGIN
    IF EXISTS (
         SELECT 1
         FROM   providers
         WHERE  id = v_provider_id
                AND name = 'ovirt-image-repository'
                AND url = 'http://glance.ovirt.org:9292'
       )
       AND NOT EXISTS (SELECT 1 FROM disk_profiles WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM external_leases WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM image_storage_domain_map WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM mb_storage WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM quota_limitation WHERE storage_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM repo_file_meta_data WHERE repo_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM storage_domain_dr WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM storage_domains_ovf_info WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM storage_pool_iso_map WHERE storage_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM unregistered_disks WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM unregistered_ovf_of_entities WHERE storage_domain_id = v_sd_id)
       AND NOT EXISTS (SELECT 1 FROM vm_static WHERE lease_sd_id = v_sd_id)

       AND NOT EXISTS (SELECT 1 FROM cluster WHERE default_network_provider_id = v_provider_id)
       AND NOT EXISTS (SELECT 1 FROM libvirt_secrets WHERE provider_id = v_provider_id)
       AND NOT EXISTS (SELECT 1 FROM network WHERE provider_network_provider_id = v_provider_id)
       AND NOT EXISTS (SELECT 1 FROM vds_static WHERE host_provider_id = v_provider_id)
       AND NOT EXISTS (SELECT 1 FROM vm_static WHERE provider_id = v_provider_id)

       AND NOT EXISTS (
             SELECT 1
             FROM   storage_domain_static
             WHERE  id != v_sd_id
                    AND storage = v_provider_id::text
       )
    THEN
      DELETE FROM storage_domain_dynamic
      WHERE  id = v_sd_id;

      DELETE FROM storage_domain_static
      WHERE  id = v_sd_id
             AND storage = v_provider_id::text;

      DELETE FROM providers
      WHERE  id = v_provider_id;
    END IF;
  END;
$$;
