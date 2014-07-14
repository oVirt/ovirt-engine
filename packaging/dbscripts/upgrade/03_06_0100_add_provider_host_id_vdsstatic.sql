select fn_db_add_column('vds_static', 'host_provider_id', 'UUID');
ALTER TABLE vds_static ADD CONSTRAINT fk_vds_static_host_provider_id FOREIGN KEY (host_provider_id) REFERENCES providers(id) ON DELETE SET NULL;
