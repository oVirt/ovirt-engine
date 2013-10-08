-- Create partial index for fetching networks by external ID, when it's not null
CREATE INDEX idx_network_external_id ON network(provider_network_external_id) WHERE provider_network_external_id IS NOT NULL;
