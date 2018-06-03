CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_network_provider_physical_network_id
    ON network (provider_physical_network_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_provider_binding_host_id_vds_id
    ON provider_binding_host_id (vds_id);
