SELECT fn_db_add_column('vds_interface', 'ipv6_boot_protocol', 'integer');
SELECT fn_db_add_column('vds_interface', 'ipv6_address', 'character varying(50)');
SELECT fn_db_add_column('vds_interface', 'ipv6_prefix', 'integer');
SELECT fn_db_add_column('vds_interface', 'ipv6_gateway', 'character varying(50)');
