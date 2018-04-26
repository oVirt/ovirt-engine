DELETE FROM dns_resolver_configuration USING vds_dynamic
WHERE id = vds_dynamic.dns_resolver_configuration_id;

SELECT fn_db_drop_column('vds_dynamic', 'dns_resolver_configuration_id');
