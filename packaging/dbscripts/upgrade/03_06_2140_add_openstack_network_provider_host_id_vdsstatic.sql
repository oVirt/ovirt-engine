select fn_db_add_column('vds_static', 'openstack_network_provider_id', 'UUID');
ALTER TABLE vds_static ADD CONSTRAINT fk_vds_static_openstack_network_provider_id FOREIGN KEY (openstack_network_provider_id) REFERENCES providers(id) ON DELETE SET NULL;
