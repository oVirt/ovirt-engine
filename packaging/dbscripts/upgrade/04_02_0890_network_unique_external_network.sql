ALTER TABLE ONLY network ADD CONSTRAINT network_unique_external_network UNIQUE (storage_pool_id, provider_network_provider_id, provider_network_external_id);
