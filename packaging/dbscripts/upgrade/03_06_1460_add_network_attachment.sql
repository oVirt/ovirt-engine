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
