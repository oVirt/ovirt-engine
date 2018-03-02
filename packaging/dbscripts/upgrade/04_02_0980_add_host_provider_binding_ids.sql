CREATE TABLE provider_binding_host_id
(
  vds_id UUID REFERENCES vds_static(vds_id) ON DELETE CASCADE,
  plugin_type character varying(64),
  binding_host_id character varying(64),

  CONSTRAINT pk_provider_binding_host_id PRIMARY KEY (vds_id, plugin_type)
);
