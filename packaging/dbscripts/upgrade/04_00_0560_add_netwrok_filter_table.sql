-- ----------------------------------------------------------------------
--  table network_filter
-- ----------------------------------------------------------------------

CREATE TABLE network_filter
(
  filter_id UUID NOT NULL,
  filter_name VARCHAR(50) NOT NULL PRIMARY KEY,
  version VARCHAR(40) NOT NULL
);

-- Create index for filter name
ALTER TABLE network_filter ADD CONSTRAINT unique_network_filter_id UNIQUE (filter_id);

INSERT INTO network_filter VALUES (uuid_generate_v1(), 'vdsm-no-mac-spoofing' , '3.2');

INSERT INTO network_filter VALUES (uuid_generate_v1(), 'allow-arp' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'allow-dhcp' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'allow-dhcp-server' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'allow-incoming-ipv4' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'allow-ipv4' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'clean-traffic' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-arp-ip-spoofing' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-arp-mac-spoofing' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-arp-spoofing' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-ip-multicast' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-ip-spoofing' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-mac-broadcast' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-mac-spoofing' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-other-l2-traffic' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'no-other-rarp-traffic' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'qemu-announce-self' , '3.6');
INSERT INTO network_filter VALUES (uuid_generate_v1(), 'qemu-announce-self-rarp' , '3.6');