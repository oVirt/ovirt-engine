CREATE TABLE unregistered_ovf_of_entities
(
   entity_guid UUID,
   entity_name VARCHAR(255) NOT NULL,
   entity_type VARCHAR(32) NOT NULL,
   architecture INTEGER,
   lowest_comp_version VARCHAR(40),
   storage_domain_id UUID,
   ovf_data TEXT,
   ovf_extra_data TEXT,
   CONSTRAINT pk_entity_guid_storage_domain_unregistered PRIMARY KEY(entity_guid, storage_domain_id)
);

ALTER TABLE unregistered_ovf_of_entities add constraint fk_unregistered_ovf_of_entities_storage_domain FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id)  ON DELETE CASCADE;
