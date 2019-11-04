-- ----------------------------------------------------------------------
--  table network_filter
-- ----------------------------------------------------------------------


INSERT INTO network_filter VALUES ('d2370ab4-fee3-11e9-a310-8c1645ce738e', 'vdsm-no-mac-spoofing' , '3.2');
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
