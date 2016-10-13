CREATE TABLE dns_resolver_configuration(
  id UUID PRIMARY KEY
);

CREATE TABLE name_server(
  address VARCHAR(45),
  position SMALLINT,
  dns_resolver_configuration_id UUID REFERENCES dns_resolver_configuration(id) ON DELETE CASCADE,
  PRIMARY KEY (dns_resolver_configuration_id, address)
);


select fn_db_add_column('vds_dynamic', 'dns_resolver_configuration_id', 'UUID REFERENCES dns_resolver_configuration(id) ON DELETE SET NULL');
select fn_db_add_column('network', 'dns_resolver_configuration_id', 'UUID REFERENCES dns_resolver_configuration(id) ON DELETE SET NULL');
select fn_db_add_column('network_attachments', 'dns_resolver_configuration_id', 'UUID REFERENCES dns_resolver_configuration(id) ON DELETE SET NULL');
