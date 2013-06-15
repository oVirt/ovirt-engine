-- Create partial index for fetching networks by storage pool id
CREATE INDEX IDX_network_storage_pool_id ON network(storage_pool_id);
