ALTER TABLE network ADD COLUMN provider_network_provider_id UUID CONSTRAINT fk_network_provided_by REFERENCES providers(id) ON DELETE CASCADE;
ALTER TABLE network ADD COLUMN provider_network_external_id TEXT;

