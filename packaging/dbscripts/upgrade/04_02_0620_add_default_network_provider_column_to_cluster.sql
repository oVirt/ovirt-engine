select fn_db_add_column('cluster', 'default_network_provider_id', 'UUID REFERENCES providers(id)');
