SELECT fn_db_add_column('network_attachments', 'ipv6_boot_protocol', 'character varying(20) default NULL');
SELECT fn_db_add_column('network_attachments', 'ipv6_address', 'character varying(50) default NULL');
SELECT fn_db_add_column('network_attachments', 'ipv6_prefix', 'int default NULL');
SELECT fn_db_add_column('network_attachments', 'ipv6_gateway', 'character varying(50) default NULL');
