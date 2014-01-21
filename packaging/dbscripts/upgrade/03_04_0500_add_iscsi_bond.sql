CREATE TABLE iscsi_bonds
(
   id UUID NOT NULL,
   name varchar(50) NOT NULL,
   description varchar(4000),
   storage_pool_id UUID NOT NULL,
   CONSTRAINT PK_iscsi_bonds PRIMARY KEY(id),
   CONSTRAINT FK_iscsi_bonds_storage_pool FOREIGN KEY(storage_pool_id)
       REFERENCES storage_pool(id) ON DELETE CASCADE
) WITH OIDS;


CREATE TABLE iscsi_bonds_networks_map
(
   iscsi_bond_id UUID NOT NULL,
   network_id UUID NOT NULL,
   CONSTRAINT PK_iscsi_bonds_networks_map PRIMARY KEY(iscsi_bond_id,network_id),
   CONSTRAINT FK_iscsi_bonds_networks_map_iscsi_bond_id FOREIGN KEY(iscsi_bond_id)
       REFERENCES iscsi_bonds(id) ON DELETE CASCADE,
   CONSTRAINT FK_iscsi_bonds_networks_map_network_id FOREIGN KEY(network_id)
       REFERENCES network(id) ON DELETE CASCADE
) WITH OIDS;


CREATE TABLE iscsi_bonds_storage_connections_map
(
   iscsi_bond_id UUID NOT NULL,
   connection_id varchar(50) NOT NULL,
   CONSTRAINT PK_iscsi_bonds_storage_connections_map PRIMARY KEY(iscsi_bond_id,connection_id),
   CONSTRAINT FK_iscsi_bonds_storage_connections_map_iscsi_bond_id FOREIGN KEY(iscsi_bond_id)
       REFERENCES iscsi_bonds(id) ON DELETE CASCADE,
   CONSTRAINT FK_iscsi_bonds_storage_connections_map_connection_id FOREIGN KEY(connection_id)
       REFERENCES storage_server_connections(id) ON DELETE CASCADE
) WITH OIDS;