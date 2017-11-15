select fn_db_add_column('network', 'provider_physical_network_id', 'UUID REFERENCES network(id)');
