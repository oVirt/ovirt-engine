UPDATE storage_domain_dynamic
SET    available_disk_size = NULL, used_disk_size = NULL
FROM   storage_domain_static, providers
WHERE  storage_domain_dynamic.id = storage_domain_static.id AND
       storage_domain_static.storage = CAST(providers.id AS VARCHAR) AND
       provider_type IN ('OPENSTACK_IMAGE', 'OPENSTACK_VOLUME');