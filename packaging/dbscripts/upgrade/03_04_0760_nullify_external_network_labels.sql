UPDATE network
SET label=NULL
WHERE provider_network_external_id IS NOT NULL;
