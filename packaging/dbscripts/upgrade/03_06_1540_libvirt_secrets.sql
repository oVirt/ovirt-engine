-- ----------------------------------------------------------------------
--  table libvirt_secrets
-- ----------------------------------------------------------------------

CREATE TABLE libvirt_secrets
(
  secret_id UUID NOT NULL,
  secret_value TEXT NOT NULL,
  secret_usage_type integer NOT NULL,
  secret_description TEXT,
  provider_id UUID NOT NULL,
  _create_date timestamp with time zone DEFAULT ('now'::text)::timestamp without time zone,
  _update_date timestamp with time zone,
  CONSTRAINT PK_secret_id PRIMARY KEY (secret_id),
  FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

CREATE INDEX IDX_libvirt_secrets_provider_id ON libvirt_secrets(provider_id);
