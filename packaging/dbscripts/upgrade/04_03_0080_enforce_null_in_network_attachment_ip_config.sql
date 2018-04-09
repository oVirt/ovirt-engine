UPDATE network_attachments SET address = NULL WHERE address = '';
UPDATE network_attachments SET netmask = NULL WHERE netmask = '';
UPDATE network_attachments SET gateway = NULL WHERE gateway = '';
UPDATE network_attachments SET ipv6_address = NULL WHERE ipv6_address = '';
UPDATE network_attachments SET ipv6_gateway = NULL WHERE ipv6_gateway = '';
