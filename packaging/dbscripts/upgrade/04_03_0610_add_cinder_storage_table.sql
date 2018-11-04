CREATE TABLE cinder_storage (
    storage_domain_id UUID NOT NULL,
    driver_options JSONB NOT NULL,
    driver_sensitive_options TEXT,
    CONSTRAINT pk_cinder_storage PRIMARY KEY (storage_domain_id),
    FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE
);

