CREATE TABLE external_leases (
    lease_id UUID NOT NULL,
    storage_domain_id UUID NOT NULL,
    CONSTRAINT pk_external_lease PRIMARY KEY (lease_id),
    FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE
);

