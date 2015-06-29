-- Due to a bug that was already fixed, more than one GetCaps could be executed simultaneously.
-- In some scenarios of the race the same nic could be created more than once.
-- The building proccess of the network_attachments assumes there are no such duplicates in vds_interface,
-- so the duplicates should be cleaned up from vds_interface prior building the network_attachments table.
DELETE FROM vds_interface
WHERE id IN (SELECT id
              FROM (SELECT id,
                             row_number() over (partition BY vds_id, name ORDER BY _update_date) AS rnum
                     FROM vds_interface) t
              WHERE t.rnum > 1);

UPDATE vds_interface SET network_name = null
WHERE id in (
    SELECT vds_interface.id
    FROM vds_interface
    JOIN network on vds_interface.network_name = network.name
    WHERE network.vlan_id is not NULL AND vds_interface.vlan_id is NULL
);

-----------------------------
--  network_attachments table
-----------------------------
CREATE TABLE network_attachments
(
  id UUID NOT NULL CONSTRAINT pk_network_attachments_id PRIMARY KEY,
  network_id UUID NOT NULL,
  nic_id UUID NOT NULL,
  boot_protocol CHARACTER VARYING(20),
  address CHARACTER VARYING(20),
  netmask CHARACTER VARYING(20),
  gateway CHARACTER VARYING(20),
  custom_properties TEXT,
  _create_date TIMESTAMP WITH TIME ZONE DEFAULT ('now'::text)::timestamp without time zone,
  _update_date TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE,
  FOREIGN KEY (nic_id) REFERENCES vds_interface (id) ON DELETE CASCADE,
  UNIQUE (network_id, nic_id)
);

CREATE INDEX IDX_network_attachments_nic_id ON network_attachments(nic_id);

-- The network attachments should be populated according to existing data taken from vds_interfaces
-- based on the network existence within the cluster:
-- For each network in vds_interfaces
--      If network exist in cluster
--           Create network attachment entry
--               set network id
--               set nic id or base nic-id if exist


INSERT INTO network_attachments (id,
                                 network_id,
                                 nic_id,
                                 boot_protocol,
                                 address,
                                 netmask,
                                 gateway,
                                 custom_properties)
SELECT uuid_generate_v1(),
       network.id AS network_id,
       coalesce(base_nics.id, vds_interface.id) AS nic_id,
       vds_interface.boot_protocol,
       vds_interface.addr AS address,
       vds_interface.subnet AS netmask,
       vds_interface.gateway,
       vds_interface.custom_properties
FROM vds_interface
JOIN vds_static ON vds_static.vds_id = vds_interface.vds_id
LEFT JOIN vds_interface base_nics ON base_nics.name = vds_interface.base_interface
                                  AND vds_interface.vds_id = base_nics.vds_id
JOIN network_cluster ON vds_static.vds_group_id = network_cluster.cluster_id
JOIN network on network.id = network_cluster.network_id
WHERE vds_interface.network_name is not null
AND network.name = vds_interface.network_name;
