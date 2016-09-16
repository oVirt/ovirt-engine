ALTER TABLE network_attachments ALTER COLUMN boot_protocol SET DEFAULT 'NONE';
ALTER TABLE network_attachments ALTER COLUMN ipv6_boot_protocol SET DEFAULT 'NONE';
ALTER TABLE network_attachments ALTER COLUMN boot_protocol SET NOT NULL;
ALTER TABLE network_attachments ALTER COLUMN ipv6_boot_protocol SET NOT NULL;
